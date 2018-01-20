package com.example.achuna.weather;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends ArrayAdapter<String> {

    Context context;
    int[] icons;
    String[] temperature;
    String[] time;
    boolean isNight;

    public CustomAdapter(@NonNull Context context, int[] icons, String[] temperature, String[] time, boolean isNight) {
        super(context, R.layout.forecast_list_item);
        this.isNight = isNight;
        this.icons = icons;
        this.temperature = temperature;
        this.time = time;
        this.context = context;
    }

    @Override
    public int getCount() {
        return time.length;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        convertView = layoutInflater.inflate(R.layout.forecast_list_item, parent, false);

        viewHolder.img = (ImageView) convertView.findViewById(R.id.listImage);
        viewHolder.tempText = (TextView) convertView.findViewById(R.id.tempList);
        viewHolder.timeText = (TextView) convertView.findViewById(R.id.timeList);

        viewHolder.img.setImageResource(icons[position]);
        viewHolder.tempText.setText(temperature[position]);
        viewHolder.timeText.setText(time[position]);

        if (isNight) {
            viewHolder.timeText.setTextColor(Color.WHITE);
            viewHolder.tempText.setTextColor(Color.WHITE);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView img;
        TextView tempText;
        TextView timeText;
    }
}