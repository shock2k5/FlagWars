package com.example.kevin.flagwars;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import java.util.HashMap;

public class Lobby extends AppCompatActivity {
    TextView gameName;
    ArrayAdapter<String> redAdapter, blueAdapter;
    ListView redRoster, blueRoster;
    Game game;
    User user;
    Button btnJoinRedTeam, btnJoinBlueTeam, btnStartGameTeam;
    Boolean onRed = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        gameName = (TextView) findViewById(R.id.LobbyGameName);
        btnJoinRedTeam = (Button) findViewById(R.id.btnJoinRed);
        btnJoinBlueTeam = (Button) findViewById(R.id.btnJoinBlue);
        redRoster = (ListView) findViewById(R.id.redRosterList);
        blueRoster = (ListView) findViewById(R.id.blueRosterList);
        btnStartGameTeam = (Button) findViewById(R.id.btnStartGameTeam);

        while (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 0);

        Firebase.setAndroidContext(this.getApplicationContext());
        final Firebase fireRef = new Firebase("https://flagwar.firebaseio.com/");
        final Intent previous = getIntent();

        fireRef.child("User").child(fireRef.getAuth().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Firebase ref = fireRef.child("Game").child(getIntent().getStringExtra("gameUid"));
                HashMap<String, ?> map = (HashMap<String, ?>) dataSnapshot.getValue();
                user = new User((String) map.get("username"));

                btnJoinBlueTeam.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (game.getBlueTeamNames().contains(user.getName())) return;
                        game.switchRedtoBlue(user);
                        onRed = false;
                        updateTeamLists();
                    }
                });

                btnJoinRedTeam.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (game.getRedTeamNames().contains(user.getName())) return;
                        game.switchBlueToRed(user);
                        onRed = true;
                        updateTeamLists();
                    }
                });
                
                btnStartGameTeam.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (game.getBlueTeamNames().size() > 0 && game.getRedTeamNames().size() > 0) {
                            String uid = getIntent().getStringExtra("gameUid");
                            ref.child("started").setValue(true);

                            if (ContextCompat.checkSelfPermission(Lobby.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(Lobby.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                            } else {
                                ref.child("redFlagLatitude").setValue(38.9859);
                                ref.child("redFlagLongitude").setValue(-76.94056);
                                ref.child("blueFlagLatitude").setValue(38.9859);
                                ref.child("blueFlagLongitude").setValue(-76.944294);

                                if (game.getBlueTeamNames().contains(user.getName()))
                                    onRed = false;
                                else if (game.getRedTeamNames().contains(user.getName()))
                                    onRed = true;

                                Intent intent = new Intent(Lobby.this, GameActivity.class);
                                intent.putExtra("gameUid", uid);
                                intent.putExtra("teamColor", (onRed == null) ? "red" : "blue");
                                intent.putExtra("currentUser", user.getName());
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(Lobby.this, "There needs to be at least one player on each team", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                // Listener to check for changes to the Game class
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.child("creator").getValue(String.class).equals(user.getName()))
                            btnStartGameTeam.setVisibility(Button.VISIBLE);
                        boolean started = snapshot.child("started").getValue(Boolean.class);
                        if (started) {
                            if (onRed == null)
                                onRed = true;
                            Intent intent = new Intent(Lobby.this, GameActivity.class);
                            intent.putExtra("gameUid", previous.getStringExtra("gameUid"));
                            intent.putExtra("teamColor", (onRed) ? "red" : "blue");
                            intent.putExtra("currentUser", user.getName());
                            startActivity(intent);
                        } else {
                                String name = snapshot.child("name").getValue(String.class);
                                HashMap<String, String> teamList = (HashMap<String, String>) snapshot.child("teamList").getValue();
                                if (teamList == null) teamList = new HashMap<>();

                                game = new Game(name);
                                game.teamList = teamList;

                                gameName.setText(game.getName());
                                redAdapter = new ArrayAdapter<>(Lobby.this, android.R.layout.simple_list_item_1, game.getRedTeamNames());
                                blueAdapter = new ArrayAdapter<>(Lobby.this, android.R.layout.simple_list_item_1, game.getBlueTeamNames());
                                redRoster.setAdapter(redAdapter);
                                blueRoster.setAdapter(blueAdapter);
                            }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e("Firebase Read Error", "Occurred in Lobby/addListenerForSingleValueEvent", firebaseError.toException());
                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                user = null;
            }
        });
    }

    public void updateTeamLists() {
        redAdapter.clear();
        for (String name : game.getRedTeamNames())
            redAdapter.add(name);
        blueAdapter.clear();
        for (String name : game.getBlueTeamNames())
            blueAdapter.add(name);
    }
}
