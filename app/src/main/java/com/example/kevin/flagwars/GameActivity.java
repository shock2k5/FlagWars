package com.example.kevin.flagwars;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
import java.util.Observable;
import java.util.Observer;

public class GameActivity
        extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    final float ZOOM_LEVEL = 16.5f;
    final int RADIUS = 10;
    final int REFRESH_INTERVAL = 100;

    private GoogleMap mMap;
    private Location loc = null;
    private Game game;
    private String currentUser, teamColor;
    private GoogleApiClient myClient;
    private Firebase ref;
    private LocationListener locationListener;
    private boolean reload = true;
    private FloatingActionButton mCaptureButton;
    private float[] distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        distance = new float[1];
        mCaptureButton = (FloatingActionButton) findViewById(R.id.capture_button);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Firebase.setAndroidContext(this.getApplicationContext());
        ref = ImportantMethods.getFireBase().child("Game").child(getIntent().getStringExtra("gameUid"));


        currentUser = this.getIntent().getStringExtra("currentUser");
        teamColor = this.getIntent().getStringExtra("teamColor");

        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref.child("holding").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        boolean bfLatBool = snapshot.child("blueFlagLatitude").getValue(Double.class) != null;
                        boolean bfLongBool = snapshot.child("blueFlagLongitude").getValue(Double.class) != null;
                        boolean rfLatBool = snapshot.child("redFlagLatitude").getValue(Double.class) != null;
                        boolean rfLongBool = snapshot.child("redFlagLongitude").getValue(Double.class) != null;

                        if (teamColor.equals("blue")) {
                            if (bfLatBool && bfLongBool) {
                                // red returnFlag
                                ref.child("holding").child("redFlagLatitude").removeValue();
                                ref.child("holding").child("redFlagLongitude").removeValue();
                                Toast.makeText(GameActivity.this, "Red flag returned.", Toast.LENGTH_LONG).show();
                            }

                            if (rfLatBool && rfLongBool) {
                                // blue grabFlag
                                ref.child("holding").child("redFlagLatitude").setValue(loc.getLatitude());
                                ref.child("holding").child("redFlagLongitude").setValue(loc.getLongitude());
                                Toast.makeText(GameActivity.this, "Red flag captured.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (rfLatBool && rfLongBool) {
                                // red returnFlag
                                ref.child("holding").child("blueFlagLatitude").removeValue();
                                ref.child("holding").child("blueFlagLongitude").removeValue();
                                Toast.makeText(GameActivity.this, "Blue flag returned.", Toast.LENGTH_LONG).show();
                            }

                            if (bfLatBool && bfLongBool) {
                                // red grabFlag
                                ref.child("holding").child("blueFlagLatitude").setValue(loc.getLatitude());
                                ref.child("holding").child("blueFlagLongitude").setValue(loc.getLongitude());
                                Toast.makeText(GameActivity.this, "Blue flag captured.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.d("Debug", "GameActivity mCaptureButton onClickListener", firebaseError.toException());
                    }
                });
            }
        });

        myClient = new GoogleApiClient.Builder(GameActivity.this).addApi(LocationServices.API).addConnectionCallbacks(GameActivity.this)
                .addOnConnectionFailedListener(GameActivity.this).build();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                loc = location;
                if (reload) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationToLatLng(loc), ZOOM_LEVEL), 4000, null);
                    reload = false;
                }

                AsyncTask<Void, Void, Location> asyncTask = new AsyncTask<Void, Void, Location>() {
                    @Override
                    protected Location doInBackground(Void... params) {
                        HashMap<String, Object> update = new HashMap<>();
                        HashMap<String, Double> coords = new HashMap<>();
                        coords.put("latitude", loc.getLatitude());
                        coords.put("longitude", loc.getLongitude());
                        update.put("locations", coords);
                        update.put("teamColor", teamColor);

                        ref.child("liveLocations").child(currentUser).updateChildren(update);
                        return loc;
                    }
                };
                asyncTask.execute();
            }
        };
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
        LocationServices.FusedLocationApi.requestLocationUpdates(myClient, new LocationRequest().setInterval(REFRESH_INTERVAL), locationListener);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                HashMap<String, String> teamList = (HashMap<String, String>) snapshot.child("teamList").getValue();
                if (teamList == null) teamList = new HashMap<>();

                if (game == null) game = new Game(name);
                game.teamList = teamList;

                if (snapshot.child("redFlagLatitude").getValue(Double.class) != null &&
                        snapshot.child("redFlagLongitude").getValue(Double.class) != null &&
                        snapshot.child("blueFlagLatitude").getValue(Double.class) != null &&
                        snapshot.child("blueFlagLongitude").getValue(Double.class) != null) {
                    HashMap<String, HashMap<String, Object>> liveLocationsMap =
                            (HashMap<String, HashMap<String, Object>>) snapshot.child("liveLocations").getValue();

                    Double rfLat = snapshot.child("redFlagLatitude").getValue(Double.class);
                    Double rfLong = snapshot.child("redFlagLongitude").getValue(Double.class);
                    Double bfLat = snapshot.child("blueFlagLatitude").getValue(Double.class);
                    Double bfLong = snapshot.child("blueFlagLongitude").getValue(Double.class);
                    boolean holding = false;

                    if (snapshot.child("holding").child("redFlagLatitude").getValue(Double.class) != null &&
                            snapshot.child("holding").child("redFlagLongitude").getValue(Double.class) != null) {
                        rfLat = snapshot.child("holding").child("redFlagLatitude").getValue(Double.class);
                        rfLong = snapshot.child("holding").child("redFlagLongitude").getValue(Double.class);
                        holding = true;
                    }

                    if (snapshot.child("holding").child("blueFlagLatitude").getValue(Double.class) != null &&
                            snapshot.child("holding").child("blueFlagLongitude").getValue(Double.class) != null) {
                        bfLat = snapshot.child("holding").child("blueFlagLatitude").getValue(Double.class);
                        bfLong = snapshot.child("holding").child("blueFlagLongitude").getValue(Double.class);
                        holding = true;
                    }

                    game.redFlag = new Location(LocationManager.GPS_PROVIDER);
                    game.redFlag.setLatitude(rfLat);
                    game.redFlag.setLongitude(rfLong);

                    game.blueFlag = new Location(LocationManager.GPS_PROVIDER);
                    game.blueFlag.setLatitude(bfLat);
                    game.blueFlag.setLongitude(bfLong);

                    mMap.clear();
                    mMap.addMarker(new MarkerOptions()
                            .position(locationToLatLng(game.getRedFlagLocation())).title("Red Flag")
                            .draggable(false));
                    mMap.addMarker(new MarkerOptions()
                            .position(locationToLatLng(game.getBlueFlagLocation())).title("Blue Flag")
                            .draggable(false));

                    if (!holding) {
                        mMap.addCircle(new CircleOptions()
                                .center(locationToLatLng(game.getRedFlagLocation())).radius(RADIUS)
                                .strokeColor(Color.RED).fillColor(Color.RED));
                        mMap.addCircle(new CircleOptions()
                                .center(locationToLatLng(game.getBlueFlagLocation())).radius(RADIUS)
                                .strokeColor(Color.BLUE).fillColor(Color.BLUE));
                    }

                    if (liveLocationsMap != null) {
                        for (String userName : liveLocationsMap.keySet()) {
                            String playerTeamColor = (String) liveLocationsMap.get(userName).get("teamColor");
                            if (playerTeamColor != null) {
                                HashMap<String, Double> locationsMap = (HashMap<String, Double>) liveLocationsMap.get(userName).get("locations");
                                LatLng playerLocation = new LatLng(locationsMap.get("latitude"), locationsMap.get("longitude"));
                                mMap.addMarker(new MarkerOptions().position(playerLocation).title(playerTeamColor + " " + userName));
                                if (playerTeamColor.equals("red")) {
                                    Location.distanceBetween(locationsMap.get("latitude"),
                                            locationsMap.get("longitude"), game.blueFlag.getLatitude(), game.blueFlag.getLongitude(), distance);
                                    Log.d("Debug", "Distance: " + Float.toString(distance[0]));
                                    if (distance[0] <= RADIUS)
                                        mCaptureButton.setVisibility(View.VISIBLE);
                                } else if (playerTeamColor.equals("blue")) {
                                    Log.d("Debug", "Distance: " + Float.toString(distance[0]));
                                    Location.distanceBetween(locationsMap.get("latitude"),
                                            locationsMap.get("longitude"), game.redFlag.getLatitude(), game.redFlag.getLongitude(), distance);
                                    if (distance[0] <= RADIUS)
                                        mCaptureButton.setVisibility(View.VISIBLE);
                                }
                            }
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

    public void onConnectionSuspended(int cause) {}

    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (ActivityCompat.checkSelfPermission(GameActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(GameActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                    return false;
                } else {
                    return true;
                }
            }
        });
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMyLocationEnabled(true);
    }

    private LatLng locationToLatLng(Location loc) {
        return (loc == null) ? new LatLng(38.985933, -76.942792)
                : new LatLng(loc.getLatitude(), loc.getLongitude());
    }
}
