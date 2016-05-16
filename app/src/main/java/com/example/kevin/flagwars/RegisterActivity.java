package com.example.kevin.flagwars;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import java.util.ArrayList;
import java.util.Collection;

public class RegisterActivity extends AppCompatActivity {

    protected Button mFacebookButton, mLoginButton;
    protected EditText mEmailEditText, mPasswordEditText, mConfirmEditText;
    protected TextView mLoginTextView;
    protected ImageView mProfilePicture;
    Firebase fireRef;
    String email, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Firebase.setAndroidContext(this.getApplicationContext());
        fireRef = ImportantMethods.getFireBase();

        mFacebookButton = (Button) findViewById(R.id.facebookLoginBT);
        mLoginButton = (Button) findViewById(R.id.loginBT);
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);
        mConfirmEditText = (EditText) findViewById(R.id.confirmPasswordEditText);
        mLoginTextView = (TextView) findViewById(R.id.loginText);
        mProfilePicture = (ImageView) findViewById(R.id.profilePictureImageView);

        mFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RegisterActivity.this, "Facebook integration not currently supported.", Toast.LENGTH_LONG);
            }
        });

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
                email = mEmailEditText.getText().toString().trim();
                password = mPasswordEditText.getText().toString().trim();
                confirmPassword = mConfirmEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setMessage((email.isEmpty()) ? "Email is empty" : "Password is empty")
                            .setTitle("Login Error").setPositiveButton(android.R.string.ok, null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

                fireRef.createUser(email, password, new Firebase.ResultHandler() {
                    @Override
                    public void onSuccess() {
                        fireRef.authWithPassword(email, password, new Firebase.AuthResultHandler() {
                            @Override
                            public void onAuthenticated(AuthData authData) {
                                ImportantMethods.addNewUser(new User(email));
                                CurrentUser.setCurrentUser(RegisterActivity.this.getApplicationContext());
                                String mode = getIntent().getStringExtra("gameMode");
                                Intent intent;
                                if(mode.equals("createGame")){
                                    intent = new Intent(RegisterActivity.this, CreateGameActivity.class);
                                } else{
                                    intent = new Intent(RegisterActivity.this, JoinGameActivity.class);
                                }
                                startActivity(intent);
                            }

                            @Override
                            public void onAuthenticationError(FirebaseError firebaseError) {
                                Toast.makeText(getApplicationContext(), "Email / Password combination not valid", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        Toast.makeText(getApplicationContext(), "Email / Password combination not valid", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });
    }

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