package com.example.dickman.myapplication.service;

import android.app.IntentService;
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

/**
 * Created by HatsuneMiku on 2018/3/28.
 */

public class PhoneAnswerListener extends Service {
    private final IBinder mBinder = new LocalBinder();
    public static final byte ON_PHONE_CALL = 0x42;
    TCP_Connect tcp_connect;
    String password, socketId;
    MyThread listeningThread = null;

    class MyThread extends Thread {
        boolean isRunning = false;

        @Override
        public synchronized void start() {
            isRunning = true;
            super.start();
        }

        @Override
        public void run() {

            try {
                tcp_connect = new TCP_Connect(MainActivity.serverHost, MainActivity.serverPort, MainActivity.serverUdpPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
            tcp_connect.inputPassword(password);
            DatagramSocket socket = tcp_connect.getUdpSocket(socketId);
            byte b[] = new byte[1];
            DatagramPacket pk = new DatagramPacket(b, b.length);
            for (;isRunning;) {
                try {
                    socket.receive(pk);
                    if (b[0] == ON_PHONE_CALL) {
                        Intent onPhoneCallIntent = new Intent();
                        onPhoneCallIntent.setAction(getString(R.string.on_phone_call));
                        sendBroadcast(onPhoneCallIntent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

    public synchronized void restartListening(String password) {
        this.password = password;
        if(listeningThread != null) {
            listeningThread.stopRunning();
        }
        listeningThread = new MyThread();
        listeningThread.start();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
        password = settings.getString("password", "");
        socketId = settings.getString("socketId", "phoneListenSocket");

        if(password.equals("") || listeningThread != null) {
            return ;
        }

        listeningThread = new MyThread();
        listeningThread.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
