package com.example.kevin.flagwars;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinGameActivity extends AppCompatActivity {

    protected AdapterView mGameListView;
    protected EditText mEnterCodeTextView;
    protected List<Game> gameList;
    protected ProgressBar mLoadGamesProgressBar;

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
                Map<String, ?> games = (HashMap<String, ?>) dataSnapshot.getValue();
                if (games == null) games = new HashMap<String, Object>();
                gameList = new ArrayList<>();
                for (String key : games.keySet()) {
                    DataSnapshot snapshot = dataSnapshot.child(key);
                    String name = snapshot.child("name").getValue(String.class);
                    HashMap<String, String> teamList = (HashMap<String, String>) snapshot.child("teamList").getValue();
                    if (teamList == null) teamList = new HashMap<>();

                    Game game = new Game(name);
                    game.teamList = teamList;
                    gameList.add(game);
                }

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
                Log.e("Firebase failure", "Error in retrieving object from Firebase in JoinGameActivity", firebaseError.toException());
            }
        });

        mGameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (gameList.size() > 0) {
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
            }
        });
    }
}
