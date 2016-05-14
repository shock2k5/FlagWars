package com.example.kevin.flagwars;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Adi on 4/27/16.
 */

public class Game {
    protected String name;
    protected int numPlayers;
    protected HashMap<String, String> teamList;
    protected Location redFlag, blueFlag, anchorLocation = null;
    Firebase fireRef;


    public String toString(){
        String str = "";
        str += "Name: " + this.name + "\n";
        str += "Number of Players: " + numPlayers + "\n";
        str += "Red Team: " + teamList.toString() + "\n";
        return str;
    }

    public Game() {
        this.name = null;
        this.numPlayers = -1;
        this.redFlag = null;
        this.blueFlag = null;
        teamList = new HashMap<String, String>();
        teamList.put(ImportantMethods.getCurrentUser().getName(), "red");
    }

    public Game(String name, int numPlayers) {
        this.name = name;
        this.numPlayers = numPlayers;
        this.redFlag = null;
        this.blueFlag = null;
        teamList = new HashMap<String, String>();
        teamList.put(ImportantMethods.getUserName(), "red");
    }

    public String getName() {
        return this.name;
    }

    public int getNumPlayers() {
        return this.numPlayers;
    }

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

    public void addToRedTeam(final User user) {
        final Firebase fireRef = ImportantMethods.getFireBase().child("Game/" + this.name + "/teamList/");
        fireRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> teamList = (HashMap<String, String>) dataSnapshot.getValue();
                if(teamList == null) teamList = new HashMap<String, String>();
                if(teamList.get(user.getName()) == null){
                    return;
                }
                teamList.put(user.getName(), "red");
                fireRef.setValue(teamList);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void addToBlueTeam(final User user) {
        final Firebase fireRef = ImportantMethods.getFireBase().child("Game/" + this.name + "/teamList/");
        fireRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> teamList = (HashMap<String, String>) dataSnapshot.getValue();
                if(teamList.get(user.getName()) == null){
                    return;
                }
                teamList.put(user.getName(), "red");
                fireRef.setValue(teamList);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public boolean removeFromRedTeam(User user) {
       //TODO
        return false;
    }

    public void removeFromBlueTeam(final User user) {

        final Firebase fireRef = ImportantMethods.getFireBase().child("Game/" + this.name + "/blueTeam/");
        fireRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> teamList = (HashMap<String, String>) dataSnapshot.getValue();
                if(teamList.get(user.getName()) == null){
                    return;
                }
                teamList.remove(user.getName());
                fireRef.setValue(teamList);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void sendToFirebase() {
        Firebase ref = ImportantMethods.getFireBase().child("Game").child(this.getUid());
        ref.child("name").setValue(this.name);
        ref.child("numPlayers").setValue(this.numPlayers);
        ref.child("redTeam").setValue(this.getRedTeamNames());
        ref.child("blueTeam").setValue(this.getBlueTeamNames());

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

    /******************** STATIC METHODS ********************/

    public static Game getFromFirebase(String uid) {
        final Firebase ref = ImportantMethods.getFireBase().child("Game").child(uid);
        final Game game = new Game();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                game.name = snapshot.child("name").getValue(String.class);
                game.numPlayers = snapshot.child("numPlayers").getValue(Integer.class);

                if (snapshot.child("anchorLocationLatitude").getValue() == null) {
                    game.anchorLocation = new Location(LocationManager.GPS_PROVIDER);
                    game.anchorLocation.setLatitude(snapshot.child("anchorLocationLatitude").getValue(Double.class));
                    game.anchorLocation.setLongitude(snapshot.child("anchorLocationLongitude").getValue(Double.class));
                } else {
                    game.anchorLocation = null;
                }

                if (snapshot.child("redFlagLatitude").getValue() == null) {
                    game.redFlag = new Location(LocationManager.GPS_PROVIDER);
                    game.redFlag.setLatitude(snapshot.child("redFlagLatitude").getValue(Double.class));
                    game.redFlag.setLongitude(snapshot.child("redFlagLongitude").getValue(Double.class));
                } else {
                    game.redFlag = null;
                }

                if (snapshot.child("blueFlagLatitude").getValue(Double.class) == null) {
                    game.blueFlag = new Location(LocationManager.GPS_PROVIDER);
                    game.blueFlag.setLatitude(snapshot.child("blueFlagLatitude").getValue(Double.class));
                    game.blueFlag.setLongitude(snapshot.child("blueFlagLongitude").getValue(Double.class));
                } else {
                    game.blueFlag = null;
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.err.println("There was an error getting the Game from Firebase: " + firebaseError);
            }
        });
        return game;
    }

}