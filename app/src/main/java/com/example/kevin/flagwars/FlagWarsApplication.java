package com.example.kevin.flagwars;

import com.facebook.FacebookSdk;

import android.app.Application;

/**
 * Created by Adi on 4/11/16.
 */
public class FlagWarsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FacebookSdk.sdkInitialize(this.getApplicationContext());
    }
}
