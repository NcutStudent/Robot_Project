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
    //服務Binder需實作出來的項目
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            PhoneAnswerListener.LocalBinder binder = (PhoneAnswerListener.LocalBinder) service;//取得service
            phoneAnswerListener = binder.getService();//設定聯繫服務
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_phone_call);
        /*在鎖屏時也能夠喚醒Activity，直接顯示出來
                    FLAG_TURN_SCREEN_ON（點亮畫面）
                    FLAG_SHOW_WHEN_LOCKED（鎖屏時也能顯示）*/
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        player = MediaPlayer.create(this, R.raw.out);//設定來電音效
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);//設定聲音音量

        player.setLooping(true);//不間斷播放
        player.start();//音樂給他放下去
        Intent intent = new Intent(this, PhoneAnswerListener.class);//電話背景服務
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);//綁定聯繫服務
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);//反綁定
        super.onDestroy();
    }

    //按下接聽通話
    public void answerCall(View view) {
        Intent intent = new Intent(this,MainActivity.class);//準備主要介面
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//一樣是藉由一個非正常管道開啟Activity，故需在Intent設置一個Intent.FLAG_ACTIVITY_NEW_TASK標記
        intent.putExtra("startCommunication", true);//將所需的資料放入Intent
        player.stop();//停止來電音效
        phoneAnswerListener.answerPhoneCall(true);//將接電話的訊號傳給phoneAnswerListener.answerPhoneCall
        startActivity(intent);//開啟主畫面，並將資料帶過去
    }

    //按下拒絕通話
    public void hangUp(View view) {
        player.stop();//停止來電音效
        phoneAnswerListener.answerPhoneCall(false);//將掛電話的訊號傳給phoneAnswerListener.answerPhoneCall
        finish();
    }
}
