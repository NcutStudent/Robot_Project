package com.example.dickman.myapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dickman.myapplication.network.TCP_Connect;
import com.example.dickman.myapplication.service.PhoneAnswerListener;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.DatagramSocket;

public class MainActivity extends AppCompatActivity {
    final String PhoneKey = "Phone";
    final String RaspberryKey = "Raspberry";

    final String PhoneVideoKey = "VideoPhone";
    final String RaspberryVideoKey = "VideoRaspberry";

    public static final int serverPort = 7777;
    public static final int serverUdpPort = 8888;
    public static final int timeout = 0;
    public static final String serverHost = "140.128.88.166";

    private EditText passEdit = null;
    private SurfaceView surfaceView;
    final Object audioLock = new Object();
    Audio audio = null;
    VideoThread video = null;
    CameraDevice cameraDevice = null;
    TCP_Connect tcp_connect;
    static PhoneAnswerListener.LocalBinder binder;
    boolean surfaceIsCreated = false;

    //將socket跟token等資料藉由Java 序列化從物件轉變成資料流，達到簡易傳遞資料的效果

    private class PacketClass implements Serializable {
        public DatagramSocket socket;
        public String cientHost, token;
        public int SentPort;
    }

    //利用Handler將傳給主線程的資料儲存

    static class MyHandler extends Handler {
        static final int ON_AUDIO_START = 0;
        static final int ON_VIDEO_START = 1;
        static final int ON_IMAGE_AVAILABLE = 2;
        private WeakReference<MainActivity> mOuter;

        MyHandler(MainActivity activity) {
            mOuter = new WeakReference<>(activity);
        }

