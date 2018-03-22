import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

class Data_Set{
    public static final String MY_IP = "192.168.0.138";
}

class SlidingWindow extends Thread{
    class Window {
        private static final int IDLE = 0;
        private static final int WAIT_FOR_RES = 1;
        private static final int END = 2;

        private byte stamp = 0;
        private byte recvStamp = 0;
        private List<Integer> recvWindows = new LinkedList<>();
        private List<Integer> windows = new LinkedList<>();
        private List<Long> windowsTime = new LinkedList<>();
        private List<DatagramPacket> windowsPacket = new LinkedList<>();
        private long timeout;

        Window(byte windowSize, long timeout) {
            this.timeout = timeout;
            windows = new ArrayList<>();
            for (int i = 0; i < windowSize; ++i) {
                windows.add(IDLE);
                recvWindows.add(IDLE);
                windowsPacket.add(null);
                windowsTime.add(0L);
            }
        }

        private void init() {
            stamp = 0;
            recvStamp = 0;
            for (int i = 0; i < windows.size(); ++i) {
                windows.set(i, IDLE);
            }
        }

        boolean haveIdle() {
            int windowCount = -1;
            for (int i = 0; i < windows.size(); ++i) {
                if (windows.get(i) == IDLE) {
                    windowCount = (i + stamp) % 128;
                    break;
                }
            }
            return (windowCount != -1);
        }

        DatagramPacket pktData(byte[] data, int offset, int dataLength, InetAddress ip, int port) {
            synchronized (windows) {
                int windowCount = -1;
                int rawCount = -1;
                for (int i = 0; i < windows.size(); ++i) {
                    if (windows.get(i) == IDLE) {
                        windowCount = (i + stamp) % 128;
                        rawCount = i;
                        break;
                    }
                }
                if (windowCount == -1) {
                    return null;
                }

                if (data.length - dataLength - offset > 0) {
                    data[dataLength + offset] = (byte) windowCount;
                    dataLength += 1;
                } else {
                    byte[] dest = new byte[dataLength + 1];
                    System.arraycopy(data, offset, dest, 0, dataLength);
                    dest[dataLength] = (byte) windowCount;
                    dataLength += 1;
                    data = dest;
                    offset = 0;
                }
                DatagramPacket _dp = new DatagramPacket(data, offset, dataLength, ip, port);
                windowsPacket.set(rawCount, _dp);
                windowsTime.set(rawCount, System.currentTimeMillis());
                windows.set(rawCount, WAIT_FOR_RES);
                return _dp;
            }
        }

