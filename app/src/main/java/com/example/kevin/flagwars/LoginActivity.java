package com.example.kevin.flagwars;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;
import java.util.Collection;

public class LoginActivity extends AppCompatActivity {

    protected Button mFacebookButton, mLoginButton;
    protected EditText mEmailEditText, mPasswordEditText;
    protected TextView mRegisterTextView;
    protected String gameMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        gameMode = getIntent().getStringExtra("gameMode");
        mFacebookButton = (Button) findViewById(R.id.facebookLoginBT);
        mLoginButton = (Button) findViewById(R.id.loginBT);
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);
        mRegisterTextView = (TextView) findViewById(R.id.registerText);

        Firebase.setAndroidContext(this.getApplicationContext());
        final Firebase ref = new Firebase("https://flagwar.firebaseio.com/");

        mRegisterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            i.putExtra("gameMode", gameMode);
            startActivity(i);
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailEditText.getText().toString().trim();
                String password = mPasswordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage((email.isEmpty()) ? "Email is empty" : "Password is empty")
                            .setTitle("Login Error").setPositiveButton(android.R.string.ok, null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    setProgressBarIndeterminateVisibility(true);
                    setProgressBarIndeterminateVisibility(false);
                    ref.authWithPassword(email, password, new Firebase.AuthResultHandler() {
                        @Override
                        public void onAuthenticated(AuthData authData) {
                            Intent i;
                            Intent previousIntent = getIntent();
                            if (previousIntent.getStringExtra("gameMode").equals("createGame")){
                                i = new Intent(LoginActivity.this, CreateGameActivity.class);
                            } else if (previousIntent.getStringExtra("gameMode").equals("joinGame")) {
                                i = new Intent(LoginActivity.this, JoinGameActivity.class);
                            } else {
                                i = new Intent(LoginActivity.this, ChooseGameModeActivity.class);
                            }
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        }

                        @Override
                        public void onAuthenticationError(FirebaseError firebaseError) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setMessage(firebaseError.getMessage())
                                    .setTitle("Login Error").setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });
                }
            }
        });

        mFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collection<String> permissions = new ArrayList<String>();
                permissions.add("public_profile");
                permissions.add("email");
                Toast.makeText(LoginActivity.this, "Facebook integration coming soon", Toast.LENGTH_LONG);
            }
        });
    }
}
