package com.asav.flexis;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;
import static com.asav.flexis.MainActivity.LOG_DEBUG;

public class NotificationHandler {

    Context context;
    Class callingClass;

    NotificationManagerCompat notificationManager;

    public NotificationHandler(Context context, Class aClass) {
        this.context = context;
        callingClass = aClass;
    }

    public void createNotification(View v) {

        String objectiveName = ((TextView) (v.findViewById(R.id.tv_objCard_Name))).getText().toString();
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "name";
            String description = "desc";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(context, callingClass);

        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        //Intent snoozeIntent = new Intent(this, MyBroadcastReceiver.class);
        //snoozeIntent.setAction(ACTION_SNOOZE);
        //snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, 0);

        Gson gson = new Gson();
        String objectiveJSON = gson.toJson(v.getTag());
        Log.d(LOG_DEBUG, "ObjJSON NOTIF HANDLER: " + objectiveJSON);
        Bundle extras = new Bundle();
        Intent endTaskIntent = new Intent(context, MainActivity.class);

        extras.putString("objectiveJSON", objectiveJSON);
        extras.putString("endTask", "EndTask");
        extras.putInt("notifUniqueId", uniqueInt);
        endTaskIntent.putExtras(extras);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, uniqueInt, endTaskIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //add actions for ending, adding 5 min, adding 10 min
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "1")
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle(objectiveName)
                .setContentText("This task's assigned duration has completed...")
                //.setStyle(new NotificationCompat.BigTextStyle()
                    //.bigText("Much longer text that cannot fit one line..."))
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.string.end_task, "End Task", pendingIntent)
                .addAction(R.string.add_5_minutes, "+5 min", pendingIntent)
                .addAction(R.string.add_10_minutes, "+10 min", pendingIntent);

        notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(uniqueInt, mBuilder.build());
    }

    public void cancelNotification(int id) {

        notificationManager.cancel(id);

    }

}
