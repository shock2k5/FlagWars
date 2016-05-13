package com.example.kevin.flagwars;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.Collection;

public class RegisterActivity extends AppCompatActivity {

    protected Button mFacebookButton, mLoginButton;
    protected EditText mEmailEditText, mPasswordEditText, mConfirmEditText;
    protected TextView mLoginTextView;
    protected ImageView mProfilePicture;
    Firebase ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFacebookButton = (Button) findViewById(R.id.facebookLoginBT);
        mLoginButton = (Button) findViewById(R.id.loginBT);
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);
        mConfirmEditText = (EditText) findViewById(R.id.confirmPasswordEditText);
        mLoginTextView = (TextView) findViewById(R.id.loginText);
        mProfilePicture = (ImageView) findViewById(R.id.profilePictureImageView);

        mLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                i.putExtra("gameMode", getIntent().getStringExtra("gameMode"));
                startActivity(i);
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailEditText.getText().toString().trim();
                String password = mPasswordEditText.getText().toString().trim();
                String confirmPassword = mConfirmEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setMessage((email.isEmpty()) ? "Email is empty" : "Password is empty")
                            .setTitle("Login Error").setPositiveButton(android.R.string.ok, null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else if (!isEmailValid(email)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setMessage("Invalid email address")
                            .setTitle("Login Error").setPositiveButton(android.R.string.ok, null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else if (!password.equals(confirmPassword)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setMessage("Password mismatch")
                            .setTitle("Login Error").setPositiveButton(android.R.string.ok, null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    setProgressBarIndeterminateVisibility(true);

                }
            }
        });

        mFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collection<String> permissions = new ArrayList<>();
                permissions.add("public_profile");
                permissions.add("email");
            }
        });

        mProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Image coming soon", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean isEmailValid(String email) {
        boolean at = false, dot = false;

        for (int i = 0; i < email.length(); i++) {
            if (email.charAt(i) == '@')
                at = true;
            else if (email.charAt(i) == '.')
                dot = true;
        }

        return at && dot;
    }

}
