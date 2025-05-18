package com.example.blood_pressure.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blood_pressure.R;
import com.example.blood_pressure.adapters.MeasurementAdapter;
import com.example.blood_pressure.model.Measurement;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private FirebaseFirestore db;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialFadeThrough());
        setExitTransition(new MaterialFadeThrough());

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView welcomeText = view.findViewById(R.id.welcomeText);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String email = user.getEmail();
            welcomeText.setText("Welcome, " + email);
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewMeasurements);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Measurement> measurements = new ArrayList<>();
        MeasurementAdapter adapter = new MeasurementAdapter(measurements);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        performComplexQueries(measurements, adapter);

        return view;
    }

    private void performComplexQueries(List<Measurement> measurements, MeasurementAdapter adapter) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        // Query 1: Measurements with systolic > 120
        db.collection("measurements")
                .whereEqualTo("userId", user.getUid())
                .whereGreaterThan("systolic", 120)
                .orderBy("systolic", Query.Direction.DESCENDING)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int startPosition = measurements.size();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Measurement measurement = doc.toObject(Measurement.class);
                        measurements.add(measurement);
                    }
                    adapter.notifyItemRangeInserted(startPosition, queryDocumentSnapshots.size());
                })
                .addOnFailureListener(e -> Log.e("HomeFragment", "Query 1 failed: ", e));

        // Query 2: Measurements with diastolic between 80 and 90
        db.collection("measurements")
                .whereEqualTo("userId", user.getUid())
                .whereGreaterThanOrEqualTo("diastolic", 80)
                .whereLessThanOrEqualTo("diastolic", 90)
                .orderBy("diastolic", Query.Direction.ASCENDING)
                .orderBy("pulse", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int startPosition = measurements.size();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Measurement measurement = doc.toObject(Measurement.class);
                        measurements.add(measurement);
                    }
                    adapter.notifyItemRangeInserted(startPosition, queryDocumentSnapshots.size());
                })
                .addOnFailureListener(e -> Log.e("HomeFragment", "Query 2 failed: ", e));

        // Query 3: Paginated query
        db.collection("measurements")
                .whereEqualTo("userId", user.getUid())
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int startPosition = measurements.size();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Measurement measurement = doc.toObject(Measurement.class);
                        measurements.add(measurement);
                    }
                    adapter.notifyItemRangeInserted(startPosition, queryDocumentSnapshots.size());

                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot lastDoc = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);

                        db.collection("measurements")
                                .whereEqualTo("userId", user.getUid())
                                .orderBy("timestamp", Query.Direction.ASCENDING)
                                .startAfter(lastDoc)
                                .limit(5)
                                .get()
                                .addOnSuccessListener(nextPageSnapshots -> {
                                    int nextStartPosition = measurements.size();
                                    for (DocumentSnapshot nextDoc : nextPageSnapshots.getDocuments()) {
                                        Measurement measurement = nextDoc.toObject(Measurement.class);
                                        measurements.add(measurement);
                                    }
                                    adapter.notifyItemRangeInserted(nextStartPosition, nextPageSnapshots.size());
                                })
                                .addOnFailureListener(e -> Log.e("HomeFragment", "Query 3 (Next Page) failed: ", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("HomeFragment", "Query 3 failed: ", e));
    }
}
