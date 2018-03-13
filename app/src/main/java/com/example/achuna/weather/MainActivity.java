package com.example.achuna.weather;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.text.StringSearch;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    TextView date, temp, condition, coordinates;
    ImageView weatherImage;
    RelativeLayout layout;
    ListView forecast;
    Spinner forecast_options;
    Toolbar toolbar;
    boolean isNight = false;
    int tempConverter = 0;
    int tempUnits = 0; // 0 for F and 1 for C
    String expand = "\t\t\t\t(Tap for Details)";
    int adapterChoice = 0; //0 for daily and 1 for hourly

    AlarmManager alarmManager;

    CustomAdapter dailyWeatherAdapter;
    CustomAdapter hourlyWeatherAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("Achuna's Weather App");
        setSupportActionBar(toolbar);

        SharedPreferences units = getSharedPreferences("unit pref", MODE_PRIVATE);
        tempUnits = units.getInt("tempUnits", 0);

        //Initialized Views
        date = findViewById(R.id.date);
        temp = findViewById(R.id.temp);
        weatherImage = findViewById(R.id.weatherIcon);
        condition = findViewById(R.id.condition);
        coordinates = findViewById(R.id.coordinates);
        layout = findViewById(R.id.background_screen);
        forecast_options = findViewById(R.id.forecast_options);
        forecast = findViewById(R.id.forecast);

        //Gets the time and formats it below the title
        Calendar calander = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");

        final String currentDate = DateFormat.getDateInstance().format(calander.getTime());
        String time = simpleDateFormat.format(calander.getTime());


        //Converts time to int and changes style based on time of day
        int timeNumber = calander.get(Calendar.HOUR_OF_DAY);

        if ((((timeNumber >= 19 && timeNumber <= 23)) || ((timeNumber >= 0 && timeNumber <= 5)))) {
            layout.setBackground(getResources().getDrawable(R.drawable.night_background));
            date.setTextColor(Color.WHITE);
            temp.setTextColor(Color.WHITE);
            condition.setTextColor(Color.WHITE);
            coordinates.setTextColor(Color.WHITE);
            toolbar.setBackgroundColor(Color.GRAY);
            isNight = true;
        }  else if ((timeNumber >= 6 && timeNumber < 12)) {
            layout.setBackground(getResources().getDrawable(R.drawable.sunny_blue_background));
            date.setTextColor(Color.BLACK);
            temp.setTextColor(Color.BLACK);
            condition.setTextColor(Color.BLACK);
            coordinates.setTextColor(Color.BLACK);
            toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar));
            isNight = false;
        } else if((timeNumber >= 12 && timeNumber < 19)) {
            layout.setBackground(getResources().getDrawable(R.drawable.evening_background));
            date.setTextColor(Color.BLACK);
            temp.setTextColor(Color.BLACK);
            condition.setTextColor(Color.BLACK);
            coordinates.setTextColor(Color.BLACK);
            toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar));
            isNight = false;
        }

        //Spinner Creation
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.forecast_options, android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        forecast_options.setAdapter(spinnerAdapter);




        /////Gathering Information/////////

        //GPS Coordinates
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //https://api.darksky.net/forecast/8db497397f239659c523cff7a529e763/-76.8348263,39.16867348
        String url = "https://api.darksky.net/forecast/8db497397f239659c523cff7a529e763/";

        @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        Log.i("Achuna", Double.toString(longitude));
        Log.i("Achuna", Double.toString(latitude));


        //Get address base on location
        try{
            Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
            if (addresses.isEmpty()) {
                coordinates.setText("Your Location");
            }
            else {
                coordinates.setText(addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //Appends coordinates to dark sky url string
        //This will update data based on the phones location

        url = url + Double.toString(latitude) + ","+Double.toString(longitude);
        Log.i("Achuna", url);




        ////////////////////////DAILY WEATHER/////////////////////////////




        //Orders the forecast for the next 8 days
        String dayStart = "";
        int dayInCalander = calander.get(Calendar.DAY_OF_WEEK);
        switch (dayInCalander) {
            case Calendar.SUNDAY:
                dayStart = "Sunday"; break;
            case Calendar.MONDAY:
                dayStart = "Monday"; break;
            case Calendar.TUESDAY:
                dayStart = "Tuesday"; break;
            case Calendar.WEDNESDAY:
                dayStart = "Wednesday"; break;
            case Calendar.THURSDAY:
                dayStart = "Thursday"; break;
            case Calendar.FRIDAY:
                dayStart = "Friday"; break;
            case Calendar.SATURDAY:
                dayStart = "Saturday"; break;
        }

        String[] allDays = {"Sunday","Monday","Tuesday","Wednesday","Thursday", "Friday", "Saturday"
                ,"Sunday","Monday","Tuesday","Wednesday","Thursday", "Friday", "Saturday"};

        String[] days = new String[8];

        int beginIndex = 0;
        for (int i = 0; i<allDays.length; i++) {
            if (dayStart.equalsIgnoreCase(allDays[i])) {
                beginIndex = i;
                for (int j = 0; j<days.length; j++) {
                    days[j] = allDays[beginIndex + j] + expand;
                }
                break;
            }
        }
        date.setText("Last updated: "+ time + "\n" + dayStart + ", " +currentDate);

        final int[] weather_condition_list = new int[8];
        final String[] dailyTemps = new String[8];
        final String[] dailySummaries = new String[8];

        RequestQueue dailyWeatherRequest = Volley.newRequestQueue(getApplicationContext());

        JsonObjectRequest weatherInfo = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("Achuna", response.toString());
                try {
                    JSONObject daily = response.getJSONObject("daily");
                    JSONArray dailyData = daily.getJSONArray("data");
                    for (int i = 0; i < 8; i++) {
                        JSONObject summaryItem = dailyData.getJSONObject(i);
                        String daySummary = summaryItem.getString("summary").toLowerCase();
                        weather_condition_list[i] = summaryToImage(daySummary, false);



                        dailyTemps[i] = tempConverter(summaryItem.getString("temperatureHigh"), tempConverter, tempUnits);

                        if(!summaryItem.getString("precipProbability").equals("0")) {
                            double precipProbability = Double.parseDouble(summaryItem.getString("precipProbability"));
                            precipProbability = precipProbability * 100;
                            String precipChance = "Precipitation: " + precipProbability + "%";
                            String precipType = "Precipitation Type: "+summaryItem.getString("precipType");
                            String windSpeed = "Wind Speed: " + summaryItem.getString("windSpeed") + " m/s";
                            double humid = Double.parseDouble(summaryItem.getString("humidity"));
                            humid = humid * 100;
                            String humidity = "Humidity: " + humid+"%";
                            dailySummaries[i] = daySummary + "\n\n" + precipChance + "\n" + precipType + "\n" + windSpeed + "\n" + humidity;
                        } else {
                            String windSpeed = "Wind Speed: " + summaryItem.getString("windSpeed") + " m/s";
                            double humid = Double.parseDouble(summaryItem.getString("humidity"));
                            humid = humid * 100;
                            String humidity = "Humidity: " + humid+"%";
                            dailySummaries[i] = daySummary + "\n\nPrecipitation: 0%" + "\n" + windSpeed + "\n" + humidity;
                        }



                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error fetching forecast data", Toast.LENGTH_SHORT).show();
            }
        });

        dailyWeatherRequest.add(weatherInfo);

        //Forecast Adapters Creation
        dailyWeatherAdapter = new CustomAdapter(getApplicationContext(), weather_condition_list, dailyTemps, days, isNight);




        ////////////////////////////////HOURLY FORECAST/////////////////////////////////////////


        final int[] hourly_condition_icons = new int[8];
        final String[] hours = new String[8];
        final String[] hourTemps = new String[8];
        final String[] hourSummaries = new String[8];

        RequestQueue hourlyDataRequest = Volley.newRequestQueue(getApplication());

        JsonObjectRequest hourlyData = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject hourData = response.getJSONObject("hourly");
                    JSONArray hourForecastItems = hourData.getJSONArray("data");

                    for (int i = 0; i < 8; i++) {
                        JSONObject hourInfoItems = hourForecastItems.getJSONObject(i);

                        String epochTime = hourInfoItems.getString("time");
                        int time = Integer.parseInt(epochTime);
                        Log.i("hourly", epochTime);

                        //Converts epoch time to standard readable time that is easy to understand by the user
                        Date date = new Date(time * 1000L);
                        DateFormat format = new SimpleDateFormat("hh:mm a");
                        format.setTimeZone(TimeZone.getDefault());
                        String formattedTime = format.format(date);

                        hours[i] = formattedTime + expand;
                        Log.i("hourly", formattedTime);
                        String hourSummary = hourInfoItems.getString("summary");
                        hourly_condition_icons[i] = summaryToImage(hourSummary.toLowerCase(), false);

                        hourTemps[i] = tempConverter(hourInfoItems.getString("temperature"), tempConverter, tempUnits);

                        if(!hourInfoItems.getString("precipProbability").equals("0")) {
                            double precipProbability = Double.parseDouble(hourInfoItems.getString("precipProbability"));
                            precipProbability = precipProbability * 100;
                            String precipChance = "Precipitation: " + precipProbability + "%";
                            String precipType = "Precipitation Type: "+hourInfoItems.getString("precipType");
                            String windSpeed = "Wind Speed: " + hourInfoItems.getString("windSpeed") + " m/s";
                            double humid = Double.parseDouble(hourInfoItems.getString("humidity"));
                            humid = humid * 100;
                            String humidity = "Humidity: " + humid+"%";
                            hourSummaries[i] = hourSummary + "\n\n" + precipChance + "\n" + precipType + "\n" + windSpeed + "\n" + humidity;
                        } else {
                            String windSpeed = "Wind Speed: " + hourInfoItems.getString("windSpeed") + " m/s";
                            double humid = Double.parseDouble(hourInfoItems.getString("humidity"));
                            humid = humid * 100;
                            String humidity = "Humidity: " + humid+"%";
                            hourSummaries[i] = hourSummary + "\n\nPrecipitation: 0%" + "\n" + windSpeed + "\n" + humidity;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error fetching hourly data", Toast.LENGTH_SHORT).show();
            }
        });


        hourlyDataRequest.add(hourlyData);


        hourlyWeatherAdapter = new CustomAdapter(getApplicationContext(), hourly_condition_icons, hourTemps, hours, isNight);

        //Based on spinner value, set the forecast adapter to match

        forecast_options.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                if (selectedItem.toLowerCase().contains("daily")) {
                    forecast.setAdapter(dailyWeatherAdapter);
                    adapterChoice = 0;
                } else if(selectedItem.toLowerCase().contains("hour")) {
                    forecast.setAdapter(hourlyWeatherAdapter);
                    adapterChoice = 1;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Nothing happens
            }
        });



        ///////////////////////////CLICK EVENTS FOR LISTVIEW//////////////////////////////////



        forecast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (adapterChoice == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setIcon(weather_condition_list[i]);
                    builder.setTitle("Summary");
                    builder.setMessage(dailySummaries[i]);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.setNegativeButton("MORE INFO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Opens Chrome
                            String urlString="https://www.google.com/search?q=weather&rlz=1C1CHBF_enUS775US775&oq=weather&aqs=chrome..69i57j69i61j69i60l2j69i61j69i59.1048j0j7&sourceid=chrome&ie=UTF-8";
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setPackage("com.android.chrome");
                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException ex) {
                                // Chrome browser presumably not installed so allow user to choose instead
                                intent.setPackage(null);
                                startActivity(intent);
                            }
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setIcon(hourly_condition_icons[i]);
                    builder.setTitle("Summary");
                    builder.setMessage(hourSummaries[i]);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.setNegativeButton("MORE INFO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Opens Chrome
                            String urlString="https://www.google.com/search?q=weather&rlz=1C1CHBF_enUS775US775&oq=weather&aqs=chrome..69i57j69i61j69i60l2j69i61j69i59.1048j0j7&sourceid=chrome&ie=UTF-8";
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setPackage("com.android.chrome");
                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException ex) {
                                // Chrome browser presumably not installed so allow user to choose instead
                                intent.setPackage(null);
                                startActivity(intent);
                            }
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

            }
        });




        /////////////////////////CURRENT WEATHER/////////////////////////////






        //Getting json data and displaying it on screen
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse(JSONObject response) {
                Log.i("Achuna", response.toString());
                try {
                    JSONObject currentData = response.getJSONObject("currently");
                    condition.setText(currentData.getString("summary"));
                    temp.setText(tempConverter(currentData.getString("temperature"), tempConverter, tempUnits));
                    //temp.setText(currentData.getString("temperature") + " °F");

                    setAlarm(condition.getText().toString(), temp.getText().toString());

                    String summary = currentData.getString("summary").toLowerCase().trim();
                    Log.i("Achuna", summary);
                    //Image changes based on summary

                    weatherImage.setImageResource(summaryToImage(summary, true));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error Fetching Data\nPlease Check Connection", Toast.LENGTH_LONG).show();
                Log.e("Achuna", error.toString());
            }
        });

        queue.add(jsonObjectRequest);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.weather_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.refresh:
                recreate();
                return true;
            case R.id.google_weather:
                //Opens Chrome
                String urlString="https://www.google.com/search?q=weather&rlz=1C1CHBF_enUS775US775&oq=weather&aqs=chrome..69i57j69i61j69i60l2j69i61j69i59.1048j0j7&sourceid=chrome&ie=UTF-8";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setPackage("com.android.chrome");
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    // Chrome browser presumably not installed so allow user to choose instead
                    intent.setPackage(null);
                    startActivity(intent);
                }
                return true;
            case R.id.unit_change:
                tempUnits = (tempUnits == 0) ? 1 : 0;
                tempConverter = (tempConverter == 1) ? 0 : 1;
                //Save temperature unit preference
                SharedPreferences unitPreferences = getSharedPreferences("unit pref", MODE_PRIVATE);
                SharedPreferences.Editor editor = unitPreferences.edit();
                editor.putInt("tempUnits", tempUnits);
                editor.apply();
                recreate();
                return true;
            case R.id.exit:
                finish();
                System.exit(0);
            default:
                return super.onOptionsItemSelected(item);
        }

    }



    /////////////////////////////////METHODS///////////////////////////////////


    public void setAlarm(String condition, String temp) {
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar now = Calendar.getInstance();
        Calendar alarm = Calendar.getInstance();


        alarm.set(Calendar.HOUR_OF_DAY, 7);
        alarm.set(Calendar.MINUTE, 0);
        alarm.set(Calendar.SECOND, 0);


        Intent setNotification = new Intent(getApplicationContext(), AlertReciever.class);
        setNotification.putExtra("condition", condition);
        setNotification.putExtra("temp", temp);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, setNotification, PendingIntent.FLAG_UPDATE_CURRENT);

        long diff = now.getTimeInMillis() - alarm.getTimeInMillis();
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), 1000 * 60 * 60 * 3, pendingIntent);
        if(diff > 0) {
            alarm.add(Calendar.HOUR_OF_DAY, 2);
            //alarmManager.cancel(pendingIntent);
        } else {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), 1000 * 60 * 60 * 3, pendingIntent);
        }



    }


    /**
     * Changes the weather icon to match the given summary (uses keywords)
     * @param summary
     * @param isCurrent (checks if it is night mode or not and changes the layout accordingly)
     * @return
     */
    public int summaryToImage(String summary, boolean isCurrent) {
        int image = R.drawable.app_icon;
        if (isCurrent) {
            if (summary.contains("sun") || summary.contains("clear")) {
                if(isNight) {
                    image = R.drawable.moon_clear;
                } else {
                    image = R.drawable.sunny;
                }
            } else if (summary.contains("partly cloudy")) {
                if(isNight) {
                    image = R.drawable.night_clouds;
                } else {
                    image = R.drawable.partly_cloudy;
                }

            } else if (summary.contains("rain") || summary.contains("shower") || summary.contains("drizzle") || summary.contains("mixed")) {
                image = R.drawable.showers;
                layout.setBackground(getResources().getDrawable(R.drawable.gloomy_background));
                date.setTextColor(Color.BLACK);
                temp.setTextColor(Color.BLACK);
                condition.setTextColor(Color.BLACK);
                coordinates.setTextColor(Color.BLACK);
                toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar));
            } else if (summary.contains("thunder")) {
                image = R.drawable.thunder;
                layout.setBackground(getResources().getDrawable(R.drawable.gloomy_background));
                date.setTextColor(Color.BLACK);
                temp.setTextColor(Color.BLACK);
                condition.setTextColor(Color.BLACK);
                coordinates.setTextColor(Color.BLACK);
                toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar));
            } else if (summary.contains("snow") || summary.contains("flur")) {
                image = R.drawable.snow;
            } else if (summary.contains("cloud") || summary.contains("overcast")) {
                image = R.drawable.cloudy;
            } else if(summary.contains("wind") || summary.contains("fog") || summary.contains("breez")) {
                image = R.drawable.windy;
            }

            //Add more conditions here

            else  {
                image = R.drawable.app_icon;
            }
        } else {
            if (summary.contains("sun") || summary.contains("clear")) {
                image = R.drawable.sunny;
            } else if (summary.contains("partly cloudy")) {
                image = R.drawable.partly_cloudy;
            } else if (summary.contains("rain") || summary.contains("shower") || summary.contains("drizzle")) {
                image = R.drawable.showers;
            } else if (summary.contains("thunder")) {
                image = R.drawable.thunder;
            } else if (summary.contains("snow") || summary.contains("flur")) {
                image = R.drawable.snow;
            } else if (summary.contains("cloud") || summary.contains("overcast")) {
                image = R.drawable.cloudy;
            } else if(summary.contains("wind")|| summary.contains("fog") || summary.contains("breez")) {
                image = R.drawable.windy;
            }

            //Add more conditions here

            else  {
                image = R.drawable.app_icon;
            }
        }
        return image;
    }

    /**
     * Converts from F to C or visa versa
     * @param temp inputs temperature
     * @param conversionType 0 to convert to F and 1 to convert to C
     * @param unit tells whether or not unit is F or C
     * @return String value of new temperature unit
     *
     */
    public String tempConverter(String temp, int conversionType, int unit) {
        double temperature = Double.parseDouble(temp);
        String newTemp = "";
        if (conversionType == 0 && unit == 1) {
            double tempConvert =  (.555555556)*(temperature - 32.0) ;
            newTemp = Math.round(tempConvert) + "" + "°C";
        } else if(conversionType == 1 && unit == 0) {
            double tempConvert = ((temperature * 1.8) + 32);
            newTemp =  Math.round(tempConvert) + "" + "°F";
        } else {
            if (unit == 0) {
                newTemp = temp+"°F";
            } else {
                newTemp = temp + "°C";
            }
        }
        return newTemp;
    }
}