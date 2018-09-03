package com.example.achuna.weather;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by Achuna on 3/12/2018.
 */

public class WeatherService extends Service {

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

            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            //https://api.darksky.net/forecast/8db497397f239659c523cff7a529e763/-76.8348263,39.16867348
            String url = "https://api.darksky.net/forecast/8db497397f239659c523cff7a529e763/";

            @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            Log.i("WeatherService", Double.toString(longitude));
            Log.i("WeatherService", Double.toString(latitude));


            //Appends coordinates to dark sky url string
            //This will update data based on the phones location

            url = url + Double.toString(latitude) + ","+Double.toString(longitude);

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject currentData = response.getJSONObject("currently");
                        String summary, temp;
                        temp = currentData.getString("temperature");
                        summary = currentData.getString("summary").toLowerCase().trim();
                        JSONObject daily = response.getJSONObject("daily");
                        JSONArray dailyData = daily.getJSONArray("data");
                        JSONObject summaryItem = dailyData.getJSONObject(0);
                        String daySummary = summaryItem.getString("summary").toLowerCase();


                        Log.i("WeatherService", temp + "  :  " + summary + "  :   " + daySummary);


                        Intent notify = new Intent(context, NotificationReceiver.class);
                        notify.putExtra("summary", daySummary);
                        notify.putExtra("condition", summary);
                        notify.putExtra("temp", temp + "°F");

                        context.sendBroadcast(notify);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Toast.makeText(getApplicationContext(), "Error Fetching Data\nPlease Check Connection", Toast.LENGTH_LONG).show();
                    Log.e("Achuna", error.toString());
                }
            });

            queue.add(jsonObjectRequest);


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
