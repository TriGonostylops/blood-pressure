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
import androidx.navigation.Navigation;

import com.example.blood_pressure.R;
import com.example.blood_pressure.service.MeasurementService;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddMeasurementFragment extends Fragment {

    private EditText editTextSystolic, editTextDiastolic, editTextPulse, editTextNote;
    private Button buttonAdd;
    private MeasurementService measurementService;

    private String measurementId = null;
    private Timestamp originalTimestamp = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialFadeThrough());
        setExitTransition(new MaterialFadeThrough());

        measurementService = new MeasurementService();

        if (getArguments() != null) {
            measurementId = getArguments().getString("measurementId");
        }
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

        if (measurementId != null) {
            buttonAdd.setText("Update Measurement");
            loadMeasurementForEdit();
        }

        buttonAdd.setOnClickListener(v -> saveOrUpdateMeasurement());

        return view;
    }

    private void loadMeasurementForEdit() {
        measurementService.loadMeasurement(measurementId, new MeasurementService.OnLoadCallback() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                populateFields(doc);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populateFields(DocumentSnapshot doc) {
        Number systolic = doc.getLong("systolic");
        Number diastolic = doc.getLong("diastolic");
        Number pulse = doc.getLong("pulse");
        String note = doc.getString("note");
        originalTimestamp = doc.getTimestamp("timestamp");

        if (systolic != null) editTextSystolic.setText(String.valueOf(systolic.intValue()));
        if (diastolic != null) editTextDiastolic.setText(String.valueOf(diastolic.intValue()));
        if (pulse != null) editTextPulse.setText(String.valueOf(pulse.intValue()));
        if (note != null) editTextNote.setText(note);
    }

    private void saveOrUpdateMeasurement() {
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
            Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        MeasurementService.Callback callback = new MeasurementService.Callback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(),
                        measurementId == null ? "Measurement added" : "Measurement updated",
                        Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigate(R.id.measurementsFragment);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        if (measurementId == null) {
            measurementService.saveMeasurement(systolic, diastolic, pulse, note, currentUser, callback);
        } else {
            measurementService.updateMeasurement(measurementId, systolic, diastolic, pulse, note, currentUser, originalTimestamp, callback);
        }
    }
}
