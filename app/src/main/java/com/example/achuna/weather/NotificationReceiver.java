package com.example.achuna.weather;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.renderscript.RenderScript;
import android.support.v4.app.NotificationCompat;

import com.android.volley.Request;

/**
 * Created by Achuna on 3/10/2018.
 */

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

//        String condition = intent.getExtras().getString("condition");
//        String temp = intent.getExtras().getString("temp");
//        String summary = intent.getExtras().getString("summary");
//
//
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//
//        Intent startMain = new Intent(context, MainActivity.class);
//        PendingIntent openIntent = PendingIntent.getActivity(context, 1, startMain, PendingIntent.FLAG_UPDATE_CURRENT);
//
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
//
//        builder.setContentTitle(temp + " · " + condition)
//                .setContentText(summary)
//                .setSmallIcon(R.drawable.notification_icon)
//                .setContentIntent(openIntent)
//                .setPriority(Notification.PRIORITY_DEFAULT)
//                .setAutoCancel(true)
//                .setLights(Color.CYAN, 2000, 2000);
//
//        notificationManager.notify(0, builder.build());

    }
}
