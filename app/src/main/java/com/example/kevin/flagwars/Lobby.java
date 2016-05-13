package com.example.kevin.flagwars;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.LocationCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.ArrayList;

public class Lobby extends AppCompatActivity {
    ArrayList<String> redTeam, blueTeam;
    TextView gameName;
    ArrayAdapter<String> redAdapter, blueAdapter;
    ListView redRoster, blueRoster;
    Game game;
    Button btnJoinRedTeam, btnJoinBlueTeam, btnStartGameTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        game = Game.getObjectFromParse(getIntent().getStringExtra("gameObjectId"));
        gameName = (TextView) findViewById(R.id.LobbyGameName);
        btnJoinRedTeam = (Button) findViewById(R.id.btnJoinRed);
        btnJoinRedTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser user = ParseUser.getCurrentUser();
                if (game.getRedTeam().contains(user)) return;

                game.removeFromBlueTeam(user);
                if (game.getBlueTeam().size() == 0)
                    game.setBlueFlagLocation(null);

                game.addToRedTeam(user);
                if (game.getRedTeam().size() == 1) {
                    ParseGeoPoint.getCurrentLocationInBackground(100, new LocationCallback() {
                        @Override
                        public void done(ParseGeoPoint geoPoint, ParseException e){
                            if (e == null)
                                game.setRedFlagLocation(geoPoint);
                            else if (geoPoint == null)
                                game.setRedFlagLocation(new ParseGeoPoint(37.422, -122.084));
                            else
                                e.printStackTrace();
                        }
                    });
                }

                updateTeamLists();
                game.saveInParse();
            }
        });
        btnJoinBlueTeam = (Button) findViewById(R.id.btnJoinBlue);
        btnJoinBlueTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser user = ParseUser.getCurrentUser();
                if (game.getBlueTeam().contains(user))
                    return;

                game.removeFromRedTeam(user);
                if (game.getRedTeam().size() == 0)
                    game.setRedFlagLocation(null);

                game.addToBlueTeam(user);
                if (game.getBlueTeam().size() == 1) {
                    ParseGeoPoint.getCurrentLocationInBackground(100, new LocationCallback() {
                        @Override
                        public void done(ParseGeoPoint geoPoint, ParseException e) {
                            if (e == null)
                                game.setBlueFlagLocation(geoPoint);
                            else if (geoPoint == null)
                                game.setBlueFlagLocation(new ParseGeoPoint(37.422, -122.084));
                            else
                                e.printStackTrace();
                        }
                    });
                }

                updateTeamLists();
                game.saveInParse();
            }
        });

        game = Game.getObjectFromParse(getIntent().getStringExtra("gameObjectId"));
        gameName.setText(game.getName());

        redRoster = (ListView) findViewById(R.id.redRosterList);
        blueRoster = (ListView) findViewById(R.id.blueRosterList);
        redAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, game.getRedTeamNames());
        blueAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, game.getBlueTeamNames());
        redRoster.setAdapter(redAdapter);
        blueRoster.setAdapter(blueAdapter);

        btnStartGameTeam = (Button) findViewById(R.id.btnStartGameTeam);
        btnStartGameTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (game.getBlueTeam().size() > 0 && game.getRedTeam().size() > 0) {
                    Intent intent = new Intent(Lobby.this, GameActivity.class);
                    intent.putExtra("gameObjectId", game.getObjectId());
                    startActivity(intent);
                } else {
                    Toast.makeText(Lobby.this, "There needs to be at least one player on each team", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void updateTeamLists(){
        redAdapter.clear();
        for (String name : game.getRedTeamNames())
            redAdapter.add(name);
        blueAdapter.clear();
        for (String name : game.getBlueTeamNames())
            blueAdapter.add(name);
    }
}
