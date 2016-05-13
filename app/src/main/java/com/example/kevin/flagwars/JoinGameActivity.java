package com.example.kevin.flagwars;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.Manifest;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LocationCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class JoinGameActivity extends AppCompatActivity {

    protected AdapterView mGameListView;
    protected EditText mEnterCodeTextView;
    protected List<Game> gameList = null;
    protected ProgressBar mLoadGamesProgressBar;
    protected ParseGeoPoint loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        mGameListView = (AdapterView) findViewById(R.id.gameListView);
        mEnterCodeTextView = (EditText) findViewById(R.id.enterCodeTextView);
        mLoadGamesProgressBar = (ProgressBar) findViewById(R.id.game_progress_bar);

        mEnterCodeTextView.setCursorVisible(false);
        mEnterCodeTextView.setKeyListener(null);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            // ask for permission
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        else
            // has permission
            ParseGeoPoint.getCurrentLocationInBackground(100, new ParseLocationCallback());

        mGameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Game game = gameList.get(position);
                new AlertDialog.Builder(JoinGameActivity.this)
                        .setTitle("Join game")
                        .setMessage("Are you sure you want to join " + game.getName() + "?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                System.out.println("Move to Lobby " + game.getObjectId());
                                Intent intent = new Intent(JoinGameActivity.this, Lobby.class);
                                intent.putExtra("gameObjectId", game.getObjectId());
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 0) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    ParseGeoPoint.getCurrentLocationInBackground(100, new ParseLocationCallback());
            } else {
                Toast.makeText(this.getApplicationContext(), "Permission needs to be granted for this application", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void attemptToRetrieveObjects() {
        ParseQuery q = ParseQuery.getQuery("Game");
        q.whereWithinMiles("anchorLocation", loc, 1.0);
        q.setLimit(10);
        q.findInBackground(new FindCallback() {
            @Override
            public void done(List objects, ParseException e) {
                if (e == null)
                    objectRetrievalSucceeded((List<ParseObject>) objects);
                else // error
                    e.printStackTrace();
            }

            @Override
            public void done(Object o, Throwable t) {
                if (t == null)
                    objectRetrievalSucceeded((List<ParseObject>) o);
                else
                    t.printStackTrace();
            }
        });
    }

    private void objectRetrievalSucceeded(List<ParseObject> objects) {
        gameList = Game.getGameListFromParse(objects);
        List<String> gameNames = this.getGameNames();
        ArrayAdapter<String> arrayAdapter;
        if (gameNames.size() == 0) {
            gameNames.add("No games near you.");
            this.mGameListView.setClickable(false);
        }
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gameNames);
        this.mGameListView.setAdapter(arrayAdapter);
        mLoadGamesProgressBar.setVisibility(View.GONE);
    }

    private List<String> getGameNames() {
        ArrayList<String> a = new ArrayList<>();
        for (Game g : gameList)
            a.add(g.getName());
        return a;
    }

    class ParseLocationCallback implements LocationCallback {
        @Override
        public void done(ParseGeoPoint geoPoint, ParseException e){
            if (e == null)
                loc = geoPoint;
            else if (geoPoint == null)
                loc = new ParseGeoPoint(37.422, -122.084);
            else
                e.printStackTrace();

            attemptToRetrieveObjects();
        }
    }
}
