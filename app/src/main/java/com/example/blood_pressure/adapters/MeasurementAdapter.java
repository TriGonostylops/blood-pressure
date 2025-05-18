package com.example.blood_pressure.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blood_pressure.R;
import com.example.blood_pressure.model.Measurement;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MeasurementAdapter extends RecyclerView.Adapter<MeasurementAdapter.ViewHolder> {

    private final List<Measurement> measurements;

    public MeasurementAdapter(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_measurement_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Measurement measurement = measurements.get(position);
        holder.textViewValues.setText("BP: " + measurement.getSystolic() + "/" + measurement.getDiastolic() +
                ", Pulse: " + measurement.getPulse());
        holder.textViewNote.setText(measurement.getNote() != null ? measurement.getNote() : "No note");
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        holder.textViewTimestamp.setText(sdf.format(measurement.getTimestamp().toDate()));
    }

    @Override
    public int getItemCount() {
        return measurements.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewValues, textViewNote, textViewTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewValues = itemView.findViewById(R.id.textViewValues);
            textViewNote = itemView.findViewById(R.id.textViewNote);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
        }
    }
}