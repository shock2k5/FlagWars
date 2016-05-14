package com.example.kevin.flagwars;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.location.LocationCallback;

import java.util.ArrayList;

public class CreateGameActivity extends AppCompatActivity {
    protected EditText gameName; // radio0 is Red, radio1 is Blue
    protected Button createGameButton;
    protected Location location;
    protected Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);
        Firebase.setAndroidContext(getApplicationContext());

        createGameButton = (Button) findViewById(R.id.create_game_start_game);
        gameName = (EditText) findViewById(R.id.game_name_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getDrawable(R.drawable.ic_action_back));

        //location = ImportantMethods.getCurrentLocation(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CreateGameActivity.this, ChooseGameModeActivity.class);
                startActivity(i);
            }
        });

        createGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = gameName.getText().toString();
                int numPlayers = 4;

                game = new Game(name, numPlayers);
                location = ImportantMethods.getCurrentLocation(CreateGameActivity.this);
                game.setRedFlagLocation(location);
                game.sendToFirebase();
                game.switchBlueToRed(ImportantMethods.getCurrentUser());

                Intent intent = new Intent(CreateGameActivity.this, Lobby.class);
                intent.putExtra("gameUid", game.getUid());
                startActivity(intent);
            }
        });
    }
}
