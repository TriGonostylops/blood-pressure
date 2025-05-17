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

import com.example.blood_pressure.R;
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

                    Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
                    grouped.put("Today", new ArrayList<>());
                    grouped.put("This Week", new ArrayList<>());
                    grouped.put("This Month", new ArrayList<>());
                    grouped.put("Older", new ArrayList<>());

                    Calendar now = Calendar.getInstance();
                    long currentTime = System.currentTimeMillis();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> m = doc.getData();
                        Log.d("MeasurementFragment", "Doc: " + doc.getId() + " => " + m);

                        if (m == null) continue;

                        Object tsObj = m.get("timestamp");
                        long ts;
                        if (tsObj instanceof Long) {
                            ts = (Long) tsObj;
                        } else if (tsObj instanceof Double) {
                            ts = ((Double) tsObj).longValue();
                        } else {
                            Log.w("MeasurementFragment", "Invalid timestamp in doc: " + doc.getId());
                            continue;
                        }

                        Log.d("MeasurementFragment", "Parsed timestamp (ms): " + ts);

                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(ts);

                        if (isSameDay(cal, now)) {
                            grouped.get("Today").add(m);
                            Log.d("MeasurementFragment", "Added to Today");
                        } else if (isWithinDays(ts, currentTime, 7)) {
                            grouped.get("This Week").add(m);
                            Log.d("MeasurementFragment", "Added to This Week");
                        } else if (isWithinDays(ts, currentTime, 30)) {
                            grouped.get("This Month").add(m);
                            Log.d("MeasurementFragment", "Added to This Month");
                        } else {
                            grouped.get("Older").add(m);
                            Log.d("MeasurementFragment", "Added to Older");
                        }
                    }

                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    groupContainer.removeAllViews();

                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

                    for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
                        if (entry.getValue().isEmpty()) continue;

                        Log.d("MeasurementFragment", "Rendering group: " + entry.getKey() + " with " + entry.getValue().size() + " items.");

                        TextView title = new TextView(getContext());
                        title.setText(entry.getKey());
                        title.setTextAppearance(android.R.style.TextAppearance_Medium);
                        title.setPadding(0, 48, 0, 16);
                        groupContainer.addView(title);

                        for (Map<String, Object> m : entry.getValue()) {
                            View card = inflater.inflate(R.layout.item_measurement_card, groupContainer, false);

                            String systolic = m.get("systolic") != null ? String.valueOf(m.get("systolic")) : "-";
                            String diastolic = m.get("diastolic") != null ? String.valueOf(m.get("diastolic")) : "-";
                            String pulse = m.get("pulse") != null ? String.valueOf(m.get("pulse")) : "-";
                            String note = m.get("note") != null ? m.get("note").toString() : "";

                            Object tsObj = m.get("timestamp");
                            long ts;
                            if (tsObj instanceof Long) {
                                ts = (Long) tsObj;
                            } else if (tsObj instanceof Double) {
                                ts = ((Double) tsObj).longValue();
                            } else {
                                ts = 0;
                            }
                            String formattedDate = ts > 0 ? sdf.format(new Date(ts)) : "Unknown";

                            ((TextView) card.findViewById(R.id.textViewValues))
                                    .setText("BP: " + systolic + "/" + diastolic + ", Pulse: " + pulse);

                            ((TextView) card.findViewById(R.id.textViewNote)).setText(note);
                            ((TextView) card.findViewById(R.id.textViewTimestamp)).setText(formattedDate);

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
