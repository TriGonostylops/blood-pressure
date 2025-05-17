package com.example.blood_pressure.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.blood_pressure.R;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddMeasurementFragment extends Fragment {

    private EditText editTextSystolic, editTextDiastolic, editTextPulse, editTextNote;
    private Button buttonAdd;

    private FirebaseFirestore db;

    public AddMeasurementFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialFadeThrough());
        setExitTransition(new MaterialFadeThrough());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_measurement, container, false);

        editTextSystolic = view.findViewById(R.id.editTextSystolic);
        editTextDiastolic = view.findViewById(R.id.editTextDiastolic);
        editTextPulse = view.findViewById(R.id.editTextPulse);
        editTextNote = view.findViewById(R.id.editTextNote);
        buttonAdd = view.findViewById(R.id.buttonAddMeasurement);

        db = FirebaseFirestore.getInstance();

        buttonAdd.setOnClickListener(v -> addMeasurement());

        return view;
    }

    private void addMeasurement() {
        String systolicStr = editTextSystolic.getText().toString().trim();
        String diastolicStr = editTextDiastolic.getText().toString().trim();
        String pulseStr = editTextPulse.getText().toString().trim();
        String note = editTextNote.getText() != null ? editTextNote.getText().toString().trim() : "";

        if (TextUtils.isEmpty(systolicStr) || TextUtils.isEmpty(diastolicStr) || TextUtils.isEmpty(pulseStr)) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int systolic, diastolic, pulse;

        try {
            systolic = Integer.parseInt(systolicStr);
            diastolic = Integer.parseInt(diastolicStr);
            pulse = Integer.parseInt(pulseStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numeric values", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();

        Map<String, Object> measurement = new HashMap<>();
        measurement.put("systolic", systolic);
        measurement.put("diastolic", diastolic);
        measurement.put("pulse", pulse);
        measurement.put("note", note);
        measurement.put("timestamp", timestamp);
        measurement.put("userId", currentUser.getUid());

        db.collection("measurements")
                .add(measurement)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(getContext(), "Measurement added", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to add: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
