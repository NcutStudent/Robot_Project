package com.example.dickman.myapplication.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.dickman.myapplication.service.PhoneAnswerListener;

/**
 * Created by HatsuneMiku on 2018/3/28.
 */

public class ApplicationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent serviceIntent = new Intent(context, PhoneAnswerListener.class);
            context.startService(serviceIntent);
        }
    }
}
