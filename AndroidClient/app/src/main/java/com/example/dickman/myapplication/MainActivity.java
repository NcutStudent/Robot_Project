package com.example.dickman.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dickman.myapplication.network.TCP_Connect;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.DatagramSocket;

public class MainActivity extends AppCompatActivity {
    final String PhoneKey = "Phone";
    final String RaspberryKey = "Raspberry";

    final String PhoneVideoKey = "VideoPhone";
    final String RaspberryVideoKey = "VideoRaspberry";

    private int serverPort = 7777;
    private int serverUdpPort = 8888;
    private int timeout = 0;
    private String serverHost = "140.128.88.166";
    private EditText passEdit = null;
    private SurfaceView surfaceView;
    final Object audioLock = new Object();
    Audio audio = null;
    VideoThread video = null;
    CameraDevice cameraDevice = null;
    TCP_Connect tcp_connect;

    private class PacketClass implements Serializable {
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
                switch (msg.arg1) {
                    case ON_AUDIO_START:
                        packetClass = (PacketClass) msg.getData().getSerializable("audio");
                        if (packetClass == null)
                            break;
                        outer.audio = new Audio(packetClass.socket, packetClass.cientHost, packetClass.SentPort,
                                outer.timeout, packetClass.token);
                        break;
                    case ON_VIDEO_START:
                        outer.video = new VideoThread(outer.tcp_connect, outer.cameraDevice, outer.surfaceView.getHolder().getSurface(), 640, 480);
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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        passEdit = findViewById(R.id.editText);
        surfaceView = findViewById(R.id.image);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int res : grantResults) {
            if (res != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, 0);
                return;
            }
        }
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
        if (tcp_connect == null || tcp_connect.getToken() == null) {
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
                                CameraManager manager = ((CameraManager) getSystemService(Context.CAMERA_SERVICE));
                                try {
                                    for (String cameraId : manager.getCameraIdList()) {
                                        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                                        if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                                requestPermissions(new String[] { Manifest.permission.CAMERA }, 0);
                                                return;
                                            }
                                            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                                                @Override
                                                public void onOpened(@NonNull CameraDevice camera) {
                                                    cameraDevice = camera;
                                                    Message msg = new Message();
                                                    msg.arg1 = MyHandler.ON_VIDEO_START;
                                                    mHandler.sendMessage(msg);

                                                }

                                                @Override
                                                public void onDisconnected(@NonNull CameraDevice camera) {

                                                }

                                                @Override
                                                public void onError(@NonNull CameraDevice camera, int error) {

                                                }
                                            }, mHandler);
                                    }
                                }
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
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




