package com.asav.flexis;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class BroadcastService extends Service {

    private final static String TAG = "BroadcastService";

    public static final String TASKTIMER_BR = "com.asav.flexis.tasktimer_br";
    Intent bi = new Intent(TASKTIMER_BR);

    //CountDownTimer cdt = null;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Starting timer...");


    }

    @Override
    public void onDestroy() {

        //cdt.cancel();
        Log.i(TAG, "Timer cancelled");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
