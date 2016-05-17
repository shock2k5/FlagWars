package com.example.kevin.flagwars;

import android.location.Location;
import android.util.Log;

import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.HashMap;

public class Game {
    protected String name;
    protected HashMap<String, String> teamList;
    protected Location redFlag, blueFlag, anchorLocation = null;
    protected HashMap<String, HashMap<String, HashMap<String, Double>>> userMap = new HashMap<>();

    public String toString(){
        String str = "";
        str += "Name: " + this.name + "\n";
        str += "Red Team: " + teamList.toString() + "\n";
        return str;
    }

    public Game() {
        this.name = null;
        this.redFlag = null;
        this.blueFlag = null;
        teamList = new HashMap<>();
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

    public Location getLocation() { return this.anchorLocation; }

    public void setRedFlagLocation(Location loc) {
        this.redFlag = loc;
        if (this.anchorLocation == null)
            this.anchorLocation = loc;
    }

    public void setBlueFlagLocation(Location loc) {
        this.blueFlag = loc;
        if (this.anchorLocation == null)
            this.anchorLocation = loc;
    }

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
        ref.child("name").setValue(this.name);
        ref.child("started").setValue(false);
        HashMap<String, String> playerList = new HashMap<String, String>();
        ArrayList<String> red = getRedTeamNames();
        ArrayList<String> blue = getBlueTeamNames();
        for(String str : red){
            playerList.put(str, "red");
        }
        for(String str : blue){
            playerList.put(str, "blue");
        }
        ref.child("teamList").setValue(playerList);

        if (anchorLocation != null) {
            ref.child("anchorLocationLatitude").setValue(this.anchorLocation.getLatitude());
            ref.child("anchorLocationLongitude").setValue(this.anchorLocation.getLongitude());
        }

        if (redFlag != null) {
            ref.child("redFlagLatitude").setValue(this.redFlag.getLatitude());
            ref.child("redFlagLongitude").setValue(this.redFlag.getLongitude());
        }

        if (blueFlag != null) {
            ref.child("blueFlagLatitude").setValue(this.blueFlag.getLatitude());
            ref.child("blueFlagLongitude").setValue(this.blueFlag.getLongitude());
        }
    }
}