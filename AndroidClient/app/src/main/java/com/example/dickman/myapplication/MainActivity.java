package com.example.dickman.myapplication;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    private final String PhoneKey = "Phone";
    private final String RaspberryKey = "Raspberry";

    private final String PhoneVideoKey = "VideoPhone";
    private final String RaspberryVideoKey = "VideoRaspberry";

    private int serverPort = 7777;
    private int serverUdpPort = 8888;
    private int timeout = 0;
    private String serverHost = "140.128.88.166";
    private EditText passEdit = null;
    private ImageView imageView;
    final Object audioLock = new Object();
    Audio audio = null;
    Video video = null;
    TCP_Connect tcp_connect;

    private class PacketClass implements Serializable{
        public DatagramSocket socket;
        public String cientHost, token;
        public int SentPort;
    }
    static class MyHandler extends Handler {
        static final int ON_AUDIO_START = 0;
        static final int ON_VIDEO_START = 1;
        static final int ON_IMAGE_AVAILABLE = 2;
        private WeakReference<MainActivity> mOuter;

        MyHandler(MainActivity activity) {
            mOuter = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity outer = mOuter.get();
            if (outer != null) {
                PacketClass packetClass;
                switch (msg.arg1){
                    case ON_AUDIO_START:
                        packetClass = (PacketClass) msg.getData().getSerializable("audio");
                        if(packetClass == null)
                            break;
                        outer.audio = new Audio(packetClass.socket, packetClass.cientHost, packetClass.SentPort,
                                outer.timeout, packetClass.token);
                        break;
                    case ON_VIDEO_START:
                        packetClass = (PacketClass) msg.getData().getSerializable("video");
                        if(packetClass == null)
                            break;
                        outer.video = new Video(packetClass.socket, packetClass.cientHost, packetClass.SentPort,
                                outer.timeout);
                        break;
                    case ON_IMAGE_AVAILABLE:
                        break;
                }
            }
        }
    }

    private MyHandler mHandler = new MyHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        passEdit =  findViewById(R.id.editText);
        imageView = findViewById(R.id.image);
        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(video != null) {
                    Bitmap bmp = video.getFrame();
                    if(bmp != null) {
                        imageView.setImageBitmap(bmp);
                    }
                }
                imageView.postDelayed(this, 10);
            }
        }, 10);
    }

    public void clickcall_end(View view) {
        synchronized (audioLock) {
            if (audio != null) {
                audio.close();
                audio = null;
            }
        }
    }

    public PacketClass getSetting(TCP_Connect tcp_connect, String from, String to) {
        PacketClass packetClass = new PacketClass();
        if(tcp_connect == null || tcp_connect.getToken() == null) {
            return null;
        }
        packetClass.socket = tcp_connect.getUdpSocket(from);
        while (packetClass.socket == null) {
            packetClass.socket = tcp_connect.getUdpSocket(from);
        }
        String tmp[] = tcp_connect.getSocketIpPort(to).split(" ");
        packetClass.cientHost = tmp[0];
        packetClass.SentPort = Integer.valueOf(tmp[1]);
        while (packetClass.cientHost.equals("0.0.0.0") || packetClass.SentPort == 0) {
            tmp = tcp_connect.getSocketIpPort(to).split(" ");
            packetClass.cientHost = tmp[0];
            packetClass.SentPort = Integer.valueOf(tmp[1]);
            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        packetClass.token = tcp_connect.getToken();
        return packetClass;
    }

    public void clickcall_start(View view) {


        final String password = passEdit.getText().toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
            synchronized (audioLock) {
                if (audio == null) {
                    try {
                        tcp_connect = new TCP_Connect(serverHost, serverPort, serverUdpPort);
                        if (tcp_connect.inputPassword(password)) {
                            PacketClass packetClass = getSetting(tcp_connect, PhoneKey, RaspberryKey);
                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("audio", packetClass);
                            msg.arg1 = MyHandler.ON_AUDIO_START;
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);

                            PacketClass packetClass1 = getSetting(tcp_connect, PhoneVideoKey, RaspberryVideoKey);
                            Message msg1 = new Message();
                            Bundle bundle1 = new Bundle();
                            bundle1.putSerializable("video", packetClass1);
                            msg1.arg1 = MyHandler.ON_VIDEO_START;
                            msg1.setData(bundle1);
                            mHandler.sendMessage(msg1);
                        } else {
                            Toast.makeText(MainActivity.this, "password error or network is unavailable", Toast.LENGTH_LONG);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            }
        }).start();
    }

}




