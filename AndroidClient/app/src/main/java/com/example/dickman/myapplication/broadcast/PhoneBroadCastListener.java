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
        Intent broadcastIntent = new Intent(context, OnPhoneCallActivity.class);//準備接收訊號
        broadcastIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//因為要從一個非正式的管道開啟Activity，例如：service、BroadcastReceiver等，所以得在Intent設置一個Intent.FLAG_ACTIVITY_NEW_TASK標記
        context.startActivity(broadcastIntent);//起動OnPhoneCallActivity，已接收電話來電訊號
    }
}
