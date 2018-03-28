package com.example.dickman.myapplication.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.dickman.myapplication.network.TCP_Connect;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by HatsuneMiku on 2018/3/28.
 */

public class PhoneAnswerListener extends Service {
    private final IBinder mBinder = new LocalBinder();
    TCP_Connect tcp_connect;

    Runnable threadForListen  = new Runnable() {
        @Override
        public void run() {
            SharedPreferences settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
            String password = settings.getString("password", "");
            String socketId = settings.getString("socketId", "");
            if(password.equals("") || socketId.equals("")) {
                return;
            }
            tcp_connect.inputPassword(password);
            DatagramSocket socket = tcp_connect.getUdpSocket(socketId);
            byte b[] = new byte[10];
            DatagramPacket pk = new DatagramPacket(b, b.length);
            for(;;) {
                try {
                    socket.receive(pk);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public class LocalBinder extends Binder {
        String getToken() {
            return tcp_connect.getToken();
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
