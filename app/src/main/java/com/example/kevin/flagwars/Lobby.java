package com.example.kevin.flagwars;

import android.content.Intent;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class Lobby extends AppCompatActivity {
    ArrayList<String> redTeam, blueTeam;
    TextView gameName;
    ArrayAdapter<String> redAdapter, blueAdapter;
    ListView redRoster, blueRoster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        gameName = (TextView) findViewById(R.id.LobbyGameName);
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
        if(previousIntent.getStringExtra("teamname").equals("Red Team")){
            //TODO Add current player to the Red Team
            redTeam.add("Player");
        } else if(previousIntent.getStringExtra("teamname").equals("Blue Team")){
            //TODO Add current player to the Blue Team
            blueTeam.add("Player");
        }

    }
}
