package com.example.kevin.flagwars;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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

    private LatLng locationToLatLng(Location loc) {
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }
}