        DatagramPacket unpktData(MulticastSocket socket, DatagramPacket data) {
            synchronized (recvWindows) {
                byte[] d = new byte[1];
                d[0] = data.getData()[data.getOffset() + data.getLength() - 1];
                DatagramPacket _dp = new DatagramPacket(d, d.length, data.getAddress(), data.getPort());
                try {
                    socket.send(_dp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int index = confirmBorder(recvStamp, d[0]);
                if (index == -1)
                    return null;
                if (recvWindows.get(index) == END) {
                    return null;
                }
                recvWindows.set(index, END);
                int counter = 0;
                for (int i = 0; i < recvWindows.size(); ++i) {
                    if (recvWindows.get(i) != END) {
                        break;
                    }
                    counter += 1;
                }
                for (int i = 0; i < counter; ++i) {
                    recvWindows.remove(0);
                    recvWindows.add(IDLE);
                }

                if (recvStamp + counter >= 128) {
                    recvStamp = (byte) ((recvStamp + counter) % 128);
                } else {
                    recvStamp += counter;
                }
                data.setLength(data.getLength() - 1);
                return data;
            }
        }

        void update(MulticastSocket socket) { // check atk timeout
            synchronized (windows) {
                for (int i = 0; i < windows.size(); ++i) {
                    if (windows.get(i) != WAIT_FOR_RES) {
                        continue;
                    }

                    if (System.currentTimeMillis() - windowsTime.get(i) > timeout) {
                        try {
                            socket.send(windowsPacket.get(i));
                            windowsTime.set(i, System.currentTimeMillis());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        int confirmBorder(byte stamp, byte d) {
            int index;
            if (stamp + windows.size() > 127) {
                int min = (stamp + windows.size()) % 128;
                int top = stamp;
                if(min < d && d < top ) {
                    System.out.println("ERROR stamp: " + d);
                    return -1;
                }
                if (stamp <= d) {
                    index = d - stamp;
                } else {
                    index = d + (128 - stamp);
                }
            } else {
                int top = stamp + windows.size() - 1;
                int min = stamp - 1;
                if(d < min || top < d) {
                    System.out.println("ERROR stamp: " + d);
                    return -1;
                }
                index = d - stamp;
            }
            return index;
        }

        void recvATK(DatagramPacket data) {
            synchronized (windows) {
                byte d = data.getData()[data.getLength() - 1];
                if(d == -1) {
                    init();
                    System.out.println("window init");
                    return;
                }
                int index = confirmBorder(stamp, d);
                System.out.println("recv atk : "  + d);
                if(index == -1)
                    return;
                if(windows.get(index) != WAIT_FOR_RES)
                    return;
                windows.set(index, END);

                int counter = 0;
                for (int i = 0; i < windows.size(); ++i) {
                    if (windows.get(i) == END) {
                        counter++;
                    } else {
                        break;
                    }
                }

                for(int i = 0; i < counter; ++i) {
                    windows.remove(0);
                    windows.add(IDLE);
                    windowsTime.remove(0);
                    windowsTime.add(0L);
                    windowsPacket.remove(0);
                    windowsPacket.add(null);
                }

                if (stamp + counter > 127) {
                    stamp = (byte) ((stamp + counter) % 128);
                } else {
                    stamp += counter;
                }
            }
        }
    }
    private boolean isStop = false;
    private long timeout;
    private MulticastSocket socket;
    private Map<String, Window> windowMap = new TreeMap<>();

    public SlidingWindow(final MulticastSocket socket, final byte windowSize, final long timeout) {
        this.timeout = timeout;
        this.socket = socket;
        try {
            this.socket.setSoTimeout(0);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!isStop) {
                    try {
                        byte[] b = new byte[65505];
                        DatagramPacket pk = new DatagramPacket(b, b.length);
                        socket.receive(pk);

                        byte[]  pk_data = pk.getData();
                        int pk_length;
                        String token[] = new String(pk_data, 0, SocketProcess.PasswordLength() + 128).split(" ");
                        if(token.length < 3){
                            continue;
                        }
                        Map<String, String> set = TimeLimitMap.getData_Set(token[0]);
                        if(set == null){
                            continue;
                        }
                        String set_data = set.get(token[2]);
                        if(set_data == null) {
                            continue;
                        }
                        String check = set.get(token[1]);
                        if(check == null) {
                            continue;
                        }
                        Window window = windowMap.get(token[2]);
                        if(window == null) {
                            window = new Window(windowSize, timeout);
                            windowMap.put(token[2], window);
                        }
                        Window srcwindow = windowMap.get(token[1]);
                        if(srcwindow == null) {
                            srcwindow = new Window(windowSize, timeout);
                            windowMap.put(token[1], srcwindow);
                        }
                        int offset = token[0].length() + token[1].length() + token[2].length() + 3;
                        if(pk.getLength() - offset == 1) {
                            window.recvATK(pk);
                            continue;
                        }
                        String ipAndPort[] = set_data.split(" ");
                        if(!window.haveIdle()) {
                            continue;
                        }
                        DatagramPacket _pk = window.unpktData(socket, pk);
                        if(_pk == null)
                            continue;
                        pk_length = _pk.getLength();
                        pk_length -= offset;
                        DatagramPacket p = srcwindow.pktData(_pk.getData(), offset, pk_length, InetAddress.getByName(ipAndPort[0]), Integer.valueOf(ipAndPort[1]));
                        if(p == null)
                            continue;
                        socket.send(p);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        start();
    }

    @Override
    public void run() {
        while(!isStop) {
            try {
                Set<Map.Entry<String, Window>> entries = windowMap.entrySet();
                for(Map.Entry<String, Window> map : entries) {
                    map.getValue().update(socket);
                }
                int i = Math.max(1, entries.size());
                Thread.sleep((long) (timeout / i * 0.7));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class TimeLimitMap { //TODO ??tokenMap????
    private static final Map<String, String> tokenMap = new TreeMap<>();
    private static final Map<String, Map<String, String>> portMap = new TreeMap<>();
    public TimeLimitMap(String pass, String token) {
        tokenMap.put(pass, token);
        portMap.put(token, new TreeMap<String, String>());
    }

    public static String getToken(String pass) {
        return tokenMap.get(pass);
    }

    public static Map<String, String> getData_Set(String token){
        return portMap.get(token);
    }
}

class TCPForward extends Thread {
    class SocketPair {
        SocketPair(String key, Socket socket) {
            this.key = key; this.socket = socket;
        }
        Socket socket;
        String key;
    }

    public TCPForward() {
        start();
    }
    List<SocketPair> tcpList = new LinkedList<>();
    Map<String, Socket> tcpMap = new TreeMap<>();
    boolean isStop = false;

    public void addSocket(String key, Socket socket) {
        tcpList.add(new SocketPair(key, socket));
        tcpMap.put(key, socket);
    }

    void removePair(SocketPair pair) {
        tcpMap.remove(pair.key, pair.socket);
        tcpList.remove(pair);
    }

    @Override
    public void run() {
        byte[] buffer = new byte[65505];
        while(!isStop) {
            int length = tcpList.size();
            for(int i = 0; i < length; ++i) {
                SocketPair pair = tcpList.get(i);
                try {
                    if(pair.socket.isClosed()) {
                        removePair(pair);
                        --i;
                        continue;
                    }
                    int bufferLength = -1;
                    try {
                        bufferLength = pair.socket.getInputStream().read(buffer);
                    }catch (Exception e){
                        continue;
                    }
                    if(bufferLength == -1)
                        continue;
                    pair.socket.setSoTimeout(0);
                    while(buffer[bufferLength - 2] != '\r' && buffer[bufferLength - 1] != '\n'){
                        int tmp = pair.socket.getInputStream().read(buffer, bufferLength, buffer.length - bufferLength);
                        if(tmp == -1)
                            break;
                        bufferLength += tmp;
                    }
                    pair.socket.setSoTimeout(100);

                    String token[] = new String(buffer, 0, 128).split(" ");
                    if(token.length < 2){
                        pair.socket.close();
                        removePair(pair);
                        --i;
                        return;
                    }
                    Map<String, String> set = TimeLimitMap.getData_Set(token[0]);
                    if(set == null){
                        pair.socket.close();
                        removePair(pair);
                        --i;
                        return;
                    }
                    Socket socket = tcpMap.get(token[1]);
                    if(socket == null) {
                        return;
                    }
                    int offset = token[0].length() + token[1].length() + 2;
                    System.out.println("TCP: " + (bufferLength - offset));
                    socket.getOutputStream().write(buffer, offset, bufferLength - offset);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

class SocketProcess extends Thread{
    private Socket socket;
    private OutputStream ostream;
    private InputStream istream;
    private boolean isStop;
    private static MessageDigest digest;
    private static final List<SocketProcess> processList = new LinkedList<>();
    private static final Set<String> passwordSet;
    private static int passwordLength;

    static{
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        passwordSet = new TreeSet<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("password.txt"));
            int length = 0;
            for(String line; (line = br.readLine()) != null; ) {
                passwordSet.add(line);
                length = line.length();
            }
            passwordLength = length;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<SocketProcess> getAllProcess() {
        return processList;
    }

    public static int PasswordLength() {
        return passwordLength;
    }

    public SocketProcess(Socket socket){
        if(socket.isClosed()) {
            isStop = true;
            return;
        }
        else {
            try {
                this.ostream = socket.getOutputStream();
                this.istream = socket.getInputStream();
                this.socket = socket;
                isStop = false;
                synchronized(processList) {
                    processList.add(this);
                }
            } catch (IOException e) {
                isStop = true;
                System.out.print(e.toString());
            }
        }
        start();
    }

    @Override
    public void run() {
        super.run();
        byte inputBuffer[] = new byte[1024];
        System.out.println(socket.getInetAddress().toString() + " " + socket.getPort() + " is connected");
        while(!isStop){
            try {
                int length = istream.read(inputBuffer);
                if(length > inputBuffer.length)
                    return;
                String inputString = new String(inputBuffer, 0, length);
                System.out.print(inputString);
                byte[] yourPas = digest.digest(inputString.getBytes());
                String hex = byteArrayToHexString(yourPas, yourPas.length);
                if(passwordSet.contains(hex)){
                    String tokenHex = TimeLimitMap.getToken(hex);
                    if(tokenHex == null) {
                        byte token[] = digest.digest((inputString + "ABCV" + System.currentTimeMillis()).getBytes());
                        tokenHex = byteArrayToHexString(token, token.length);
                        new TimeLimitMap(hex, tokenHex);
                    }
                    ostream.write(tokenHex.getBytes());
                    socket.close();
                    isStop = true;
                } else {
                    ostream.write("password error".getBytes());
                }
            } catch (IOException e) {
                isStop = true;
                e.printStackTrace();
            }
        }
        try {
            if(socket.isConnected())
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        synchronized(processList) {
            processList.remove(this);
        }
    }

    public static String byteArrayToHexString(byte[] b, int length) {
        String result = "";
        for (int i=0; i < length; i++) {
            result +=
                    Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

    public String getHostIp() {
        return socket.getInetAddress().toString();
    }

    public int getHostPort() {
        return socket.getPort();
    }

    public void close() {
        isStop = true;
    }
}

class UDP_Server extends Thread { // Client set/get port
    protected MulticastSocket socket;
    protected boolean isStop;

    public UDP_Server(int port) {
        try {
            socket = new MulticastSocket(port);
            isStop = false;
            start();
        } catch (IOException e) {
            isStop = true;
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        byte[] b = new byte[65505];
        DatagramPacket pk = new DatagramPacket(b, b.length);
        while(!isStop) {

            try {
                socket.receive(pk);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(pk.getLength() > 128) {
                new Transfer_UDP(pk, socket).start();
                continue;
            }
            String datas = new String(pk.getData(), 0, pk.getLength());
            String[] data = datas.split(" ");
            if(data.length != 3) {
                continue;
            }
            Map<String, String> set = TimeLimitMap.getData_Set(data[0]);
            if(set == null) {
                continue;
            }
            if(!data[1].equals("s") && !data[1].equals("g") && !data[1].equals("t") && !data[1].equals("w")){
                continue;
            }

            System.out.println("get " + pk.getAddress().toString() + " " + pk.getPort() + " request");
            switch (data[1]) {
                case "s":
                    set.put(data[2], pk.getAddress().toString().substring(1) + " " + pk.getPort());
                    try {
                        byte[] _data = "OK".getBytes();
                        DatagramPacket _dp = new DatagramPacket(_data, _data.length,
                                pk.getAddress(), pk.getPort());
                        socket.send(_dp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "g": //UDP PORT IP
                    try {
                        byte[] _data = (Data_Set.MY_IP + " " + String.valueOf(socket.getLocalPort())).getBytes();
                        DatagramPacket _dp = new DatagramPacket(_data, _data.length, pk.getAddress(), pk.getPort());
                        socket.send(_dp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "t": //TCP PORT IP
                    try{
                        byte[] _data = (Data_Set.MY_IP + " " + String.valueOf(socket.getLocalPort())).getBytes();
                        DatagramPacket _dp = new DatagramPacket(_data, _data.length, pk.getAddress(), pk.getPort());
                        socket.send(_dp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "w": //UDP PORT IP
                    try {
                        byte[] _data = (Data_Set.MY_IP + " " + String.valueOf(Server.sliding_port)).getBytes();
                        DatagramPacket _dp = new DatagramPacket(_data, _data.length, pk.getAddress(), pk.getPort());
                        socket.send(_dp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    public void close() {
        isStop = true;
    }
}

class Transfer_UDP extends Thread {

    DatagramPacket pk;
    static MulticastSocket socket = null;

    public Transfer_UDP(DatagramPacket pk, MulticastSocket socket) {
        this.pk = pk;
        if(this.socket == null)
            this.socket = socket;
    }
    public Transfer_UDP(DatagramPacket pk) {
        this.pk = pk;
        if(socket == null)
            try {
                this.socket = new MulticastSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void run() {
        if(pk.getLength() < 128) {
            return;
        }
        byte[]  pk_data = pk.getData();
        int pk_length = pk.getLength();
        String token[] = new String(pk_data, 0, SocketProcess.PasswordLength() + 128).split(" ");
        if(token.length < 2){
            return;
        }
        Map<String, String> set = TimeLimitMap.getData_Set(token[0]);
        if(set == null){
            return;
        }
        String set_data = set.get(token[1]);
        if(set_data == null) {
            return;
        }
        String ipAndPort[] = set_data.split(" ");
        int offset = (SocketProcess.PasswordLength() + token[1].length() + 2);
        try {
            DatagramPacket _dp =
                    new DatagramPacket(pk_data, offset, pk_length - offset,
                            InetAddress.getByName(ipAndPort[0]),
                            Integer.valueOf(ipAndPort[1]));
            System.out.println("UDP: " + pk_length);
            if(socket != null)
                socket.send(_dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class Server {
    private static ServerSocket serverSocket;
    private static ServerSocket tcpFoewardSocket;
    public static int serverport = 7777;
    public static int udp_server_port = 8888;
    public static int sliding_port = 8080;


    public static void main (String[] args){

        try {
            serverSocket = new ServerSocket(serverport);
            tcpFoewardSocket = new ServerSocket(udp_server_port);
            MulticastSocket slidingSocket = new MulticastSocket(sliding_port);
            SlidingWindow slidingWindow = new SlidingWindow(slidingSocket, (byte)10, 100);
            new Thread(new Runnable() {
                private boolean isStop = false;
                @Override
                public void run() {
                    try {
                        while(!isStop)
                            new SocketProcess(serverSocket.accept());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                public void close() {
                    isStop = true;
                }
            }).start();

            new Thread(new Runnable() {
                private boolean isStop = false;
                TCPForward forward = new TCPForward();

                class WaitForKey extends Thread {
                    private Socket socket;
                    WaitForKey(Socket socket){
                        this.socket = socket;
                    }
                    @Override
                    public void run() {
                        byte[] b = new byte[128];
                        try{
                            socket.setSoTimeout(100);
                            int length = socket.getInputStream().read(b);
                            String[] token = new String(b, 0, length).split(" ");
                            if(token.length < 1) {
                                socket.close();
                                return;
                            }

                            Map<String, String> set = TimeLimitMap.getData_Set(token[0]);
                            if(set == null){
                                socket.close();
                                return;
                            }

                            socket.getOutputStream().write("OK".getBytes());
                            forward.addSocket(token[1], socket);

                        } catch (IOException e) {
                            try {
                                if(!socket.isClosed())
                                    socket.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }
                }
                @Override
                public void run() {
                    try {
                        while(!isStop)
                            new WaitForKey(tcpFoewardSocket.accept()).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                public void close() {
                    isStop = true;
                }
            });

            new UDP_Server(udp_server_port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
