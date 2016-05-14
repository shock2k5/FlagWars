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
        Firebase ref = new Firebase("https://flagwar.firebaseio.com/");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Game> games = dataSnapshot.child("Game").getValue(Map.class);

                for (Game g : games.values())
                    gameList.add(g);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        List<String> gameNames = new ArrayList<>();
        for (Game g : gameList)
            gameNames.add(g.getName());

        if (gameNames.size() == 0) {
            gameNames.add("No games near you.");
            this.mGameListView.setClickable(false);
        }

        this.mGameListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gameNames));
        mLoadGamesProgressBar.setVisibility(View.GONE);

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
