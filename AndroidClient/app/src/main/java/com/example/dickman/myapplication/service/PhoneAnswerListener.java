package com.example.dickman.myapplication.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.dickman.myapplication.MainActivity;
import com.example.dickman.myapplication.R;
import com.example.dickman.myapplication.network.TCP_Connect;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by HatsuneMiku on 2018/3/28.
 */

public class PhoneAnswerListener extends Service {
    private final IBinder mBinder = new LocalBinder();

    public static final byte ON_PHONE_CALL = 0x42;//通話持續
    public static final byte ALIVE_CALL = 0x53;//來電通話
    public static final byte HANG_UP_CALL = 0x21;//掛斷通話

    TCP_Connect tcp_connect = null;
    DatagramSocket socket = null;
    String password, listenSocketId = "phoneListenSocket", sendSocketId = "raspberryListenSocket";
    InetAddress ip;
    int port;
    ServiceMainThread listeningThread = null;
    boolean haveCall = false;
    boolean isCalling = false;
    boolean answerCall = false;
    boolean hangUpCall = false;
    boolean initFinish  = false;
    boolean passwordError = false;
    long soTimeout = 20000;

    class ServiceMainThread extends Thread {
        boolean isRunning = false;

        @Override
        public synchronized void start() {//服務起動訊號
            isRunning = true;
            super.start();
        }

        @Override
        public void run() {

            if(tcp_connect == null) {//如果尚未連線，則進行連線
                try {
                    tcp_connect = new TCP_Connect(MainActivity.serverHost, MainActivity.serverPort, MainActivity.serverUdpPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(tcp_connect.inputPassword(password)) {//判斷密碼
                initFinish = true;
                do {//取得port，如果拿到的port是0，則重複一直拿，直到得到port為止
                    String ip_port[] = tcp_connect.getSocketIpPort(sendSocketId).split(" ");
                    try {
                        ip = InetAddress.getByName(ip_port[0]);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    port = Integer.valueOf(ip_port[1]);
                } while(port == 0);

                socket = tcp_connect.getUdpSocket(listenSocketId);//傳送token
                byte b[] = new byte[200];
                int offset = tcp_connect.getToken().length() + sendSocketId.length() + 2;
                System.arraycopy(tcp_connect.getToken().getBytes(), 0, b, 0, tcp_connect.getToken().length());
                b[tcp_connect.getToken().length()] = ' ';
                System.arraycopy(sendSocketId.getBytes(), 0, b, tcp_connect.getToken().length() + 1, sendSocketId.length());
                b[offset - 1] = ' ';
                DatagramPacket icmp = new DatagramPacket(b, offset + 1, ip, port);
                DatagramPacket pk = new DatagramPacket(b, offset, b.length - offset);
                int loseConnectionCount = 0;
                for (; isRunning; ) {
                    try {
                        if(hangUpCall) {//如果通話結束，全部關閉
                            b[offset] = HANG_UP_CALL;
                            socket.send(icmp);
                            isCalling = false;
                            haveCall = false;
                            answerCall = false;
                            hangUpCall = false;
                            loseConnectionCount = 0;
                        }
                        else if(isCalling) {//否則就是持續通話
                            b[offset] = ON_PHONE_CALL;
                            socket.send(icmp);
                            socket.receive(pk);
                            if(pk.getLength() > 1) {
                                continue;
                            }
                            if(b[offset] == HANG_UP_CALL) {
                                hangUpCall = true;
                            } else if(b[offset] == ALIVE_CALL || b[offset] == ON_PHONE_CALL) {
                                isCalling = false;
                                answerCall = true;
                                Intent intent = new Intent();
                                intent.setAction(getString(R.string.answer_call));//設定一個自定義的action，此為來電通話
                                sendBroadcast(intent);
                            }
                        } else if (answerCall) {//如果來電通話
                            if(loseConnectionCount > 2) {//超過一定時間沒接，則視為錯過通話
                                hangUpCall = true;
                                answerCall = false;
                                loseConnectionCount = 0;
                                Intent missConnectionIntent = new Intent();
                                missConnectionIntent.setAction(getString(R.string.miss_connection));////設定一個自定義的action，此為錯過來電
                                sendBroadcast(missConnectionIntent);
                                continue;
                            }
                            b[offset] = ALIVE_CALL;
                            socket.send(icmp);
                            long theTime = System.currentTimeMillis();//從起動開始計算毫秒
                            loseConnectionCount += 1;
                            socket.receive(pk);
                            if(pk.getLength() > 1) {
                                continue;
                            }
                            if(b[offset] == HANG_UP_CALL) {
                                hangUpCall = true;
                                continue;
                            } else if(b[offset] == ALIVE_CALL) {
                            } else {
                                continue;
                            }
                            theTime = System.currentTimeMillis() - theTime;
                            theTime = 1000 - theTime;
                            if(theTime > 0)
                                Thread.sleep(theTime);
                            loseConnectionCount = 0;
                        } else {
                            socket.receive(pk);
                            if(pk.getLength() > 1) {
                                continue;
                            }
                            if(b[offset] == ALIVE_CALL){
                                hangUpCall = true;
                            } else if (!haveCall && b[offset] == ON_PHONE_CALL) {//如果通話持續
                                Intent onPhoneCallIntent = new Intent();
                                onPhoneCallIntent.setAction(getString(R.string.on_phone_call));//設定一個自定義的action，此為通話持續
                                sendBroadcast(onPhoneCallIntent);
                                synchronized (PhoneAnswerListener.this) {
                                    haveCall = true;
                                }
                            }
                        }
                    } catch (IOException | InterruptedException ignored) {}
                }
            } else {
                passwordError = true;
                initFinish = true;
            }
            isRunning = false;
        }

        synchronized void stopRunning() {
            isRunning = false;
        }
    }

    public class LocalBinder extends Binder {
        public PhoneAnswerListener getService() {
            return PhoneAnswerListener.this;
        }
    }

    public synchronized void restartListening(String password) {//重起監聽服務
        if(password.equals(this.password)){//判斷密碼
            return;
        }
        this.password = password;
        if(listeningThread != null) {//關閉前一個監聽
            listeningThread.stopRunning();
        }
        listeningThread = new ServiceMainThread();//開啟一個新的監聽
        listeningThread.start();
        initFinish = false;
        passwordError = false;
    }

    //判斷是接電話還是掛電話
    public void answerPhoneCall(boolean answer) {
        if(socket != null && haveCall) {
            if(answer) {
                answerCall = true;
            } else {
                hangUpCall = true;
            }
        }
    }

    public void makeACall() {
        isCalling = true;
    }

    public boolean isInit() {
        return initFinish;
    }

    public boolean isPasswordError() {
        return passwordError;
    }

    public TCP_Connect getTCP_Client() {
        return tcp_connect;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
        password = settings.getString("password", "");

        if(password.equals("")) {//如果是接電話，就算沒密碼也可行
            passwordError = true;
            initFinish = true;
            return ;
        }

        listeningThread = new ServiceMainThread();
        listeningThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent();
        intent.setAction(getString(R.string.service_closing));
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
