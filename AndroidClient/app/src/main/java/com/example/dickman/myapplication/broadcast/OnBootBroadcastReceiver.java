package com.example.dickman.myapplication.broadcast;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.example.dickman.myapplication.MainActivity;
import com.example.dickman.myapplication.R;
import com.example.dickman.myapplication.service.PhoneAnswerListener;

/**
 * Created by HatsuneMiku on 2018/3/28.
 */

public class OnBootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, PhoneAnswerListener.class);
        context.startService(serviceIntent);
    }
}
