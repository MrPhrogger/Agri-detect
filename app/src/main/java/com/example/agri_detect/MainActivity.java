package com.example.agri_detect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnLogin = findViewById(R.id.btnGoToLogin);
        Button btnSignup = findViewById(R.id.btnGoToSignup);

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        btnSignup.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
        });
    }
}