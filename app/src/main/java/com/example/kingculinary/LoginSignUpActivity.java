package com.example.kingculinary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoginSignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sign_up);

        Button buttonToSignUp = findViewById(R.id.buttonToSignUp);
        TextView buttonToLogin = findViewById(R.id.buttonToLogin);

        buttonToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginSignUpActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        buttonToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginSignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}