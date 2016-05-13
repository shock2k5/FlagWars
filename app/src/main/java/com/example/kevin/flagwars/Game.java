package com.example.kevin.flagwars;

import android.location.Location;
import java.util.ArrayList;

/**
 * Created by Adi on 4/27/16.
 */

public class Game {
    /**
     *  public is true private is false
     */
    private String name, uid = null; // TODO update uid somehow
    private int numPlayers;
    private ArrayList<User> redTeam, blueTeam;
    private Location redFlag, blueFlag;
    private Location anchorLocation = null;

    public Game(String name, int numPlayers) {
        this.name = name;
        this.numPlayers = numPlayers;
        this.redFlag = null;
        this.blueFlag = null;
        this.redTeam = new ArrayList<User>();
        this.blueTeam = new ArrayList<User>();
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

    public String getUid() { return this.uid; }

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
}
