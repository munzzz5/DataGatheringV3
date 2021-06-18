package com.example.datagatheringv3.BroadReceieve;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class transitionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BROADCAST RECEIVER","RECEIVED!");

    }
}
