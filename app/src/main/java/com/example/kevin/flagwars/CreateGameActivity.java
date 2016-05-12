package com.example.kevin.flagwars;

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

public class CreateGameActivity extends AppCompatActivity {
    EditText email;
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
        email = (EditText) findViewById(R.id.game_name_edit);
        team = (RadioGroup) findViewById(R.id.create_game_team_group);
    }

    public void onClickCreateGame(View v){
        ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder.from(this);
        intentBuilder.setSubject("gamename");
        intentBuilder.setText(email.getText().toString());
        intentBuilder.setSubject("team");
        selectedTeam = (RadioButton) findViewById(team.getCheckedRadioButtonId());
        intentBuilder.setText(selectedTeam.getText().toString());
        startActivity(intentBuilder.getIntent());
    }

}
