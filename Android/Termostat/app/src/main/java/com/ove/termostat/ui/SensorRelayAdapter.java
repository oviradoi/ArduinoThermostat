package com.ove.termostat.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.ove.termostat.R;
import com.ove.termostat.model.SensorRelay;

import java.util.Locale;

class SensorRelayAdapter extends ArrayAdapter<SensorRelay> {

    private static class ViewHolder {
        private TextView tvName;
        private TextView tvCurrentTemp;
        private TextView tvTargetTemp;
        private TextView tvHysteresis;
    }

    SensorRelayAdapter(Context context, int resource, SensorRelay[] objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.sensorrelay_listitem, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            viewHolder.tvCurrentTemp = (TextView) convertView.findViewById(R.id.tvCurrentTemp);
            viewHolder.tvTargetTemp = (TextView) convertView.findViewById(R.id.tvTargetTemp);
            viewHolder.tvHysteresis = (TextView) convertView.findViewById(R.id.tvHyst);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        SensorRelay sr = getItem(position);
        if (sr != null) {
            viewHolder.tvName.setText(sr.getName());
            viewHolder.tvCurrentTemp.setText(String.format(Locale.getDefault(), "%.1f\u00B0C", sr.getCurrentTemp()));
            viewHolder.tvTargetTemp.setText(String.format(Locale.getDefault(), "%d\u00B0C", sr.getTargetTemp()));
            viewHolder.tvHysteresis.setText(String.format(Locale.getDefault(), "%d\u00B0C", sr.getHysteresis()));
        }

        return convertView;
    }
}
