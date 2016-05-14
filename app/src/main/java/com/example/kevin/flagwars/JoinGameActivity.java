package com.example.kevin.flagwars;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.Manifest;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JoinGameActivity extends AppCompatActivity {

    protected AdapterView mGameListView;
    protected EditText mEnterCodeTextView;
    protected List<Game> gameList = new ArrayList<>();
    protected ProgressBar mLoadGamesProgressBar;
    protected Location loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        mGameListView = (AdapterView) findViewById(R.id.gameListView);
        mEnterCodeTextView = (EditText) findViewById(R.id.enterCodeTextView);
        mLoadGamesProgressBar = (ProgressBar) findViewById(R.id.game_progress_bar);

        mEnterCodeTextView.setCursorVisible(false);
        mEnterCodeTextView.setKeyListener(null);

        Firebase.setAndroidContext(this.getApplicationContext());
        final Firebase ref = new Firebase("https://flagwar.firebaseio.com/").child("Game");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // TODO update it to allow for calling all objects in a "class"
                Map<String, ?> games = dataSnapshot.getValue(Map.class);
                for (String key : games.keySet()) {
                    ref.child(key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            String name = snapshot.child("name").getValue(String.class);
                            int numPlayers = Integer.parseInt(snapshot.child("numPlayers").getValue(String.class));
                            Collection<User> red = (ArrayList<User>) snapshot.child("redTeam").getValue();
                            Collection<User> blue = snapshot.child("blueTeam").getValue(new GenericTypeIndicator<ArrayList<User>>() {});
                            ArrayList<User> redTeam = (red == null) ? new ArrayList<User>() : new ArrayList<>(red);
                            ArrayList<User> blueTeam = (blue == null) ? new ArrayList<User>() : new ArrayList<>(blue);

                            Location anchorLocation, redFlag, blueFlag;
                            if (snapshot.child("anchorLocationLatitude").getValue() != null) {
                                anchorLocation = new Location(LocationManager.GPS_PROVIDER);
                                anchorLocation.setLatitude(snapshot.child("anchorLocationLatitude").getValue(Double.class));
                                anchorLocation.setLongitude(snapshot.child("anchorLocationLongitude").getValue(Double.class));
                            } else {
                                anchorLocation = null;
                            }

                            if (snapshot.child("redFlagLatitude").getValue() != null) {
                                redFlag = new Location(LocationManager.GPS_PROVIDER);
                                redFlag.setLatitude(snapshot.child("redFlagLatitude").getValue(Double.class));
                                redFlag.setLongitude(snapshot.child("redFlagLongitude").getValue(Double.class));
                            } else {
                                redFlag = null;
                            }

                            if (snapshot.child("blueFlagLatitude").getValue(Double.class) != null) {
                                blueFlag = new Location(LocationManager.GPS_PROVIDER);
                                blueFlag.setLatitude(snapshot.child("blueFlagLatitude").getValue(Double.class));
                                blueFlag.setLongitude(snapshot.child("blueFlagLongitude").getValue(Double.class));
                            } else {
                                blueFlag = null;
                            }

                            Game game = new Game(name, numPlayers);
                            game.anchorLocation = anchorLocation;
                            game.redFlag = redFlag;
                            game.blueFlag = blueFlag;

                            if (game == null)
                                Log.e("Failure", "Null object retrieved from Firebase", new NullPointerException());
                            gameList.add(game);
                            List<String> gameNames = new ArrayList<>();
                            for (Game g : gameList)
                                gameNames.add(g.getName());

                            if (gameNames.size() == 0) {
                                gameNames.add("No games near you.");
                                mGameListView.setClickable(false);
                            }

                            mGameListView.setAdapter(new ArrayAdapter<>(JoinGameActivity.this, android.R.layout.simple_list_item_1, gameNames));
                            mLoadGamesProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            System.err.println("There was an error getting the Game from Firebase: " + firebaseError);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("Firebase failure", "Error in retrieving object from Firebase in JoinGameActivity", firebaseError.toException());
            }
        });

        loc = ImportantMethods.getCurrentLocation(this);

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
                                Intent intent = new Intent(JoinGameActivity.this, Lobby.class);
                                intent.putExtra("gameUid", game.getUid());
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
                    // TODO get current location
                    loc = null;
            } else {
                Toast.makeText(this.getApplicationContext(),
                    "Permission needs to be granted for this application", Toast.LENGTH_LONG).show();
            }
        }
    }
}
