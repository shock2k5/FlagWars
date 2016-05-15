package com.example.kevin.flagwars;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
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

import java.util.ArrayList;
import java.util.HashMap;

public class Lobby extends AppCompatActivity {
    ArrayList<String> redTeam, blueTeam;
    TextView gameName;
    ArrayAdapter<String> redAdapter, blueAdapter;
    ListView redRoster, blueRoster;
    Game game;
    User user;
    Button btnJoinRedTeam, btnJoinBlueTeam, btnStartGameTeam;
    boolean onRed = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Firebase.setAndroidContext(this.getApplicationContext());
        final Firebase ref = new Firebase("https://flagwar.firebaseio.com/").child("Game");
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
                    Intent intent = new Intent(Lobby.this, GameActivity.class);
                    if (game.getBlueTeamNames().get(0).equals(user.getName())) {
                        if (ContextCompat.checkSelfPermission(Lobby.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(Lobby.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 0);
                        }

                        LocationManager locationManager = (LocationManager) Lobby.this.getSystemService(Context.LOCATION_SERVICE);
                        Criteria criteria = new Criteria();

                        String provider = locationManager.getBestProvider(criteria, true);
                        Location myLocation = locationManager.getLastKnownLocation(provider);
                        if (myLocation == null) {
                            myLocation = new Location(LocationManager.GPS_PROVIDER);
                            myLocation.setLatitude(38.9859);
                            myLocation.setLongitude(-76.944294);
                        }
                        ref.child(uid).child("blueFlagLatitude").setValue(myLocation.getLatitude());
                        ref.child(uid).child("blueFlagLongitude").setValue(myLocation.getLongitude());
                    } else if (game.getRedTeamNames().get(0).equals(user.getName())) {
                        if (ContextCompat.checkSelfPermission(Lobby.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                            ActivityCompat.requestPermissions(Lobby.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 0);

                        LocationManager locationManager = (LocationManager) Lobby.this.getSystemService(Context.LOCATION_SERVICE);
                        Criteria criteria = new Criteria();

                        String provider = locationManager.getBestProvider(criteria, true);
                        Location myLocation = locationManager.getLastKnownLocation(provider);
                        if (myLocation == null) {
                            myLocation = new Location(LocationManager.GPS_PROVIDER);
                            myLocation.setLatitude(38.986);
                            myLocation.setLongitude(-76.94056);
                        }
                        ref.child(uid).child("redFlagLatitude").setValue(myLocation.getLatitude());
                        ref.child(uid).child("redFlagLongitude").setValue(myLocation.getLongitude());
                    }
                    intent.putExtra("gameUid", uid);
                    intent.putExtra("teamColor", (onRed) ? "red" : "blue");
                    startActivity(intent);
                } else {
                    Toast.makeText(Lobby.this, "There needs to be at least one player on each team", Toast.LENGTH_LONG).show();
                }
            }
        });

        ref.child(previous.getStringExtra("gameUid")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                int numPlayers = Integer.parseInt(snapshot.child("numPlayers").getValue(String.class));
                HashMap<String, String> teamList = (HashMap<String, String>) snapshot.child("teamList").getValue();
                if (teamList == null) teamList = new HashMap<>();

                game = new Game(name, numPlayers);
                game.teamList = teamList;

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

    public void updateTeamLists() {
        redAdapter.clear();
        for (String name : game.getRedTeamNames())
            redAdapter.add(name);
        blueAdapter.clear();
        for (String name : game.getBlueTeamNames())
            blueAdapter.add(name);
    }
}
