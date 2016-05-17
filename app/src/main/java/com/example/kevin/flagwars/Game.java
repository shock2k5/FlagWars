package com.example.kevin.flagwars;

import android.location.Location;

import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.HashMap;

public class Game {
    protected String name;
    protected HashMap<String, String> teamList;
    protected Location redFlag, blueFlag, anchorLocation = null;

    public String toString(){
        String str = "";
        str += "Name: " + this.name + "\n";
        str += "Red Team: " + teamList.toString() + "\n";
        return str;
    }

    public Game(String name) {
        this.name = name;
        this.redFlag = null;
        this.blueFlag = null;
        teamList = new HashMap<>();
    }

    public String getName() { return this.name; }

    public String getUid() { return this.name; }

    public Location getRedFlagLocation() {
        return this.redFlag;
    }

    public Location getBlueFlagLocation() { return this.blueFlag; }

    public ArrayList<String> getRedTeamNames() {
        ArrayList<String> red = new ArrayList<>();
        for(String key : this.teamList.keySet()){
            if(teamList.get(key).equals("red")){
                red.add(key);
            }
        }
        return red;
    }

    public ArrayList<String> getBlueTeamNames() {
        ArrayList<String> blue = new ArrayList<>();
        for(String key : this.teamList.keySet()){
            if(teamList.get(key).equals("blue")){
                blue.add(key);
            }
        }
        return blue;
    }


    public void switchRedtoBlue(final User user) {
        this.teamList.put(user.getName(), "blue");
        this.sendToFirebase();
    }

    public void switchBlueToRed(final User user) {
        this.teamList.put(user.getName(), "red");
        this.sendToFirebase();
    }

    public void sendToFirebase() {
        Firebase ref = ImportantMethods.getFireBase().child("Game").child(this.name);
        HashMap<String, Object> gameMap = new HashMap<>();

        HashMap<String, String> playerList = new HashMap<>();
        ArrayList<String> red = getRedTeamNames();
        ArrayList<String> blue = getBlueTeamNames();
        for(String str : red){
            playerList.put(str, "red");
        }
        for(String str : blue){
            playerList.put(str, "blue");
        }

        gameMap.put("name", this.name);
        gameMap.put("started", false);
        gameMap.put("teamList", playerList);

        if (anchorLocation != null) {
            gameMap.put("anchorLocationLatitude", this.anchorLocation.getLatitude());
            gameMap.put("anchorLocationLongitude", this.anchorLocation.getLongitude());
        }

        if (redFlag != null) {
            gameMap.put("redFlagLatitude", this.redFlag.getLatitude());
            gameMap.put("redFlagLongitude", this.redFlag.getLongitude());
        }

        if (blueFlag != null) {
            gameMap.put("blueFlagLatitude", this.blueFlag.getLatitude());
            gameMap.put("blueFlagLongitude", this.blueFlag.getLongitude());
        }

        ref.updateChildren(gameMap);
    }
}