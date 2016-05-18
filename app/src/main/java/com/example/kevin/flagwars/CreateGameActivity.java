package com.example.kevin.flagwars;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

@SuppressWarnings("unchecked")
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

        buttonEffect(createGameButton);

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
                if(name.isEmpty()){
                    Toast.makeText(CreateGameActivity.this, "Please enter a Game Name.", Toast.LENGTH_LONG).show();
                } else {
                    game = new Game(name);
                    game.sendToFirebase();
                    final Firebase ref = ImportantMethods.getFireBase();
                    ref.child("User").child(ImportantMethods.getFireBase().getAuth().getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    HashMap<String, ?> map = (HashMap<String, ?>) dataSnapshot.getValue();
                                    User currentUser = new User((String) map.get("username"));
                                    game.switchBlueToRed(currentUser);
                                    ref.child("Game").child(game.getUid()).child("creator").setValue(currentUser.getName());
                                    Intent intent = new Intent(CreateGameActivity.this, Lobby.class);
                                    intent.putExtra("gameUid", game.getUid());
                                    startActivity(intent);
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                    Log.e("Firebase error", "CreateGameActivity createGameButton", firebaseError.toException());
                                }
                            });
                    }
            }
        });
    }

    public static void buttonEffect(View button){
        button.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.getBackground().clearColorFilter();
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }
}
