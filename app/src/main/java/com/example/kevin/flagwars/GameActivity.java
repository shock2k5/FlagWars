package com.example.kevin.flagwars;

import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class GameActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location loc;
    private Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        loc = ImportantMethods.getCurrentLocation(this);
        String uid = getIntent().getStringExtra("gameUid");

        final Firebase ref = ImportantMethods.getFireBase().child("Game").child(uid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                int numPlayers = Integer.parseInt(snapshot.child("numPlayers").getValue(String.class));
                HashMap<String, String> teamList = (HashMap<String, String>) snapshot.child("teamList").getValue();
                if (teamList == null) teamList = new HashMap<>();

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

                game = new Game(name, numPlayers);
                game.anchorLocation = anchorLocation;
                game.teamList = teamList;
                if (redFlag == null) {
                    redFlag = new Location(LocationManager.GPS_PROVIDER);
                    redFlag.setLatitude(38.986);
                    redFlag.setLongitude(-76.94056);
                }
                if (blueFlag == null) {
                    blueFlag = new Location(LocationManager.GPS_PROVIDER);
                    blueFlag.setLatitude(38.9859);
                    blueFlag.setLongitude(-76.944294);
                }
                game.redFlag = redFlag;
                game.blueFlag = blueFlag;

                LatLng currentLocation = locationToLatLng(loc);
                mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                mMap.addMarker(new MarkerOptions()
                        .position(locationToLatLng(game.getRedFlagLocation()))
                        .title("Red Flag")
                        .draggable(false)
                        .flat(true));
                mMap.addMarker(new MarkerOptions()
                        .position(locationToLatLng(game.getBlueFlagLocation()))
                        .title("Blue Flag")
                        .draggable(false)
                        .flat(true));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.err.println("There was an error getting the Game from Firebase: " + firebaseError);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private LatLng locationToLatLng(Location loc) {
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }
}
