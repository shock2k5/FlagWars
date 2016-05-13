package com.example.kevin.flagwars;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    Button btnJoinRedTeam, btnJoinBlueTeam, btnStartGameTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        game = Game.getObjectFromParse(getIntent().getStringExtra("gameObjectId"));
        gameName = (TextView) findViewById(R.id.LobbyGameName);
        btnJoinRedTeam = (Button) findViewById(R.id.btnJoinRed);
        btnJoinRedTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser user = ParseUser.getCurrentUser();
                if (game.getRedTeam().contains(user)) {
                    return;
                }
                game.removeFromBlueTeam(user);
                game.addToRedTeam(user);
                updateTeamLists();
                game.saveInParse();
            }
        });
        btnJoinBlueTeam = (Button) findViewById(R.id.btnJoinBlue);
        btnJoinBlueTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser user = ParseUser.getCurrentUser();
                if (game.getBlueTeam().contains(user)){
                    return;
                }
                game.removeFromRedTeam(user);
                game.addToBlueTeam(user);
                updateTeamLists();
                game.saveInParse();
            }
        });

        game = Game.getObjectFromParse(getIntent().getStringExtra("gameObjectId"));
        gameName.setText(game.getName());

        redRoster = (ListView) findViewById(R.id.redRosterList);
        blueRoster = (ListView) findViewById(R.id.blueRosterList);
        redAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, game.getRedTeamNames());
        blueAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, game.getBlueTeamNames());
        redRoster.setAdapter(redAdapter);
        blueRoster.setAdapter(blueAdapter);

    }

    public void updateTeamLists(){
        redAdapter.clear();
        for(ParseUser user : this.game.getRedTeam()){
            redAdapter.add(user.getUsername());
        }
        blueAdapter.clear();
        for(ParseUser user : this.game.getBlueTeam()){
            blueAdapter.add(user.getUsername());
        }
    }
}
