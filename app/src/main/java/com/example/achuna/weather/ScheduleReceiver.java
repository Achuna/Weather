package com.example.achuna.weather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by Achuna on 3/10/2018.
 */

public class ScheduleReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent scheduleAlarms = new Intent(context, AlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, scheduleAlarms, 0);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 1000*60*30, pendingIntent);
    }
}

