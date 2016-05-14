package com.example.kevin.flagwars;

import android.location.Location;
import android.location.LocationManager;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Adi on 4/27/16.
 */

public class Game {
    protected String name;
    protected int numPlayers;
    protected ArrayList<User> redTeam, blueTeam;
    protected Location redFlag, blueFlag, anchorLocation = null;

    public Game() {
        this.name = null;
        this.numPlayers = -1;
        this.redFlag = null;
        this.blueFlag = null;
        this.redTeam = new ArrayList<>();
        this.blueTeam = new ArrayList<>();
    }

    public Game(String name, int numPlayers) {
        this.name = name;
        this.numPlayers = numPlayers;
        this.redFlag = null;
        this.blueFlag = null;
        this.redTeam = new ArrayList<>();
        this.blueTeam = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public int getNumPlayers() {
        return this.numPlayers;
    }

    public ArrayList<User> getRedTeam() {
        return this.redTeam;
    }

    public ArrayList<User> getBlueTeam() {
        return this.blueTeam;
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
        ArrayList<String> names = new ArrayList<>();

        for (User u : this.redTeam)
            u.getUsername();

        return names;
    }

    public ArrayList<String> getBlueTeamNames() {
        ArrayList<String> names = new ArrayList<>();

        for (User u : blueTeam)
            u.getUsername();

        return names;
    }

    public boolean addToRedTeam(User user) {
        if(this.redTeam.add(user)) {
            getRedTeamNames().add(user.getUsername());
            return true;
        }
        return false;
    }

    public boolean addToBlueTeam(User user) {
        if(this.blueTeam.add(user)) {
            getBlueTeamNames().add(user.getUsername());
            return true;
        }
        return false;
    }

    public boolean removeFromRedTeam(User user) {
        if(this.redTeam.remove(user)){
            getRedTeamNames().remove(user.getUsername());
            if (this.getRedTeam().size() == 0)
                this.setRedFlagLocation(null);
            return true;
        }
        return false;
    }

    public boolean removeFromBlueTeam(User user) {
        if(this.blueTeam.remove(user)){
            getBlueTeamNames().remove(user.getUsername());
            if (this.getBlueTeam().size() == 0)
                this.setBlueFlagLocation(null);
            return true;
        }
        return false;
    }

    public void sendToFirebase() {
        Firebase ref = ImportantMethods.getFireBase().child("Game").child(this.getUid());
        ref.child("name").setValue(this.name);
        ref.child("numPlayers").setValue(this.numPlayers);
        ref.child("redTeam").setValue(this.redTeam);
        ref.child("blueTeam").setValue(this.blueTeam);

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