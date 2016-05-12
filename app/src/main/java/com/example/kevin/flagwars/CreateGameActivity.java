package com.example.kevin.flagwars;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class CreateGameActivity extends AppCompatActivity {
    EditText gameName;
    RadioGroup team;
    RadioButton selectedTeam;
    Button createGameButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        createGameButton = (Button) findViewById(R.id.create_game_start_game);

        //Set variables to the appropriate editText, ,etc.
        gameName = (EditText) findViewById(R.id.game_name_edit);
        team = (RadioGroup) findViewById(R.id.create_game_team_group);

        createGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedTeam = (RadioButton) findViewById(team.getCheckedRadioButtonId());
                Intent intent = new Intent(CreateGameActivity.this, Lobby.class);

                intent.putExtra("gamename", gameName.getText().toString());
                intent.putExtra("teamName", selectedTeam.getText().toString());
                startActivity(intent);
            }
        });
    }


}
