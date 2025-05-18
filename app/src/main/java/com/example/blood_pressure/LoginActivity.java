package com.example.blood_pressure;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Context;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    EditText userEmailEditText;
    EditText passwordEditText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        userEmailEditText = findViewById(R.id.editTextEmailAddress);
        passwordEditText = findViewById(R.id.passwordEditText);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
    }
    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        boolean keepLoggedIn = preferences.getBoolean("KeepLoggedIn", false);

        if (keepLoggedIn && FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    public void register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void login(View view) {
        String email = userEmailEditText.getText() != null ? userEmailEditText.getText().toString().trim() : "";
        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please enter both email and password.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, MainActivity.class);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Successful authentication",Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Wrong email or password",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}