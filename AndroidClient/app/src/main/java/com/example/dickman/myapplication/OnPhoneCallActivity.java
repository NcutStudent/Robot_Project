package com.example.dickman.myapplication;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.dickman.myapplication.service.PhoneAnswerListener;

import static com.example.dickman.myapplication.Util.TEMP_FILE;
import static com.example.dickman.myapplication.Util.USER_ICON_PATH;

public class OnPhoneCallActivity extends AppCompatActivity {
    MediaPlayer player;

    PhoneAnswerListener phoneAnswerListener;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            PhoneAnswerListener.LocalBinder binder = (PhoneAnswerListener.LocalBinder) service;
            phoneAnswerListener = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    BroadcastReceiver reciveCallStatues = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            hangUp(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_phone_call);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        player = MediaPlayer.create(this, R.raw.out);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setLooping(true);

        Intent intent = new Intent(this, PhoneAnswerListener.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        IntentFilter broadCastIntentFitter = new IntentFilter();
        broadCastIntentFitter.addAction(getString(R.string.hang_up));
        registerReceiver(reciveCallStatues, broadCastIntentFitter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String path = getSharedPreferences(TEMP_FILE, MODE_PRIVATE).getString(USER_ICON_PATH, null);
        Bitmap bmp = BitmapFactory.decodeFile(path);
        if(bmp != null) {
            ((ImageView)findViewById(R.id.icon)).setImageBitmap(bmp);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.stop();
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        unregisterReceiver(reciveCallStatues);
        super.onDestroy();
    }

    public void answerCall(View view) {
        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("startCommunication", true);
        phoneAnswerListener.answerPhoneCall(true);
        startActivity(intent);
    }

    public void hangUp(View view) {
        phoneAnswerListener.answerPhoneCall(false);

        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("startCommunication", false);
        startActivity(intent);
    }
}
