package com.example.dickman.myapplication.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.dickman.myapplication.OnPhoneCallActivity;

/**
 * Created by aeon on 2018/3/29.
 */

public class PhoneBroadCastListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent broadcastIntent = new Intent(context, OnPhoneCallActivity.class);
        broadcastIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(broadcastIntent);
    }
}
