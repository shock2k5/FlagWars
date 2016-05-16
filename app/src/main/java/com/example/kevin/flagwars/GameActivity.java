package com.example.kevin.flagwars;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class GameActivity
        extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private Location loc;
    private Game game;
    private User currentUser;
    private GoogleApiClient myClient;
    private Firebase ref;
    LocationListener locationListener;
    final float ZOOM_LEVEL = 16.5f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = CurrentUser.getCurrentUser(GameActivity.this.getApplicationContext());
        setContentView(R.layout.activity_game);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Firebase.setAndroidContext(this.getApplicationContext());
        ref = ImportantMethods.getFireBase().child("Game").child(getIntent().getStringExtra("gameUid"));

        myClient = new GoogleApiClient.Builder(GameActivity.this).addApi(LocationServices.API).addConnectionCallbacks(GameActivity.this)
                .addOnConnectionFailedListener(GameActivity.this).build();

        ref.child("liveLocations").child(currentUser.getName()).child("teamColor").setValue(getIntent().getStringExtra("teamColor"));
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                loc = location;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationToLatLng(loc), ZOOM_LEVEL), 4000, null);
                ref.child("liveLocations").child(currentUser.getName()).child("locations").child("latitude").setValue(loc.getLatitude());
                ref.child("liveLocations").child(currentUser.getName()).child("locations").child("longitude").setValue(loc.getLongitude());
            }
        };
    }

    public void onStart() {
        super.onStart();
        myClient.connect();
    }

    public void onStop() {
        super.onStop();
        LocationServices.FusedLocationApi.removeLocationUpdates(myClient, locationListener);
        myClient.disconnect();
    }

    public void onConnected(Bundle connectionHint) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

        loc = LocationServices.FusedLocationApi.getLastLocation(myClient);
        if (loc == null) {
            loc = new Location(LocationManager.GPS_PROVIDER);
            loc.setLatitude(38.985933);
            loc.setLongitude(-76.942792);
        }

        LocationRequest locationRequest = new LocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(myClient, locationRequest, locationListener);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                int numPlayers = snapshot.child("numPlayers").getValue(Integer.class);
                HashMap<String, String> teamList = (HashMap<String, String>) snapshot.child("teamList").getValue();
                if (teamList == null) teamList = new HashMap<>();
                game = new Game(name, numPlayers);
                game.teamList = teamList;
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.err.println("There was an error getting the Game from Firebase: " + firebaseError);
            }
        });

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                HashMap<String, HashMap<String, Object>> liveLocationsMap =
                        (HashMap<String, HashMap<String, Object>>) snapshot.child("liveLocations").getValue();

                mMap.clear();
                Double rfLat = snapshot.child("redFlagLatitude").getValue(Double.class);
                Double rfLong = snapshot.child("redFlagLongitude").getValue(Double.class);
                Double bfLat = snapshot.child("blueFlagLatitude").getValue(Double.class);
                Double bfLong = snapshot.child("blueFlagLongitude").getValue(Double.class);

                if (game != null && rfLat != null && rfLong != null && bfLat != null && bfLong != null) {
                    game.redFlag = new Location(LocationManager.GPS_PROVIDER);
                    game.redFlag.setLatitude(rfLat);
                    game.redFlag.setLongitude(rfLong);

                    game.blueFlag = new Location(LocationManager.GPS_PROVIDER);
                    game.blueFlag.setLatitude(bfLat);
                    game.blueFlag.setLongitude(bfLong);

                    mMap.addMarker(new MarkerOptions()
                            .position(locationToLatLng(game.getRedFlagLocation()))
                            .title("Red Flag")
                            .draggable(false));
                    mMap.addMarker(new MarkerOptions()
                            .position(locationToLatLng(game.getBlueFlagLocation()))
                            .title("Blue Flag")
                            .draggable(false));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationToLatLng(loc), ZOOM_LEVEL), 4000, null);
                }

                for (String userName : liveLocationsMap.keySet()) {
                    String teamColor = (String) liveLocationsMap.get(userName).get("teamColor");
                    LatLng userLocation = new LatLng(
                            ((HashMap<String, Double>) liveLocationsMap.get(userName).get("locations")).get("latitude"),
                            ((HashMap<String, Double>) liveLocationsMap.get(userName).get("locations")).get("longitude"));
                    mMap.addMarker(new MarkerOptions()
                            .position(userLocation)
                            .title(teamColor + " " + userName)
                            .draggable(false));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.err.println("There was an error getting the LiveLocations from Firebase: " + firebaseError);
            }
        });
    }

    public void onConnectionSuspended(int cause) {
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    if (ActivityCompat.checkSelfPermission(GameActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(GameActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                        return false;
                    } else {
                        loc = LocationServices.FusedLocationApi.getLastLocation(myClient);
                        if (loc == null) {
                            loc = new Location(LocationManager.GPS_PROVIDER);
                            loc.setLatitude(38.985933);
                            loc.setLongitude(-76.942792);
                        }
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationToLatLng(loc), ZOOM_LEVEL), 4000, null);
                        return true;
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(GameActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }

    private LatLng locationToLatLng(Location loc) {
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }
}
