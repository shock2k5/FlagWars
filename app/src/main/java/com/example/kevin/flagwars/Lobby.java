package com.example.kevin.flagwars;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

public class Lobby extends AppCompatActivity {
    ArrayList<String> redTeam, blueTeam;
    TextView gameName;
    ArrayAdapter<String> redAdapter, blueAdapter;
    ListView redRoster, blueRoster;
    Game game;
    User user;
    Button btnJoinRedTeam, btnJoinBlueTeam, btnStartGameTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Firebase ref = new Firebase("https://flagwar.firebaseio.com/");
        final Intent previous = getIntent();

        gameName = (TextView) findViewById(R.id.LobbyGameName);

        btnJoinRedTeam = (Button) findViewById(R.id.btnJoinRed);
        btnJoinBlueTeam = (Button) findViewById(R.id.btnJoinBlue);
        redRoster = (ListView) findViewById(R.id.redRosterList);
        blueRoster = (ListView) findViewById(R.id.blueRosterList);

        btnStartGameTeam = (Button) findViewById(R.id.btnStartGameTeam);

        redAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, game.getRedTeamNames());
        blueAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, game.getBlueTeamNames());

        gameName.setText(game.getName());
        redRoster.setAdapter(redAdapter);
        blueRoster.setAdapter(blueAdapter);

        btnJoinBlueTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (game.getBlueTeam().contains(user))
                    return;

                game.removeFromRedTeam(user);
                game.addToBlueTeam(user);
                if (game.getBlueTeam().size() == 1 && game.getBlueFlagLocation() == null) {
                    // TODO update location
                    game.setBlueFlagLocation(null);
                }

                updateTeamLists();
            }
        });

        btnJoinRedTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (game.getRedTeam().contains(user)) return;

                game.removeFromBlueTeam(user);
                game.addToRedTeam(user);
                if (game.getRedTeam().size() == 1 && game.getRedFlagLocation() == null) {
                    // TODO update location
                    game.setRedFlagLocation(null);
                }

                updateTeamLists();
            }
        });

        btnStartGameTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (game.getBlueTeam().size() > 0 && game.getRedTeam().size() > 0) {
                    Intent intent = new Intent(Lobby.this, GameActivity.class);
                    intent.putExtra("gameID", getIntent().getStringExtra("gameID"));
                    startActivity(intent);
                } else {
                    Toast.makeText(Lobby.this, "There needs to be at least one player on each team", Toast.LENGTH_LONG).show();
                }
            }
        });

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uid = previous.getStringExtra("gameUid");
                game = dataSnapshot.child("Game/" + uid).getValue(Game.class);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("Firebase Read Error", "Occurred in Lobby/addListenerForSingleValueEvent", firebaseError.toException());
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
