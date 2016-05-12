package com.example.kevin.flagwars;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.ArrayList;

public class Lobby extends AppCompatActivity {
    ArrayList<String> redTeam, blueTeam;
    TextView gameName;
    ArrayAdapter<String> redAdapter, blueAdapter;
    ListView redRoster, blueRoster;
    ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        currentUser = ParseUser.getCurrentUser();
        gameName = (TextView) findViewById(R.id.LobbyGameName);
        gameName.setText(getIntent().getStringExtra("gamename"));
        redTeam = new ArrayList<String>();
        blueTeam = new ArrayList<String>();
        redRoster = (ListView) findViewById(R.id.redRosterList);
        blueRoster = (ListView) findViewById(R.id.blueRosterList);
        redAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, redTeam);
        blueAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, blueTeam);
        redRoster.setAdapter(redAdapter);
        blueRoster.setAdapter(blueAdapter);

        Intent previousIntent = getIntent();
        gameName.setText(previousIntent.getStringExtra("gamename"));

        //Put current player on the right team
        if(previousIntent.getStringExtra("teamName").equals("Red Team")){
            //TODO Get the current players name from the database
            redTeam.add("Current Player");
        } else if(previousIntent.getStringExtra("teamName").equals("Blue Team")){
            //TODO Get the current players name from the database
            blueTeam.add("Current Player");
        }

    }
}
