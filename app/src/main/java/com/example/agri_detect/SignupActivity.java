package com.example.agri_detect;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etSignupEmail);
        etPassword = findViewById(R.id.etSignupPassword);
        Button btnSignup = findViewById(R.id.btnEmailSignup);

        btnSignup.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Account Created!", Toast.LENGTH_SHORT).show();
                            // Go straight to dashboard after signup
                            startActivity(new android.content.Intent(SignupActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}