package com.example.kevin.flagwars;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.KeyListener;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.Manifest;
import android.widget.ProgressBar;

import com.parse.FindCallback;
import com.parse.LocationCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class JoinGameActivity extends AppCompatActivity {

    ListView mGameListView;
    EditText mEnterCodeTextView;
    List<Game> gameList = null;
    ProgressBar mLoadGamesProgressBar;

    ParseGeoPoint loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        mGameListView = (ListView) findViewById(R.id.gameListView);
        mEnterCodeTextView = (EditText) findViewById(R.id.enterCodeTextView);
        mLoadGamesProgressBar = (ProgressBar) findViewById(R.id.game_progress_bar);

        mEnterCodeTextView.setCursorVisible(false);
        mEnterCodeTextView.setKeyListener(null);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            // ask for permission
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        else
            // has permission
            ParseGeoPoint.getCurrentLocationInBackground(1000, new ParseLocationCallback());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 0) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    ParseGeoPoint.getCurrentLocationInBackground(100, new ParseLocationCallback());
                }
            } else {
                System.out.println("Denied permission");
            }
        }
    }

    private void attemptToRetrieveObjects() {
        ParseQuery q = ParseQuery.getQuery("Game");
        q.whereWithinMiles("location", loc, 1.0);
        q.setLimit(10);
        q.findInBackground(new FindCallback() {
            @Override
            public void done(List objects, ParseException e) {
                if (e == null) {
                    objectRetrievalSucceeded((List<ParseObject>) objects);
                } else { // error
                    System.out.println(e.getLocalizedMessage());
                }
            }

            @Override
            public void done(Object o, Throwable t) {
                if (t == null) {
                    objectRetrievalSucceeded((List<ParseObject>) o);
                } else {
                    System.out.println(t.getLocalizedMessage());
                }
            }
        });
    }

    private void objectRetrievalSucceeded(List<ParseObject> objects) {
        gameList = Game.parseObjectsToGames(objects);
        List<String> gameNames = this.getGameNames();
        ArrayAdapter<String> arrayAdapter;
        if (gameNames.size() == 0)
            gameNames.add("No games near you.");
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gameNames);
        this.mGameListView.setAdapter(arrayAdapter);
        mLoadGamesProgressBar.setVisibility(View.GONE);
    }

    private List<String> getGameNames() {
        ArrayList<String> a = new ArrayList<>();

        for (Game g : gameList)
            if (g.visibility) {
                System.out.println(g.name);
                a.add(g.name);
            }

        return a;
    }

    class ParseLocationCallback implements LocationCallback {
        @Override
        public void done(ParseGeoPoint geoPoint, ParseException e){
            if (e == null) {
                loc = geoPoint;
            } else if (geoPoint == null) {
                loc = new ParseGeoPoint(37.422, -122.084);
            } else {
                e.printStackTrace();
            }

            attemptToRetrieveObjects();
        }
    }
}
