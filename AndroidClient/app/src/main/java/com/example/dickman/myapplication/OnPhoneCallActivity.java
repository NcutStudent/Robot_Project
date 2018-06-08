package com.example.dickman.myapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.example.dickman.myapplication.service.PhoneAnswerListener;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_phone_call);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        player = MediaPlayer.create(this, R.raw.out);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setLooping(true);
        player.start();
        Intent intent = new Intent(this, PhoneAnswerListener.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }

    public void answerCall(View view) {
        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("startCommunication", true);
        player.stop();
        phoneAnswerListener.answerPhoneCall(true);
        startActivity(intent);
    }

    public void hangUp(View view) {
        player.stop();
        phoneAnswerListener.answerPhoneCall(false);
        finish();
    }
}
