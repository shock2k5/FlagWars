package com.example.kevin.flagwars;

import com.facebook.FacebookSdk;

import android.app.Application;

/**
 * Created by Adi on 4/11/16.
 */
public class FlagWarsApplication extends Application {
<<<<<<< HEAD
<<<<<<< HEAD
    Firebase fireRef;
=======
>>>>>>> parent of 302a925... no changes

    @Override
    public void onCreate() {
        super.onCreate();
<<<<<<< HEAD
        Firebase.setAndroidContext(this.getApplicationContext());
        fireRef = new Firebase("https://flagwar.firebaseio.com/");
=======

>>>>>>> parent of 302a925... no changes
=======
    @Override
    public void onCreate() {
        super.onCreate();
>>>>>>> origin/master

        FacebookSdk.sdkInitialize(this.getApplicationContext());
    }
}
