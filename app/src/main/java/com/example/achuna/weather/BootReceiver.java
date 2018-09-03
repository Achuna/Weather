package com.example.achuna.weather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import java.util.Calendar;
import static android.content.Context.ALARM_SERVICE;


/**
 * Created by Achuna on 3/4/2018.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

//
//
//        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
//
//            Intent startAlarmService = new Intent(context, AlarmService.class);
//            context.startService(startAlarmService);
//            Log.i("AService", "Started Alarm Service From Device Boot--------------------------------------------");
//
//            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
//            Intent scheduleAlarms = new Intent(context, AlarmService.class);
//            PendingIntent pendingIntent = PendingIntent.getService(context, 0, scheduleAlarms, 0);
//            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 1000*60*30, pendingIntent);
//
//
//            }




    }
}
