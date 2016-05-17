package com.example.kevin.flagwars;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.CircleOptions;
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
    private boolean reload = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Firebase.setAndroidContext(this.getApplicationContext());
        ref = ImportantMethods.getFireBase();

        myClient = new GoogleApiClient.Builder(GameActivity.this).addApi(LocationServices.API).addConnectionCallbacks(GameActivity.this)
                .addOnConnectionFailedListener(GameActivity.this).build();

        ref.child("User").child(ref.getAuth().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, ?> map = (HashMap<String, ?>) dataSnapshot.getValue();
                currentUser = new User((String) map.get("username"));

                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        loc = location;
                        if (reload) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationToLatLng(loc), ZOOM_LEVEL), 4000, null);
                            reload = false;
                        } else {
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(locationToLatLng(loc)));
                        }

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

        ref = ref.child("Game").child(getIntent().getStringExtra("gameUid"));
    }

    public void onStart() {
        super.onStart();
        myClient.connect();
    }

    public void onStop() {
        super.onStop();
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

        final LocationRequest locationRequest = new LocationRequest().setInterval(50);
        LocationServices.FusedLocationApi.requestLocationUpdates(myClient, locationRequest, locationListener);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                HashMap<String, String> teamList = (HashMap<String, String>) dataSnapshot.child("teamList").getValue();
                if (teamList == null) teamList = new HashMap<>();

                game = new Game(name);
                game.teamList = teamList;
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.child("redFlagLatitude").getValue(Double.class) != null &&
                                snapshot.child("redFlagLongitude").getValue(Double.class) != null &&
                                snapshot.child("blueFlagLatitude").getValue(Double.class) != null &&
                                snapshot.child("blueFlagLongitude").getValue(Double.class) != null) {
                            HashMap<String, HashMap<String, Object>> liveLocationsMap =
                                    (HashMap<String, HashMap<String, Object>>) snapshot.child("liveLocations").getValue();
                            game.redFlag = new Location(LocationManager.GPS_PROVIDER);
                            game.redFlag.setLatitude(snapshot.child("redFlagLatitude").getValue(Double.class));
                            game.redFlag.setLongitude(snapshot.child("redFlagLongitude").getValue(Double.class));

                            game.blueFlag = new Location(LocationManager.GPS_PROVIDER);
                            game.blueFlag.setLatitude(snapshot.child("blueFlagLatitude").getValue(Double.class));
                            game.blueFlag.setLongitude(snapshot.child("blueFlagLongitude").getValue(Double.class));

                            mMap.addMarker(new MarkerOptions()
                                    .position(locationToLatLng(game.getRedFlagLocation()))
                                    .title("Red Flag")
                                    .draggable(false));
                            mMap.addCircle(new CircleOptions()
                                    .center(locationToLatLng(game.getRedFlagLocation()))
                                    .radius(100)
                                    .strokeColor(Color.RED)
                                    .fillColor(Color.RED));
                            mMap.addMarker(new MarkerOptions()
                                    .position(locationToLatLng(game.getBlueFlagLocation()))
                                    .title("Blue Flag")
                                    .draggable(false));
                            mMap.addCircle(new CircleOptions()
                                    .center(locationToLatLng(game.getBlueFlagLocation()))
                                    .radius(100)
                                    .strokeColor(Color.BLUE)
                                    .fillColor(Color.BLUE));

                            for (String userName : liveLocationsMap.keySet()) {
                                String teamColor = (String) liveLocationsMap.get(userName).get("teamColor");
                                if (teamColor != null) {
                                    HashMap<String, Double> locationsMap = (HashMap<String, Double>) liveLocationsMap.get(userName).get("locations");
                                    LatLng userLocation = new LatLng(locationsMap.get("latitude"), locationsMap.get("longitude"));
                                    mMap.addMarker(new MarkerOptions().position(userLocation).title(teamColor + " " + userName));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        System.err.println("There was an error getting the LiveLocations from Firebase: " + firebaseError);
                    }
                });
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
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationToLatLng(loc), ZOOM_LEVEL), 4000, null);
        }
    }

    private LatLng locationToLatLng(Location loc) {
        if (loc == null) {
            return new LatLng(38.985933, -76.942792);
        }
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    private double distanceInMeters(double lat1, double lng1, double lat2, double lng2) {
        int r = 6371; // average radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = r * c;
        return d/1000;
    }
}
