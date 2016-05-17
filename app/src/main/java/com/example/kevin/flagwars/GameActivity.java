package com.example.kevin.flagwars;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.client.snapshot.DoubleNode;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

@SuppressWarnings("unchecked")
public class GameActivity
        extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    final float ZOOM_LEVEL = 16.5f;
    final int RADIUS = 10;
    final int REFRESH_INTERVAL = 10;

    private GoogleMap mMap;
    private Location loc = null;
    private Game game;
    private String currentUser, teamColor;
    private GoogleApiClient myClient;
    private Firebase ref;
    private LocationListener locationListener;
    private boolean reload = true, userHoldingFlag = false;
    private Button mCaptureButton;
    private float[] distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        distance = new float[1];
        mCaptureButton = (Button) findViewById(R.id.capture_button);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Firebase.setAndroidContext(this.getApplicationContext());
        ref = ImportantMethods.getFireBase().child("Game").child(getIntent().getStringExtra("gameUid"));


        currentUser = this.getIntent().getStringExtra("currentUser");
        teamColor = this.getIntent().getStringExtra("teamColor");

        final Intent end = new Intent(GameActivity.this, ChooseGameModeActivity.class);
        end.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        end.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref.child("holding").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (loc == null && ContextCompat.checkSelfPermission(GameActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                            loc = LocationServices.FusedLocationApi.getLastLocation(myClient);

                        Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                                game.blueFlag.getLatitude(), game.blueFlag.getLongitude(), distance);
                        double distToBlue = distance[0];
                        Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                                game.redFlag.getLatitude(), game.redFlag.getLongitude(), distance);
                        double distToRed = distance[0];

                        boolean bfLatBool = snapshot.child("blueFlagLatitude").getValue(Double.class) != null;
                        boolean bfLongBool = snapshot.child("blueFlagLongitude").getValue(Double.class) != null;
                        boolean rfLatBool = snapshot.child("redFlagLatitude").getValue(Double.class) != null;
                        boolean rfLongBool = snapshot.child("redFlagLongitude").getValue(Double.class) != null;

                        if (teamColor.equals("blue")) { // blue
                            if (rfLatBool && rfLongBool) { // red flag held ->
                                if (distToBlue < RADIUS) { // blue capture red flag
                                    ref.child("holding").child("redFlagLatitude").removeValue();
                                    ref.child("holding").child("redFlagLongitude").removeValue();
                                    Toast.makeText(GameActivity.this, "Red flag captured! Game over!!!", Toast.LENGTH_LONG).show();
                                    startActivity(end);
                                }
                            } else if (bfLatBool && bfLongBool) { // blue flag held -> blue return blue flag
                                ref.child("holding").child("blueFlagLatitude").removeValue();
                                ref.child("holding").child("blueFlagLongitude").removeValue();
                                Toast.makeText(GameActivity.this, "Blue flag returned.", Toast.LENGTH_LONG).show();
                            } else { // blue take red flag
                                ref.child("holding").child("redFlagLatitude").setValue(loc.getLatitude());
                                ref.child("holding").child("redFlagLongitude").setValue(loc.getLongitude());
                                userHoldingFlag = true;
                                Toast.makeText(GameActivity.this, "Red flag taken.", Toast.LENGTH_LONG).show();
                            }
                        } else { // red
                            if (bfLatBool && bfLongBool) { // blue flag held ->
                                if (distToRed < RADIUS) { // red capture blue flag
                                    ref.child("holding").child("blueFlagLatitude").removeValue();
                                    ref.child("holding").child("blueFlagLongitude").removeValue();
                                    Toast.makeText(GameActivity.this, "Blue flag captured! Game over!!!", Toast.LENGTH_LONG).show();
                                    startActivity(end);
                                }
                            } else if (rfLatBool && rfLongBool) { // red flag held -> red return red flag
                                ref.child("holding").child("redFlagLatitude").removeValue();
                                ref.child("holding").child("redFlagLongitude").removeValue();
                                Toast.makeText(GameActivity.this, "Red flag returned.", Toast.LENGTH_LONG).show();
                                startActivity(end);
                            } else { // red take blue flag
                                ref.child("holding").child("blueFlagLatitude").setValue(loc.getLatitude());
                                ref.child("holding").child("blueFlagLongitude").setValue(loc.getLongitude());
                                userHoldingFlag = true;
                                Toast.makeText(GameActivity.this, "Blue flag taken.", Toast.LENGTH_LONG).show();
                            }
                        }

                        mCaptureButton.setVisibility(View.INVISIBLE);
                        mCaptureButton.setText(R.string.reset_capture_button);
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
                Log.d("Debug", "location found at: " + loc);
                if (reload) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationToLatLng(loc), ZOOM_LEVEL), 4000, null);
                    reload = false;
                }

                HashMap<String, Object> update = new HashMap<>();
                HashMap<String, Double> coords = new HashMap<>();
                coords.put("latitude", loc.getLatitude());
                coords.put("longitude", loc.getLongitude());
                update.put("locations", coords);
                update.put("teamColor", teamColor);

                ref.child("liveLocations").child(currentUser).updateChildren(update);

                if (userHoldingFlag) {
                    HashMap<String, Object> holdingCoords = new HashMap<>();

                    if (teamColor.equals("red")) {
                        holdingCoords.put("blueHoldingLatitude", loc.getLatitude());
                        holdingCoords.put("blueHoldingLongitude", loc.getLongitude());
                    } else {
                        holdingCoords.put("redHoldingLatitude", loc.getLatitude());
                        holdingCoords.put("redHoldingLongitude", loc.getLongitude());
                    }

                    ref.child("holding").updateChildren(holdingCoords);
                }
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

                Double rfLat = snapshot.child("redFlagLatitude").getValue(Double.class);
                Double rfLong = snapshot.child("redFlagLongitude").getValue(Double.class);
                Double bfLat = snapshot.child("blueFlagLatitude").getValue(Double.class);
                Double bfLong = snapshot.child("blueFlagLongitude").getValue(Double.class);

                if (rfLat != null && rfLong != null && bfLat != null && bfLong != null) {
                    mMap.clear();
                    HashMap<String, HashMap<String, Object>> liveLocationsMap =
                            (HashMap<String, HashMap<String, Object>>) snapshot.child("liveLocations").getValue();

                    Double rfhLat = snapshot.child("holding").child("redFlagLatitude").getValue(Double.class);
                    Double rfhLong = snapshot.child("holding").child("redFlagLongitude").getValue(Double.class);
                    Double bfhLat = snapshot.child("holding").child("blueFlagLatitude").getValue(Double.class);
                    Double bfhLong = snapshot.child("holding").child("blueFlagLongitude").getValue(Double.class);

                    boolean redHolding = rfhLat != null && rfhLong != null;
                    boolean blueHolding = bfhLat != null && bfhLong != null;

                    if (redHolding) {
                        rfLat = rfhLat;
                        rfLong = rfhLong;
                    }

                    if (blueHolding) {
                        bfLat = bfhLat;
                        bfLong = bfhLong;
                    }

                    game.redFlag = new Location(LocationManager.GPS_PROVIDER);
                    game.redFlag.setLatitude(rfLat);
                    game.redFlag.setLongitude(rfLong);

                    game.blueFlag = new Location(LocationManager.GPS_PROVIDER);
                    game.blueFlag.setLatitude(bfLat);
                    game.blueFlag.setLongitude(bfLong);

                    mMap.addMarker(new MarkerOptions()
                            .position(locationToLatLng(game.getRedFlagLocation())).title("Red Flag")
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.red_flag)));
                    mMap.addMarker(new MarkerOptions()
                            .position(locationToLatLng(game.getBlueFlagLocation())).title("Blue Flag")
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_flag)));

                    if (!redHolding) {
                        mMap.addCircle(new CircleOptions()
                                .center(locationToLatLng(game.getRedFlagLocation())).radius(RADIUS)
                                .strokeColor(Color.RED));
                    } else {
                        mMap.addMarker(new MarkerOptions().position(new LatLng(rfLat, rfLong))
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.red_base)));
                    }
                    if (!blueHolding) {
                        mMap.addCircle(new CircleOptions()
                                .center(locationToLatLng(game.getBlueFlagLocation())).radius(RADIUS)
                                .strokeColor(Color.BLUE));
                    } else {
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(bfLat, bfLong))
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_base)));
                    }

                    if (loc != null) {
                        Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                                bfLat, bfLong, distance);
                        if (distance[0] < RADIUS) { // blue distance
                            if (teamColor.equals("red") && !blueHolding) {
                                mCaptureButton.setText(R.string.capture_the_flag);
                                mCaptureButton.setVisibility(View.VISIBLE);
                            } else if (teamColor.equals("red") && blueHolding){
                                mCaptureButton.setText(R.string.return_the_flag);
                                mCaptureButton.setVisibility(View.VISIBLE);
                            }
                        }
                        Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                                rfLat, rfLong, distance);
                        if (distance[0] < RADIUS) { // red distance
                            if (teamColor.equals("blue") && !redHolding) {
                                mCaptureButton.setText(R.string.capture_the_flag);
                                mCaptureButton.setVisibility(View.VISIBLE);
                            } else if (teamColor.equals("red") && redHolding) {
                                mCaptureButton.setText(R.string.return_the_flag);
                                mCaptureButton.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    if (liveLocationsMap != null) {
                        for (String userName : liveLocationsMap.keySet()) {
                            String playerTeamColor = (String) liveLocationsMap.get(userName).get("teamColor");
                            if (playerTeamColor != null) {
                                HashMap<String, Double> locationsMap = (HashMap<String, Double>) liveLocationsMap.get(userName).get("locations");
                                LatLng playerLocation = new LatLng(locationsMap.get("latitude"), locationsMap.get("longitude"));
                                mMap.addMarker(new MarkerOptions().position(playerLocation).title(userName)
                                    .icon(BitmapDescriptorFactory.fromResource((playerTeamColor.equals("red") ?
                                        R.mipmap.red_marker : R.mipmap.blue_marker))));
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
                    if (loc != null)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationToLatLng(loc), ZOOM_LEVEL), 4000, null);
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
