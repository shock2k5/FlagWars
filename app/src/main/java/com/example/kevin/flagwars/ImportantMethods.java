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

    public static Firebase getFireBase(){
        return new Firebase("https://flagwar.firebaseio.com/");
    }

    public static String emailToUsername(String str){
        return str.substring(0, str.indexOf("@"));
    }

    public static void addNewUser(User user){
        fireRef.child("User").child(fireRef.getAuth().getUid()).setValue(user);
    }
}
