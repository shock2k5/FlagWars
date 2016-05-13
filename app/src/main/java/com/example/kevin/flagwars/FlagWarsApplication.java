package com.example.kevin.flagwars;

import com.facebook.FacebookSdk;
import com.firebase.client.Firebase;

import android.app.Application;

/**
 * Created by Adi on 4/11/16.
 */
public class FlagWarsApplication extends Application {
<<<<<<< HEAD
    Firebase fireRef;

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this.getApplicationContext());
        fireRef = new Firebase("https://flagwar.firebaseio.com/");
=======
    @Override
    public void onCreate() {
        super.onCreate();
>>>>>>> origin/master

        FacebookSdk.sdkInitialize(this.getApplicationContext());
    }
}
