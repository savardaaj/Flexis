package com.asav.flexis

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.google.gson.Gson

import android.app.Notification.EXTRA_NOTIFICATION_ID
import com.asav.flexis.MainActivity.Companion.LOG_DEBUG

class NotificationHandler(internal var context: Context, internal var callingClass: Class<*>) {

    internal var notificationManager: NotificationManagerCompat? = null

    fun createNotification(v: View) {

        val objectiveName = (v.findViewById<View>(R.id.tv_objCard_Name) as TextView).text.toString()
        val uniqueInt = (System.currentTimeMillis() and 0xfffffff).toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val description = "desc"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("1", name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, callingClass)

        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        //Intent snoozeIntent = new Intent(this, MyBroadcastReceiver.class);
        //snoozeIntent.setAction(ACTION_SNOOZE);
        //snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, 0);

        val gson = Gson()
        val objectiveJSON = gson.toJson(v.tag)
        Log.d(LOG_DEBUG, "ObjJSON NOTIF HANDLER: $objectiveJSON")
        val extras = Bundle()
        val endTaskIntent = Intent(context, MainActivity::class.java)

        extras.putString("objectiveJSON", objectiveJSON)
        extras.putString("endTask", "EndTask")
        extras.putInt("notifUniqueId", uniqueInt)
        endTaskIntent.putExtras(extras)

        val pendingIntent = PendingIntent.getActivity(context, uniqueInt, endTaskIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //add actions for ending, adding 5 min, adding 10 min
        val mBuilder = NotificationCompat.Builder(context, "1")
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
                .addAction(R.string.add_10_minutes, "+10 min", pendingIntent)

        notificationManager = NotificationManagerCompat.from(context)

        // notificationId is a unique int for each notification that you must define
        notificationManager!!.notify(uniqueInt, mBuilder.build())
    }

    fun cancelNotification(id: Int) {

        if (notificationManager != null) {
            notificationManager!!.cancel(id)
        } else {
            Toast.makeText(context, "Unable to cancel dismiss notification", Toast.LENGTH_SHORT).show()
        }
    }

}