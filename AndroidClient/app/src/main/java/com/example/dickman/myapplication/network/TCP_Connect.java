package com.example.dickman.myapplication.network;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.InetAddress;



public class TCP_Connect {
    private int serverPort;
    private int serverUdpPort;
    private OutputStream bw;//取得網路輸出串流
    private InputStream br;//取得網路輸入串流
    private InetAddress serverIp;
    private String token = null;
    private DatagramSocket socket = null;

    public TCP_Connect(String serverHost, int serverPort, int serverUdpPort) throws IOException {
        serverIp = InetAddress.getByName(serverHost);
        this.serverPort = serverPort;
        this.serverUdpPort = serverUdpPort;
        socket = new DatagramSocket();
    }

    public boolean inputPassword(String pass) {
        try {
            Socket clientSocket = new Socket(serverIp, serverPort);
            clientSocket.setSoTimeout(1000);
            bw = clientSocket.getOutputStream();// 取得網路輸出串流
            br = clientSocket.getInputStream();//取得網路輸入串流
            bw.write(pass.getBytes());
            byte[] buffer = new byte[256];
            int length = br.read(buffer);
            String data = new String(buffer, 0, length);
            if(data.length() < 16){
                clientSocket.close();
                return false;
            }
            token = data;
            clientSocket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String getToken() {
        return token;
    }

    public String getSocketIpPort(String key){
        try {
            if (socket == null)
                return "";
            String str = token + " g " + key;
            DatagramPacket pk = new DatagramPacket(str.getBytes(), str.getBytes().length, serverIp, serverUdpPort);
            socket.send(pk);
            socket.receive(pk);
            return new String(pk.getData(), 0, pk.getLength());
        }catch (IOException e) {
            return "0.0.0.0 0";
        }
    }

    public DatagramSocket getUdpSocket(String key) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(1000);
            String str = token + " s " + key;
            DatagramPacket pk = new DatagramPacket(str.getBytes(), str.getBytes().length, serverIp, serverUdpPort);
            socket.send(pk);
            socket.receive(pk);
            Log.d("aASDSADAS", new String(pk.getData(), 0, pk.getLength()));
        } catch (IOException e) {
            return null;
        }
        return socket;
    }

    public String getSlidingIp_Port(String key) {
        try {
            if (socket == null)
                return "";
            String str = token + " w " + key;
            DatagramPacket pk = new DatagramPacket(str.getBytes(), str.getBytes().length, serverIp, serverUdpPort);
            socket.send(pk);
            socket.receive(pk);
            return new String(pk.getData(), 0, pk.getLength());
        }catch (IOException e) {
            return "0.0.0.0 0";
        }
    }
}