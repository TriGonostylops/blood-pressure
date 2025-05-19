package com.example.blood_pressure.service;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MeasurementService {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface Callback {
        void onSuccess();

        void onFailure(Exception e);
    }

    public void loadMeasurement(String measurementId, OnLoadCallback callback) {
        db.collection("measurements").document(measurementId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.onSuccess(doc);
                    } else {
                        callback.onFailure(new Exception("Measurement not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void saveMeasurement(int systolic, int diastolic, int pulse, String note, FirebaseUser user, Callback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("systolic", systolic);
        data.put("diastolic", diastolic);
        data.put("pulse", pulse);
        data.put("note", note);
        data.put("userId", user.getUid());
        data.put("timestamp", Timestamp.now());

        db.collection("measurements")
                .add(data)
                .addOnSuccessListener(doc -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public void updateMeasurement(String id, int systolic, int diastolic, int pulse, String note, FirebaseUser user, Timestamp timestamp, Callback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("systolic", systolic);
        data.put("diastolic", diastolic);
        data.put("pulse", pulse);
        data.put("note", note);
        data.put("userId", user.getUid());
        data.put("timestamp", timestamp != null ? timestamp : Timestamp.now());

        db.collection("measurements")
                .document(id)
                .update(data)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public interface OnLoadCallback {
        void onSuccess(DocumentSnapshot doc);

        void onFailure(Exception e);
    }
}
