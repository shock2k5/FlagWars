package com.example.kevin.flagwars;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.List;

public class JoinGameActivity extends AppCompatActivity {

    ListView mGameListView;
    TextView mEnterCodeTextView;
    List<Game> gameList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        mGameListView = (ListView) findViewById(R.id.gameListView);
        mEnterCodeTextView = (TextView) findViewById(R.id.enterCodeTextView);

        ParseGeoPoint currentLocation = ParseGeoPoint.getCurrentLocationInBackground(100).getResult();
        ParseQuery q = ParseQuery.getQuery("Game");
        q.whereWithinMiles("location", currentLocation, 2.0);
        q.setLimit(10);
        System.out.println(currentLocation.toString() + "\nAbout to be done");
        q.findInBackground(new FindCallback() {
            @Override
            public void done(List objects, ParseException e) {
                if (e == null) {
                    System.out.println("Success");
                    objectRetrievalSucceeded(objects);
                } else { // error
                    System.out.println(e.getLocalizedMessage());
                }
            }

            @Override
            public void done(Object o, Throwable t) {
                if (t == null) {
                    System.out.println(o);
                } else {
                    System.out.println(t.getLocalizedMessage());
                }
            }
        });
    }

    private void objectRetrievalSucceeded(List objects) {
        gameList = objects;
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Game.getGameNames(gameList));
        this.mGameListView.setAdapter(arrayAdapter);
        System.out.println("Ended");
    }
}
