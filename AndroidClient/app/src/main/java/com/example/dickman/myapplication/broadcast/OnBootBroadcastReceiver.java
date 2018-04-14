package com.example.dickman.myapplication.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.dickman.myapplication.service.PhoneAnswerListener;

/**
 * Created by HatsuneMiku on 2018/3/28.
 */

public class OnBootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, PhoneAnswerListener.class);//準備手機服務的監聽程式
        context.startService(serviceIntent);//起動PhoneAnswerListener這個服務
    }
}
