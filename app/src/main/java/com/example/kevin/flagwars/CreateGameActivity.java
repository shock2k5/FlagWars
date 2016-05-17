package com.example.kevin.flagwars;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

public class CreateGameActivity extends AppCompatActivity {
    protected EditText gameName; // radio0 is Red, radio1 is Blue
    protected Button createGameButton;
    protected Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);
        Firebase.setAndroidContext(getApplicationContext());

        createGameButton = (Button) findViewById(R.id.create_game_start_game);
        gameName = (EditText) findViewById(R.id.game_name_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //toolbar.setNavigationIcon(getDrawable(R.drawable.ic_action_back));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CreateGameActivity.this, ChooseGameModeActivity.class);
                startActivity(i);
            }
        });

        createGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = gameName.getText().toString();
                if(name == null || name.isEmpty()){
                    Toast.makeText(CreateGameActivity.this, "Please enter a Game Name.", Toast.LENGTH_LONG).show();

                }else{
                    int numPlayers = 4;

                    game = new Game(name, numPlayers);
                    game.sendToFirebase();
                    ImportantMethods.getFireBase().child("User").child(ImportantMethods.getFireBase().getAuth().getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    HashMap<String, ?> map = (HashMap<String, ?>) dataSnapshot.getValue();
                                    User currentUser = new User((String) map.get("username"));
                                    game.switchBlueToRed(currentUser);
                                    Intent intent = new Intent(CreateGameActivity.this, Lobby.class);
                                    intent.putExtra("gameUid", game.getUid());
                                    intent.putExtra("creator", currentUser.getName());
                                    startActivity(intent);
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                    Log.e("Firebase error", "CreateGameActivity setOnClickListener createGameButton", firebaseError.toException());
                                }
                            });
                }
            }
        });
    }
}
