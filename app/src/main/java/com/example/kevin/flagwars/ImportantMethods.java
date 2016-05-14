package com.example.kevin.flagwars;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by E&D on 5/13/2016.
 */
public class ImportantMethods {
    private static Firebase fireRef = ImportantMethods.getFireBase();
    private static User user;
    public static Firebase getFireBase(){
        return new Firebase("https://flagwar.firebaseio.com/");
    }
    public static Firebase getFireBase(String key) {
        if(key.equals("Game")) {
            return new Firebase("https://flagwar.firebaseio.com/Game/");
        } else if(key.equals("User")){
            return new Firebase("https://flagwar.firebaseio.com/Game/User/");
        }
        return new Firebase("https://flagwar.firebaseio.com/Game/");
    }


    public static String emailToUsername(String str){
        return str.substring(0, str.indexOf("@"));
    }

    public static void addNewUser(User user){
        fireRef.child("User").child(fireRef.getAuth().getUid()).setValue(user);

    }

    public static User getCurrentUser(){
        AuthData authUser = fireRef.getAuth();
        String uid = "";
        if (authUser == null) {
            return new User();
        }
        uid = authUser.getUid();

        fireRef.child("User/" + uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, ?> map = (HashMap<String, ?>) dataSnapshot.getValue();
                user = new User((String) map.get("username"));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                user = null;
            }
        });

        return user;
    }

    public static String getUserName(){
        String uid = fireRef.getAuth().getUid();
        fireRef.child("User/" + uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        return (user.name.indexOf('.') == -1) ? user.name : user.name.substring(0, user.name.indexOf('@'));
    }
    public static Location getCurrentLocation(Activity a) {

        // Enable MyLocation Layer of Google Map
        if (ContextCompat.checkSelfPermission(a, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // ask for permission
            ActivityCompat.requestPermissions(a, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 0);
        }

        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) a.getSystemService(Context.LOCATION_SERVICE);


        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        // Get Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);
        if (myLocation == null) {
            myLocation = new Location(LocationManager.GPS_PROVIDER);
            myLocation.setLongitude(-122.083);
            myLocation.setLatitude(37.453);
        }
        return myLocation;

    }
    /*
    // TODO DOESNT WORK
    public static Location getCurrentLocation(Activity a) {
        if (ContextCompat.checkSelfPermission(a, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // ask for permission
            ActivityCompat.requestPermissions(a, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 0);
        }

        if (ContextCompat.checkSelfPermission(a, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // has permission
            LocationManager locationManager = (LocationManager) a.getSystemService(Context.LOCATION_SERVICE);
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else {
            Toast.makeText(a.getApplicationContext(), "Permission needs to be granted for this application", Toast.LENGTH_LONG);
            return null;
        }
    }*/
    public static Game getGameFromFirebase(String uid) {
        final Firebase ref = ImportantMethods.getFireBase().child("Game").child(uid);
        final Game game = new Game();
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

                game.name = name;
                game.numPlayers = numPlayers;
                game.anchorLocation = anchorLocation;
                game.teamList = teamList;
                game.redFlag = redFlag;
                game.blueFlag = blueFlag;
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.err.println("There was an error getting the Game from Firebase: " + firebaseError);
            }
        });

        return game;
    }
}
