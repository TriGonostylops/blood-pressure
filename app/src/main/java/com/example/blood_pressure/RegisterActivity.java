package com.example.blood_pressure;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

public class RegisterActivity extends AppCompatActivity {
    EditText userEmailEditText;
    EditText passwordEditText;

    EditText confirmPasswordEditText;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        userEmailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
    }

    public void register(View view) {
        Intent intent = new Intent(this, LoginActivity.class);

        String email = userEmailEditText.getText() != null ? userEmailEditText.getText().toString().trim() : "";
        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";
        String password2 = confirmPasswordEditText.getText() != null ? confirmPasswordEditText.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty() || password2.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill in all fields.", Toast.LENGTH_LONG).show();
            return;
        }
        if(!password.equals(password2)){
            Toast.makeText(RegisterActivity.this, "The passwords don't match!",Toast.LENGTH_LONG).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(RegisterActivity.this, "Succesful registration",Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    finish();
                } else {
                    String errorMessage = "Registration failed";

                    if (task.getException() != null) {
                        String exceptionMessage = task.getException().getMessage();

                        if (exceptionMessage != null) {
                            if (exceptionMessage.contains("email address is badly formatted")) {
                                errorMessage = "Invalid email format.";
                            } else if (exceptionMessage.contains("password is invalid") || exceptionMessage.contains("Password should be at least")) {
                                errorMessage = "Password must be at least 6 characters.";
                            } else if (exceptionMessage.contains("The email address is already in use")) {
                                errorMessage = "Email is already registered.";
                            } else {
                                errorMessage = exceptionMessage;
                            }
                        }
                    }

                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void cancel(View view) {
        finish();
    }
}