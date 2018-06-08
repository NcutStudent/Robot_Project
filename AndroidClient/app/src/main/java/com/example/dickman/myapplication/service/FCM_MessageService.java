package com.example.dickman.myapplication.service;

import android.content.Intent;

import com.example.dickman.myapplication.OnPhoneCallActivity;
import com.example.dickman.myapplication.broadcast.PhoneBroadCastListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCM_MessageService extends FirebaseMessagingService {
    private PhoneBroadCastListener phoneBroadCastListener = new PhoneBroadCastListener();

    public FCM_MessageService() {
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            Intent intent = new Intent (this, OnPhoneCallActivity.class);
            startActivity(intent);
        }
    }
}
