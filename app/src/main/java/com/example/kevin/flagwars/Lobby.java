package com.example.kevin.flagwars;

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
    Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        gameName = (TextView) findViewById(R.id.LobbyGameName);
        game = Game.getObjectFromParse(getIntent().getStringExtra("gameObjectId"));
        gameName.setText(game.getName());

        redRoster = (ListView) findViewById(R.id.redRosterList);
        blueRoster = (ListView) findViewById(R.id.blueRosterList);
        redAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, redTeam);
        blueAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, blueTeam);

        redRoster.setAdapter(redAdapter);
        blueRoster.setAdapter(blueAdapter);

        redTeam = game.getRedTeamNames();
        blueTeam = game.getBlueTeamNames();
    }
}
