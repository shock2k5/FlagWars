package com.example.kevin.flagwars;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class GameActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location loc;
    private Game game;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final String uid = getIntent().getStringExtra("gameUid");
        final String teamColor = getIntent().getStringExtra("teamColor");
        Firebase.setAndroidContext(this.getApplicationContext());
        final Firebase ref = ImportantMethods.getFireBase().child("Game").child(uid);
        currentUser = ImportantMethods.getCurrentUser();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 0);
        }

        final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        final Criteria criteria = new Criteria();

        String provider = locationManager.getBestProvider(criteria, true);
        loc = locationManager.getLastKnownLocation(provider);
        if (loc == null) {
            loc = new Location(provider);
            loc.setLatitude(38.985933);
            loc.setLongitude(-76.942792);
        }
        Firebase childLocationsRef = ref.child("liveLocations").child(currentUser.getName());
        childLocationsRef.child("teamColor").setValue(teamColor);
        childLocationsRef.child("locations").child("latitude").setValue(loc.getLatitude());
        childLocationsRef.child("locations").child("longitude").setValue(loc.getLongitude());


        locationManager.requestLocationUpdates(provider, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                loc = location;
                Log.d("Debug", "Location changed to: " + location.toString());
                ref.child("liveLocations").child(currentUser.getName()).child("locations").child("latitude").setValue(loc.getLatitude());
                ref.child("liveLocations").child(currentUser.getName()).child("locations").child("longitude").setValue(loc.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // loc.setProvider(provider);
            }

            @Override
            public void onProviderEnabled(String p) {
                loc.setProvider(p);
            }

            @Override
            public void onProviderDisabled(String p) {
                loc.setProvider(locationManager.getBestProvider(criteria, true));
            }
        });

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                int numPlayers = snapshot.child("numPlayers").getValue(Integer.class);
                HashMap<String, String> teamList = (HashMap<String, String>) snapshot.child("teamList").getValue();
                if (teamList == null) teamList = new HashMap<>();

                HashMap<String, HashMap<String, Object>> liveLocationsMap =
                        (HashMap<String, HashMap<String, Object>>) snapshot.child("liveLocations").getValue();

                Location redFlag = null, blueFlag = null;
                game = new Game(name, numPlayers);
                game.teamList = teamList;

                LatLng currentLocation = locationToLatLng(loc);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16.0f), 4000, null);
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
                    mMap.clear();

                    mMap.addMarker(new MarkerOptions()
                            .position(locationToLatLng(game.getRedFlagLocation()))
                            .title("Red Flag")
                            .draggable(false));
                    mMap.addMarker(new MarkerOptions()
                            .position(locationToLatLng(game.getBlueFlagLocation()))
                            .title("Blue Flag")
                            .draggable(false));
                }

                for (String userName : liveLocationsMap.keySet()) {
                    String teamColor = (String) liveLocationsMap.get(userName).get("teamColor");
                    LatLng userLocation = new LatLng(
                            (Double) ((HashMap<String, Double>) liveLocationsMap.get(userName).get("locations")).get("latitude"),
                            (Double) ((HashMap<String, Double>) liveLocationsMap.get(userName).get("locations")).get("longitude"));
                    mMap.addMarker(new MarkerOptions()
                            .position(userLocation)
                            .title(teamColor+" "+userName));
                }
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }

    private LatLng locationToLatLng(Location loc) {
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }
}
