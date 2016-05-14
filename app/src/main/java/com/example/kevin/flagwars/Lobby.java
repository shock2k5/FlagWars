package com.example.kevin.flagwars;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
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
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;

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

        Firebase.setAndroidContext(this.getApplicationContext());
        Firebase ref = new Firebase("https://flagwar.firebaseio.com/").child("Game");
        final Intent previous = getIntent();

        gameName = (TextView) findViewById(R.id.LobbyGameName);

        btnJoinRedTeam = (Button) findViewById(R.id.btnJoinRed);
        btnJoinBlueTeam = (Button) findViewById(R.id.btnJoinBlue);
        redRoster = (ListView) findViewById(R.id.redRosterList);
        blueRoster = (ListView) findViewById(R.id.blueRosterList);

        btnStartGameTeam = (Button) findViewById(R.id.btnStartGameTeam);

        user = ImportantMethods.getCurrentUser();

        btnJoinBlueTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (game.getBlueTeam().contains(user)) {
                    return;
                }
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
            public void onDataChange(DataSnapshot snapshot) {
                snapshot = snapshot.child(previous.getStringExtra("gameUid"));
                String name = snapshot.child("name").getValue(String.class);
                int numPlayers = Integer.parseInt(snapshot.child("numPlayers").getValue(String.class));
                Collection<User> red = snapshot.child("redTeam").getValue(new GenericTypeIndicator<ArrayList<User>>() {});
                Collection<User> blue = snapshot.child("blueTeam").getValue(new GenericTypeIndicator<ArrayList<User>>() {});
                ArrayList<User> redTeam = (red == null) ? new ArrayList<User>() : new ArrayList<>(red);
                ArrayList<User> blueTeam = (blue == null) ? new ArrayList<User>() : new ArrayList<>(blue);

                Location anchorLocation, redFlag, blueFlag;
                if (snapshot.child("anchorLocationLatitude").getValue() != null) {
                    anchorLocation = new Location(LocationManager.GPS_PROVIDER);
                    anchorLocation.setLatitude(snapshot.child("anchorLocationLatitude").getValue(Double.class));
                    anchorLocation.setLongitude(snapshot.child("anchorLocationLongitude").getValue(Double.class));
                } else {
                    anchorLocation = null;
                }

                if (snapshot.child("redFlagLatitude").getValue() != null) {
                    redFlag = new Location(LocationManager.GPS_PROVIDER);
                    redFlag.setLatitude(snapshot.child("redFlagLatitude").getValue(Double.class));
                    redFlag.setLongitude(snapshot.child("redFlagLongitude").getValue(Double.class));
                } else {
                    redFlag = null;
                }

                if (snapshot.child("blueFlagLatitude").getValue(Double.class) != null) {
                    blueFlag = new Location(LocationManager.GPS_PROVIDER);
                    blueFlag.setLatitude(snapshot.child("blueFlagLatitude").getValue(Double.class));
                    blueFlag.setLongitude(snapshot.child("blueFlagLongitude").getValue(Double.class));
                } else {
                    blueFlag = null;
                }

                game = new Game();
                game.name = name;
                game.numPlayers = numPlayers;
                game.redTeam = redTeam;
                game.blueTeam = blueTeam;
                game.anchorLocation = anchorLocation;
                game.redFlag = redFlag;
                game.blueFlag = blueFlag;

                gameName.setText(game.getName());
                redAdapter = new ArrayAdapter<>(Lobby.this, android.R.layout.simple_list_item_1, game.getRedTeamNames());
                blueAdapter = new ArrayAdapter<>(Lobby.this, android.R.layout.simple_list_item_1, game.getBlueTeamNames());
                redRoster.setAdapter(redAdapter);
                blueRoster.setAdapter(blueAdapter);
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

        Log.d("Debug", "red:\n" + game.getRedTeamNames().toString() + "\nblue:\n" + game.getBlueTeamNames());
    }
}
