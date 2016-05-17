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
import com.google.android.gms.maps.model.LatLngBounds;
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
        setContentView(R.layout.activity_game);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Firebase.setAndroidContext(this.getApplicationContext());
        ref = ImportantMethods.getFireBase().child("Game").child(getIntent().getStringExtra("gameUid"));

        myClient = new GoogleApiClient.Builder(GameActivity.this).addApi(LocationServices.API).addConnectionCallbacks(GameActivity.this)
                .addOnConnectionFailedListener(GameActivity.this).build();

        ImportantMethods.getFireBase().child("User").child(ImportantMethods.getFireBase().getAuth().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, ?> map = (HashMap<String, ?>) dataSnapshot.getValue();
                currentUser = new User((String) map.get("username"));

                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        loc = location;
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationToLatLng(loc), ZOOM_LEVEL), 4000, null);
                        ref.child("liveLocations").child(currentUser.getName()).child("locations").child("latitude").setValue(loc.getLatitude());
                        ref.child("liveLocations").child(currentUser.getName()).child("locations").child("longitude").setValue(loc.getLongitude());
                        ref.child("liveLocations").child(currentUser.getName()).child("teamColor").setValue(getIntent().getStringExtra("teamColor"));
                    }
                };
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("Firebase error", "Error in GameActivity onCreate", firebaseError.toException());
                currentUser = null;
            }
        });
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

        final LocationRequest locationRequest = new LocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(myClient, locationRequest, locationListener);


        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                HashMap<String, String> teamList = (HashMap<String, String>) snapshot.child("teamList").getValue();
                if (teamList == null) teamList = new HashMap<>();

                HashMap<String, HashMap<String, Object>> liveLocationsMap =
                        (HashMap<String, HashMap<String, Object>>) snapshot.child("liveLocations").getValue();

                Location redFlag = null, blueFlag = null;
                game = new Game(name);
                game.teamList = teamList;

                mMap.clear();
                if (snapshot.child("redFlagLatitude").getValue(Double.class) != null &&
                        snapshot.child("redFlagLongitude").getValue(Double.class) != null &&
                        snapshot.child("blueFlagLatitude").getValue(Double.class) != null &&
                        snapshot.child("blueFlagLongitude").getValue(Double.class) != null) {
                    redFlag = new Location(LocationManager.GPS_PROVIDER);
                    redFlag.setLatitude(snapshot.child("redFlagLatitude").getValue(Double.class));
                    redFlag.setLongitude(snapshot.child("redFlagLongitude").getValue(Double.class));

                    blueFlag = new Location(LocationManager.GPS_PROVIDER);
                    blueFlag.setLatitude(snapshot.child("blueFlagLatitude").getValue(Double.class));
                    blueFlag.setLongitude(snapshot.child("blueFlagLongitude").getValue(Double.class));

                    game.redFlag = redFlag;
                    game.blueFlag = blueFlag;
                    mMap.addMarker(new MarkerOptions()
                            .position(locationToLatLng(game.getRedFlagLocation()))
                            .title("Red Flag")
                            .draggable(false));
                    mMap.addMarker(new MarkerOptions()
                            .position(locationToLatLng(game.getBlueFlagLocation()))
                            .title("Blue Flag")
                            .draggable(false));
                }

                if (liveLocationsMap != null) {
                    for (String userName : liveLocationsMap.keySet()) {
                        String teamColor = (String) liveLocationsMap.get(userName).get("teamColor");
                        HashMap<String, Double> locationsMap = (HashMap<String, Double>) liveLocationsMap.get(userName).get("locations");
                        if (locationsMap != null) {
                            LatLng userLocation = new LatLng(locationsMap.get("latitude"), locationsMap.get("longitude"));
                            mMap.addMarker(new MarkerOptions()
                                    .position(userLocation)
                                    .title(teamColor + " " + userName));
                        } else {
                            Log.d("Null locations", userName + " " + teamColor);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.err.println("There was an error getting the Game from Firebase: " + firebaseError);
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
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
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(
                    new LatLng(38.985439, -76.944562), // SW corner of mall
                    new LatLng(38.986507, -76.940271) // NE corner of mall
            ), 0));
        }
    }

    private LatLng locationToLatLng(Location loc) {
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    private double distanceInKm(double lat1, double lng1, double lat2, double lng2) {
        int r = 6371; // average radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = r * c;
        return d;
    }
}
