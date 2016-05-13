package com.example.kevin.flagwars;

import com.facebook.FacebookSdk;
import com.firebase.client.Firebase;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;

import android.app.Application;

/**
 * Created by Adi on 4/11/16.
 */
public class FlagWarsApplication extends Application {
    Firebase fireRef;

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this.getApplicationContext());
        fireRef = new Firebase("https://flagwar.firebaseio.com/");


        //Parse.initialize(this, "iEVgiEIkebbBlikxwCSSEPZnepg0khNnUXbhoPwy", "JIj1LA0LQQTHzq6vd8nB5FyTdKbCxcvfxfmF1qL1");
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        //ParseFacebookUtils.initialize(this.getApplicationContext());
    }
}
