package com.example.blood_pressure.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.blood_pressure.LoginActivity;
import com.example.blood_pressure.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private SwitchMaterial switchKeepLoggedIn;
    private Button buttonLogout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        switchKeepLoggedIn = view.findViewById(R.id.switchKeepLoggedIn); // Use SwitchMaterial
        buttonLogout = view.findViewById(R.id.buttonLogout);

        SharedPreferences preferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        boolean keepLoggedIn = preferences.getBoolean("KeepLoggedIn", false);
        switchKeepLoggedIn.setChecked(keepLoggedIn);

        switchKeepLoggedIn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("KeepLoggedIn", isChecked).apply();
            Toast.makeText(getContext(), "Preference updated", Toast.LENGTH_SHORT).show();
        });

        buttonLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            preferences.edit().putBoolean("KeepLoggedIn", false).apply();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }
}