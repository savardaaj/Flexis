package com.asav.flexis;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import static android.content.ContentValues.TAG;

public class TaskTimer {

    boolean isRunning = false;

    final MainActivity mainActivity;

    long elapsedTime;
    TextView cardAction;
    Context context;
    Chronometer cmCardTimer;
    boolean isFinished;

    String desiredTime;
    long desiredTimeInMilliSeconds;

    public TaskTimer(final MainActivity mainAct, final View v, int duration) {

        this.mainActivity = mainAct;
        calculateDuration(duration);

        cmCardTimer = v.findViewById(R.id.cm_timer);
        cardAction = v.findViewById(R.id.tv_card_action);

        cmCardTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            long hours = 0, minutes = 0, seconds = 0;
            public void onChronometerTick(Chronometer arg0) {

                if (!isRunning) {
                    seconds = ((SystemClock.elapsedRealtime() - cmCardTimer.getBase())/1000) % 60;
                    minutes = ((SystemClock.elapsedRealtime() - cmCardTimer.getBase())/1000) / 60;
                    hours = minutes / 60;
                    elapsedTime = SystemClock.elapsedRealtime();
                } else {
                    seconds = ((elapsedTime - cmCardTimer.getBase())/1000) % 60;
                    minutes = ((elapsedTime - cmCardTimer.getBase())/1000) / 60;
                    hours = minutes / 60;
                    elapsedTime = elapsedTime + 1000;
                }
                String output = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                cmCardTimer.setText(output);
                if(output.equals(desiredTime)) {
                    //call push notification
                    mainActivity.onClickCreateNotification(v);
                    cardAction.setText("Finish");
                }

            }
        });
    }

    public void startTimer() {

        isRunning = true;

        cmCardTimer.setBase(0);
        cmCardTimer.start();

        cardAction.setText("Pause");

    }

    public void resumeTimer() {
        isRunning = true;
        cmCardTimer.start();
        cardAction.setText("Pause");
    }

    public void pauseTimer() {
        isRunning = false;
        cmCardTimer.stop();
        cardAction.setText("Resume");
    }

    public void calculateDuration(int duration) {

        int desiredHours;
        int desiredMinutes;
        int desiredSeconds;
        desiredHours = duration / 60;
        desiredMinutes = duration % 60;
        desiredSeconds = 0;

        this.desiredTime = String.format("%02d:%02d:%02d", desiredHours, desiredMinutes, desiredSeconds);
        this.desiredTimeInMilliSeconds = (desiredSeconds + (desiredMinutes * 60) + (desiredHours * 3600)) * 1000;
    }
}
