package com.example.dickman.myapplication.service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FCM_IDService extends FirebaseInstanceIdService {

    public FCM_IDService() {

    }

    public void onTokenRefresh(){
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("zzz：","refreshedToken："+refreshedToken);
    }

}
