package com.example.kevin.flagwars;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.firebase.client.annotations.NotNull;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;

public class Lobby extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    TextView gameName;
    ArrayAdapter<String> redAdapter, blueAdapter;
    ListView redRoster, blueRoster;
    Game game;
    User user;
    Button btnJoinRedTeam, btnJoinBlueTeam, btnStartGameTeam;
    boolean onRed = true;
    GoogleApiClient myClient;
    Location loc;
    LocationListener locationListener;

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

        Firebase.setAndroidContext(this.getApplicationContext());
        final Firebase fireRef = new Firebase("https://flagwar.firebaseio.com/");
        final Intent previous = getIntent();
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                loc = location;
            }
        };
        myClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        fireRef.child("User").child(fireRef.getAuth().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Firebase ref = fireRef.child("Game");
                HashMap<String, ?> map = (HashMap<String, ?>) dataSnapshot.getValue();
                user = new User((String) map.get("username"));

                if (getIntent().getStringExtra("creator").equals(user.getName()))
                    btnStartGameTeam.setVisibility(Button.VISIBLE);

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

                            if (ContextCompat.checkSelfPermission(Lobby.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                                ActivityCompat.requestPermissions(Lobby.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 0);

                            loc = LocationServices.FusedLocationApi.getLastLocation(myClient);
                            if (game.getBlueTeamNames().get(0).equals(user.getName())) {
                                if (loc == null) {
                                    loc = new Location(LocationManager.GPS_PROVIDER);
                                    loc.setLatitude(38.9859);
                                    loc.setLongitude(-76.944294);
                                }
                                fireRef.child("Game").child(uid).child("blueFlagLatitude").setValue(loc.getLatitude());
                                fireRef.child("Game").child(uid).child("blueFlagLongitude").setValue(loc.getLongitude());
                            } else if (game.getRedTeamNames().get(0).equals(user.getName())) {
                                if (loc == null) {
                                    loc = new Location(LocationManager.GPS_PROVIDER);
                                    loc.setLatitude(38.986);
                                    loc.setLongitude(-76.94056);
                                }
                                fireRef.child("Game").child(uid).child("redFlagLatitude").setValue(loc.getLatitude());
                                fireRef.child("Game").child(uid).child("redFlagLongitude").setValue(loc.getLongitude());
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

    public void onConnected(Bundle connectedHint) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 0);
        LocationRequest locationRequest = new LocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(myClient, locationRequest, locationListener);
    }

    public void onConnectionSuspended(int cause) {}

    public void onConnectionFailed(@NotNull ConnectionResult connectionResult) {}
}