        /*將主線程需要的資料利用handleMessage作為傳輸管道
                    因為Thread不能刷新主線程，必須得用HandMessage做為傳輸的管道*/

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
                        //啟動後呼叫音訊的傳輸功能，Audio(音訊資料, 手機端IP位址, 手機端傳輸port,outer.timeout, packetClass.token);
                        outer.audio = new Audio(packetClass.socket, packetClass.cientHost, packetClass.SentPort,
                                outer.timeout, packetClass.token);
                        break;
                    case ON_VIDEO_START:
                        //啟動後呼叫視訊的傳輸功能，VideoThread(tcp_connect的連接設定，cameraDevice啟動相機功能，surfaceView.getHolder().getSurface()將接收到的資料畫面渲染到surfaceView上)
                        outer.video = new VideoThread(outer.tcp_connect, outer.cameraDevice, outer.surfaceView.getHolder().getSurface(), 640, 480);
                        break;
                    case ON_IMAGE_AVAILABLE:
                        break;
                }
            }
        }
    }

    BroadcastReceiver phoneListener = new BroadcastReceiver() {//實作出背景服務的手機來電監聽
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(getString(R.string.miss_connection))){
                clickcall_end(null);//關閉通話
            } else if (intent.getAction().equals(getString(R.string.answer_call))) {
                //new Thread(new StartCommuication(binder)).start();
            }
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {//實作通話背景服務

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            binder = (PhoneAnswerListener.LocalBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private MyHandler mHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        passEdit      = findViewById(R.id.editText);
        surfaceView  = findViewById(R.id.image);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                surfaceIsCreated = true;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                int a = 0;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                surfaceIsCreated = false;
                synchronized (surfaceView) {
                    surfaceView.notifyAll();
                }
                surfaceIsCreated = true;
            }
        });

        IntentFilter broadCastIntentFitter = new IntentFilter();
        broadCastIntentFitter.addAction(getString(R.string.miss_connection));//Intent filter內會設定的資料包括action,data與category三種。也就是說filter只會與intent裡的這三種資料做比對動作，所以我們新增一個自定義的action，其代表錯過電話
        broadCastIntentFitter.addAction(getString(R.string.answer_call));//Intent filter內會設定的資料包括action,data與category三種。也就是說filter只會與intent裡的這三種資料做比對動作，所以我們新增一個自定義的action，其代表來電
        registerReceiver(phoneListener, broadCastIntentFitter);

        Intent intent = new Intent(this, PhoneAnswerListener.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);//綁定服務
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 0);
        } else {
            boolean startCommunication = getIntent().getBooleanExtra("startCommunication", false);
            if(startCommunication) {
                new Thread(new StartCommuication(binder)).start();
            } else {
                getIntent().putExtra("startCommunication", false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        unregisterReceiver(phoneListener);
        super.onDestroy();
    }

    //檢查並確認權限
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

    //關閉通話
    public void clickcall_end(View view) {
        synchronized (audioLock) {
            if (audio != null || video != null) {
                audio.close();
                audio = null;
                video.stopRunning();
                video = null;
                Toast.makeText(this, "Communication stop", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //設定TCP等資料，PacketClass(TCP連結，連結從哪，連結到哪)
    public PacketClass getSetting(TCP_Connect tcp_connect, String from, String to) {
        PacketClass packetClass = new PacketClass();//建構PacketClass
        if (tcp_connect == null || tcp_connect.getToken() == null) {//當連結或權杖都為null時回傳null，表示沒有任何對象或是找不到對象(?)[我不太清楚這裡的意思]
            //簡單來說就是 如果初始化失敗就不要動作
            return null;
        }
        packetClass.socket = tcp_connect.getUdpSocket(from);
        while (packetClass.socket == null) {//得到UDP的socket才繼續做，否則一直等
            packetClass.socket = tcp_connect.getUdpSocket(from);
        }
        String tmp[] = tcp_connect.getSocketIpPort(to).split(" ");//將得到的IP跟PORT切割，因為進來的資料長OO.OO.OO.OO XXX，中間空白切掉，並放入原先準備好的陣列內
        packetClass.cientHost = tmp[0];//切過以後第一格為IP
        packetClass.SentPort = Integer.valueOf(tmp[1]);//第二格為port
        while (packetClass.cientHost.equals("0.0.0.0") || packetClass.SentPort == 0) {//確認Host跟port是否存在，若沒有拿到就繼續做，直到拿到為止
            tmp = tcp_connect.getSocketIpPort(to).split(" ");
            packetClass.cientHost = tmp[0];
            packetClass.SentPort = Integer.valueOf(tmp[1]);
            try {
                Thread.sleep(60);//等待時間，也許會有時間差，不一定是沒有對象
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        packetClass.token = tcp_connect.getToken();
        return packetClass;
    }

    public void clickcall_start(View view) {
        PhoneAnswerListener phoneAnswerListener = binder.getService();
        if(!phoneAnswerListener.isInit()){
            Toast.makeText(this, "wait for program init", Toast.LENGTH_SHORT).show();
        } else if(phoneAnswerListener.isPasswordError()) {
            new Thread(new InitService(passEdit.getText().toString(), binder)).start();
        } else {
            phoneAnswerListener.makeACall();
            Toast.makeText(this, "calling", Toast.LENGTH_SHORT).show();
        }
    }

    class InitService implements Runnable{
        String password;
        PhoneAnswerListener.LocalBinder binder;

        InitService(String password, PhoneAnswerListener.LocalBinder binder) {
            this.password = password;
            this.binder = binder;
        }

        @Override
        public void run() {
            PhoneAnswerListener phoneAnswerListener = binder.getService();
            if(password != null) {
                phoneAnswerListener.restartListening(password);
            }
            while(phoneAnswerListener.isInit());
            if(!phoneAnswerListener.isPasswordError()) {
                getSharedPreferences("settings", MODE_PRIVATE).edit()
                        .putString("password", password)
                        .apply();
            }
        }
    }

    class StartCommuication implements Runnable{
        PhoneAnswerListener.LocalBinder binder;
        StartCommuication(PhoneAnswerListener.LocalBinder binder) {
            this.binder = binder;
        }

        @Override
        public void run() {
            synchronized (audioLock) {//啟動通訊，並利用synchronized限制只會啟動一個，避免出現錯誤
                if (audio == null && video == null) {
                    PhoneAnswerListener phoneAnswerListener = binder.getService();
                    while(!phoneAnswerListener.isInit());
                    boolean isPasswordError = phoneAnswerListener.isPasswordError();
                    tcp_connect = phoneAnswerListener.getTCP_Client();

                    if (!isPasswordError) {
                        PacketClass packetClass = getSetting(tcp_connect, PhoneKey, RaspberryKey);//設定tcp跟手機和TX2的金鑰
                        Message msg = new Message();//將資料傳給headle
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("audio", packetClass);
                        msg.arg1 = MyHandler.ON_AUDIO_START;//啟動通話
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);


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

                                            while (!surfaceIsCreated);
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "password error or network is unavailable", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        }
    }
}




