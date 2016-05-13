package com.example.kevin.flagwars;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by E&D on 5/13/2016.
 */
public class ImportantMethods {
    private static Firebase fireRef = ImportantMethods.getFireBase();
    private static User user;

    public static Firebase getFireBase(){
        return new Firebase("https://flagwar.firebaseio.com/");
    }

    public static void addNewUser(User user){

    }

    public static User getCurrentUser(){
        AuthData authUser = fireRef.getAuth();
        String uid = null;
        if (authUser == null) {
            return null;
        } else {
            uid = authUser.getUid();
        }
        fireRef.child("User/" + uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                user = null;
            }
        });
        return user;
    }

    public static String getUserName(){
        Firebase fireRef = ImportantMethods.getFireBase();
        fireRef = new Firebase("https://flagwar.firebaseio.com/");
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
        if(user == null) return "";
        return user.username;
    }

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
    }
}
