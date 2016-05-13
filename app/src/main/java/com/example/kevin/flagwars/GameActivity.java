package com.example.kevin.flagwars;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.LocationCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;

public class GameActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ParseGeoPoint loc;
    private Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        game = Game.getObjectFromParse(getIntent().getStringExtra("gameObjectId"));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ParseGeoPoint.getCurrentLocationInBackground(100, new LocationCallback() {
            @Override
            public void done(ParseGeoPoint geoPoint, ParseException e){
                if (e == null)
                    loc = geoPoint;
                else if (geoPoint == null)
                    loc = new ParseGeoPoint(37.422, -122.084);
                else
                    e.printStackTrace();

                LatLng currentLocation = parseGeoPointToLatLng(loc);
                mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            }
        });
        mMap.addMarker(new MarkerOptions()
                .position(parseGeoPointToLatLng(game.getRedFlagLocation()))
                .title("Red Flag")
                .draggable(false)
                .flat(true));
        mMap.addMarker(new MarkerOptions()
                .position(parseGeoPointToLatLng(game.getBlueFlagLocation()))
                .title("Blue Flag")
                .draggable(false)
                .flat(true));
    }

    private LatLng parseGeoPointToLatLng(ParseGeoPoint parseGeoPoint) {
        return new LatLng(parseGeoPoint.getLatitude(), parseGeoPoint.getLongitude());
    }
}
