package com.example.dickman.myapplication.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by aeon on 2018/3/29.
 */

public class PhoneBroadCastListener extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        /*Intent broadcastIntent = new Intent(context, OnPhoneCallActivity.class);
        broadcastIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle b = intent.getExtras();
        broadcastIntent.putExtras(b);
        context.startActivity(broadcastIntent);*/
    }

}
