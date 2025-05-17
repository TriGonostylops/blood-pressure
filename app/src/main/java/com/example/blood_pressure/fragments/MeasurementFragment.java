package com.example.blood_pressure.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.blood_pressure.R;
import com.example.blood_pressure.model.Measurement;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MeasurementFragment extends Fragment {

    private LinearLayout groupContainer;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_measurement, container, false);
        groupContainer = view.findViewById(R.id.groupContainer);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            loadMeasurements();
        }

        return view;
    }

    private static class MeasurementWithId {
        String id;
        Measurement measurement;

        MeasurementWithId(String id, Measurement measurement) {
            this.id = id;
            this.measurement = measurement;
        }
    }

    private void loadMeasurements() {
        db.collection("measurements")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    Log.d("MeasurementFragment", "Query returned " + count + " documents.");

                    if (count == 0) {
                        Log.d("MeasurementFragment", "No documents found for user.");
                    }

                    Map<String, List<MeasurementWithId>> grouped = new LinkedHashMap<>();
                    grouped.put("Today", new ArrayList<>());
                    grouped.put("This Week", new ArrayList<>());
                    grouped.put("This Month", new ArrayList<>());
                    grouped.put("Older", new ArrayList<>());

                    Calendar now = Calendar.getInstance();
                    long currentTime = System.currentTimeMillis();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Measurement measurement = doc.toObject(Measurement.class);
                        if (measurement == null || measurement.getTimestamp() == null) continue;

                        measurement.setDocumentId(doc.getId());

                        long ts = measurement.getTimestamp().toDate().getTime();

                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(ts);

                        MeasurementWithId mwid = new MeasurementWithId(doc.getId(), measurement);

                        if (isSameDay(cal, now)) {
                            grouped.get("Today").add(mwid);
                            Log.d("MeasurementFragment", "Added to Today");
                        } else if (isWithinDays(ts, currentTime, 7)) {
                            grouped.get("This Week").add(mwid);
                            Log.d("MeasurementFragment", "Added to This Week");
                        } else if (isWithinDays(ts, currentTime, 30)) {
                            grouped.get("This Month").add(mwid);
                            Log.d("MeasurementFragment", "Added to This Month");
                        } else {
                            grouped.get("Older").add(mwid);
                            Log.d("MeasurementFragment", "Added to Older");
                        }
                    }

                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    groupContainer.removeAllViews();

                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

                    for (Map.Entry<String, List<MeasurementWithId>> entry : grouped.entrySet()) {
                        if (entry.getValue().isEmpty()) continue;

                        Log.d("MeasurementFragment", "Rendering group: " + entry.getKey() + " with " + entry.getValue().size() + " items.");

                        TextView title = new TextView(getContext());
                        title.setText(entry.getKey());
                        title.setTextAppearance(android.R.style.TextAppearance_Medium);
                        title.setPadding(0, 48, 0, 16);
                        groupContainer.addView(title);

                        for (MeasurementWithId mwid : entry.getValue()) {
                            View card = inflater.inflate(R.layout.item_measurement_card, groupContainer, false);

                            // Set document ID as tag for update/delete operations
                            card.setTag(mwid.id);

                            Measurement m = mwid.measurement;

                            String systolic = String.valueOf(m.getSystolic());
                            String diastolic = String.valueOf(m.getDiastolic());
                            String pulse = String.valueOf(m.getPulse());
                            String note = m.getNote() != null ? m.getNote() : "";

                            String formattedDate = m.getTimestamp() != null
                                    ? sdf.format(m.getTimestamp().toDate())
                                    : "Unknown";

                            ((TextView) card.findViewById(R.id.textViewValues))
                                    .setText("BP: " + systolic + "/" + diastolic + ", Pulse: " + pulse);

                            ((TextView) card.findViewById(R.id.textViewNote)).setText(note);
                            ((TextView) card.findViewById(R.id.textViewTimestamp)).setText(formattedDate);

                            card.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
                                new android.app.AlertDialog.Builder(requireContext())
                                        .setTitle("Confirm Deletion")
                                        .setMessage("Are you sure you want to delete this measurement?")
                                        .setPositiveButton("Delete", (dialog, which) -> {
                                            db.collection("measurements")
                                                    .document(mwid.id)
                                                    .delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d("MeasurementFragment", "Deleted measurement: " + mwid.id);
                                                        groupContainer.removeView(card);
                                                    })
                                                    .addOnFailureListener(e -> Log.e("MeasurementFragment", "Error deleting document", e));
                                        })
                                        .setNegativeButton("Cancel", null)
                                        .show();
                            });
                            card.findViewById(R.id.buttonEdit).setOnClickListener(v -> {
                                Bundle bundle = new Bundle();
                                bundle.putString("measurementId", mwid.id);
                                NavHostFragment.findNavController(MeasurementFragment.this)
                                        .navigate(R.id.addMeasurementFragment, bundle);
                            });

                            groupContainer.addView(card);

                            Log.d("MeasurementFragment", "Rendered card with values: " + systolic + "/" + diastolic + " Pulse: " + pulse);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("MeasurementFragment", "Firestore query failed: ", e));
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isWithinDays(long ts, long now, int days) {
        return now - ts <= days * 24L * 60 * 60 * 1000;
    }
}
