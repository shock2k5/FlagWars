package com.example.kevin.flagwars;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseSession;
import com.parse.ParseUser;

public class ChooseGameModeActivity extends AppCompatActivity {

    protected Button mCreateGameButton, mJoinGameButton;
    protected ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_game_mode);

        mCreateGameButton = (Button) findViewById(R.id.createGameBT);
        mJoinGameButton = (Button) findViewById(R.id.joinGameBT);

        currentUser = ParseUser.getCurrentUser();
        /*ParseSession.getCurrentSessionInBackground(new GetCallback<ParseSession>() {
            @Override
            public void done(ParseSession object, ParseException e) {
                if (e == null) {
                    ParseUser.becomeInBackground(String.valueOf(object), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (user != null) {

                            }
                        }
                    });
                }
            }
        });*/

        mCreateGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser == null) {
                    // Don't have current user
                    Intent i = new Intent(ChooseGameModeActivity.this, LoginActivity.class);
                    i.putExtra("gameMode", "createGame");
                    startActivity(i);
                } else {
                    // Current user is logged in
                    Intent i = new Intent(ChooseGameModeActivity.this, CreateGameActivity.class);
                    startActivity(i);
                }
            }
        });

        mJoinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser == null) {
                    // Don't have current user
                    Intent i = new Intent(ChooseGameModeActivity.this, LoginActivity.class);
                    i.putExtra("gameMode", "joinGame");
                    startActivity(i);
                } else {
                    // Current user is logged in
                    Intent i = new Intent(ChooseGameModeActivity.this, JoinGameActivity.class);
                    startActivity(i);
                }
            }
        });
    }
}
