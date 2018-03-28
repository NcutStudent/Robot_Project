package com.example.dickman.myapplication.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.dickman.myapplication.MainActivity;
import com.example.dickman.myapplication.R;
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
        } else if(intent.getAction().equals(context.getString(R.string.on_phone_call))){
            Intent broadcastIntent = new Intent(context, MainActivity.class);
            broadcastIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(broadcastIntent);
            //TODO
        }
    }
}
