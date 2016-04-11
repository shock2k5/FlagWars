package com.example.kevin.flagwars;

import com.parse.Parse;
import android.app.Application;

/**
 * Created by Adi on 4/11/16.
 */
public class FlagWarsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, "iEVgiEIkebbBlikxwCSSEPZnepg0khNnUXbhoPwy", "JIj1LA0LQQTHzq6vd8nB5FyTdKbCxcvfxfmF1qL1");
    }
}
