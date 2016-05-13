package com.example.kevin.flagwars;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.parse.LocationCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.ArrayList;

public class CreateGameActivity extends AppCompatActivity {
    protected EditText gameName; // radio0 is Red, radio1 is Blue
    protected Button createGameButton;
    protected Game game;
    ParseGeoPoint location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);
        Firebase.setAndroidContext(this.getApplicationContext());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        toolbar.setNavigationIcon(getDrawable(R.drawable.ic_action_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CreateGameActivity.this, ChooseGameModeActivity.class);
                startActivity(i);
            }
        });

        createGameButton = (Button) findViewById(R.id.create_game_start_game);

        Toast.makeText(getApplicationContext(), ImportantMethods.getUserName(), Toast.LENGTH_SHORT).show();
        //Set variables to the appropriate editText, ,etc.
        gameName = (EditText) findViewById(R.id.game_name_edit);

        createGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = gameName.getText().toString();
                int numPlayers = 4;
                ArrayList<ParseGeoPoint> flagLocations = new ArrayList<>(2);
                ArrayList<ParseUser> redTeamNames = new ArrayList<ParseUser>(numPlayers/2);
                ArrayList<ParseUser> blueTeamNames = new ArrayList<ParseUser>(numPlayers/2);

                game = new Game(name, numPlayers);

                ParseGeoPoint.getCurrentLocationInBackground(100, new LocationCallback() {
                    @Override
                    public void done(ParseGeoPoint geoPoint, ParseException e){
                        if (e == null)
                            location = geoPoint;
                        else if (geoPoint == null)
                            location = new ParseGeoPoint(37.422, -122.084);
                        else
                            e.printStackTrace();

                        //game.setRedFlagLocation(location);

                        Intent intent = new Intent(CreateGameActivity.this, Lobby.class);
                        intent.putExtra("gameID", gameName.getText().toString());
                        startActivity(intent);
                    }
                });
            }
        });

    }
}
