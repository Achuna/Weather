package com.example.achuna.weather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import java.util.Calendar;


/**
 * Created by Achuna on 3/10/2018.
 *
 * Repeatedly Sets the alarm for each show
 */

public class AlarmService extends Service {
    private boolean isRunning;
    private Context context;
    private Thread backgroundThread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.context = this;
        this.isRunning = false;
        this.backgroundThread = new Thread(myTask);
    }

    private Runnable myTask = new Runnable() {
        public void run() {


            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

            Calendar now = Calendar.getInstance();
            Calendar alarm = Calendar.getInstance();

            alarm.set(Calendar.HOUR_OF_DAY,6);
            alarm.set(Calendar.MINUTE, 0);
            alarm.set(Calendar.SECOND, 0);

            Intent setNotification = new Intent(getApplicationContext(), WeatherService.class);
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, setNotification, PendingIntent.FLAG_UPDATE_CURRENT);

            long diff = now.getTimeInMillis() - alarm.getTimeInMillis();
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

            if(diff < 0) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), pendingIntent);
            } else {
                alarm.set(Calendar.HOUR_OF_DAY, 10);
                alarm.set(Calendar.MINUTE, 0);
                alarm.set(Calendar.SECOND, 0);
                diff = now.getTimeInMillis() - alarm.getTimeInMillis();
                if(diff < 0) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), pendingIntent);
                } else {
                    alarm.set(Calendar.HOUR_OF_DAY, 13);
                    alarm.set(Calendar.MINUTE, 30);
                    alarm.set(Calendar.SECOND, 0);
                    diff = now.getTimeInMillis() - alarm.getTimeInMillis();
                    if (diff < 0) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), pendingIntent);
                    } else {
                        alarm.set(Calendar.HOUR_OF_DAY, 16);
                        alarm.set(Calendar.MINUTE, 0);
                        alarm.set(Calendar.SECOND, 0);
                        diff = now.getTimeInMillis() - alarm.getTimeInMillis();
                        if(diff < 0) {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), pendingIntent);
                        } else {
                            alarm.set(Calendar.HOUR_OF_DAY, 18);
                            alarm.set(Calendar.MINUTE, 0);
                            alarm.set(Calendar.SECOND, 0);
                            diff = now.getTimeInMillis() - alarm.getTimeInMillis();
                            if(diff < 0) {
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), pendingIntent);
                            } else {
                                alarm.set(Calendar.HOUR_OF_DAY, 20); //20
                                alarm.set(Calendar.MINUTE, 0);
                                alarm.set(Calendar.SECOND, 0);
                                diff = now.getTimeInMillis() - alarm.getTimeInMillis();
                                if (diff < 0) {
                                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), pendingIntent);
                                }
                            }

                        }
                    }
                }
            }

            stopSelf();
        }
    };

    @Override
    public void onDestroy() {
        this.isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!this.isRunning) {
            this.isRunning = true;
            this.backgroundThread.start();
        }
        return START_STICKY;
    }

}

