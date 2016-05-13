package com.example.kevin.flagwars;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import bolts.Task;

/**
 * Created by Adi on 4/27/16.
 */

public class Game {
    /**
     *  public is true private is false
     */
    private String name, objectId = null;
    private int numPlayers;
    private ArrayList<ParseGeoPoint> flagLocations;
    private ArrayList<ParseUser> redTeam, blueTeam;
    private ParseGeoPoint anchorLocation = null;

    public Game(String name, int numPlayers, ArrayList<ParseGeoPoint> flagLocations) {
        this.name = name;
        this.numPlayers = numPlayers;
        this.flagLocations = flagLocations;
        this.flagLocations.ensureCapacity(2);
        this.redTeam = new ArrayList<ParseUser>();
        this.blueTeam = new ArrayList<ParseUser>();
    }

    public String getName() {
        return this.name;
    }

    public int getNumPlayers() {
        return this.numPlayers;
    }

    public String getObjectId() { return this.objectId; }

    public ArrayList<ParseUser> getRedTeam() {
        return this.redTeam;
    }

    public ArrayList<ParseUser> getBlueTeam() {
        return this.blueTeam;
    }

    public void setRedFlagLocation(ParseGeoPoint loc) {
        if (!loc.equals(this.flagLocations.get(0))) {
            if (this.flagLocations.size() == 2)
                this.flagLocations.remove(0);
            this.flagLocations.add(0, loc);
            if (this.anchorLocation == null)
                this.anchorLocation = getLocation();
        }
    }

    public void setBlueFlagLocation(ParseGeoPoint loc) {
        if (!loc.equals(this.flagLocations.get(1))) {
            if (this.flagLocations.size() == 2)
                this.flagLocations.remove(1);
            this.flagLocations.add(1, loc);
            if (this.anchorLocation == null)
                this.anchorLocation = getLocation();
        }
    }

    public ParseGeoPoint getRedFlagLocation() {
        return (this.flagLocations.size() > 0) ? this.flagLocations.get(0) : null;
    }

    public ParseGeoPoint getBlueFlagLocation() {
        return (this.flagLocations.size() == 2) ? this.flagLocations.get(1) : null;
    }

    public ParseGeoPoint getLocation() {
        return (getRedFlagLocation() != null) ? getRedFlagLocation() : getBlueFlagLocation();
    }

    public ArrayList<String> getRedTeamNames() {
        ArrayList<String> names = new ArrayList<>();

        for (ParseUser u : this.redTeam)
            try {
                names.add(u.fetchIfNeeded().getUsername());
            } catch (ParseException e) {
                Log.e("Parse Failure", "Something has gone terribly wrong with Parse", e);
            }

        return names;
    }

    public ArrayList<String> getBlueTeamNames() {
        ArrayList<String> names = new ArrayList<>();

        for (ParseUser u : this.blueTeam)
            try {
                names.add(u.fetchIfNeeded().getUsername());
            } catch (ParseException e) {
                e.printStackTrace();
            }

        return names;
    }

    public boolean addToRedTeam(ParseUser user) {
        return this.redTeam.add(user);
    }

    public boolean addToBlueTeam(ParseUser user) {
        if(this.blueTeam.add(user)) {
            getBlueTeamNames().add(user.getUsername());
            this.saveInParse();
            return true;
        }
        return false;
    }

    public boolean removeFromRedTeam(ParseUser user) {
        if(this.redTeam.remove(user)){
            getRedTeamNames().remove(user.getUsername());
            this.saveInParse();
            return true;
        }
        return false;
    }

    public boolean removeFromBlueTeam(ParseUser user) {
        if(this.blueTeam.remove(user)){
            getBlueTeamNames().remove(user.getUsername());
            this.saveInParse();
            return true;
        }
        return false;
    }

    public ParseObject toParseObject() {
        final ParseObject p = ParseObject.create("Game");
        p.put("name", this.name);
        p.put("numPlayers", this.numPlayers);
        p.put("flagLocations", this.flagLocations);
        p.put("redTeamNames", this.redTeam);
        p.put("blueTeamNames", this.blueTeam);
        if (anchorLocation != null){
            p.put("anchorLocation", this.anchorLocation);
        }
        if (this.objectId != null)
            p.setObjectId(this.objectId);
        this.saveInParse();
        return p;
    }

    public void saveInParse() {
        ParseObject o = this.toParseObject();
        try {
            o.save();
            if (this.objectId == null)
                this.objectId = o.getObjectId();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /************************ STATIC HELPER METHODS ************************/

    public static Game parseObjectToGame(ParseObject o) {
        Game g = new Game(o.getString("name"), o.getInt("numPlayers"),
                (ArrayList<ParseGeoPoint>) o.get("flagLocations"));
        g.objectId = o.getObjectId();
        g.anchorLocation = o.getParseGeoPoint("anchorLocation");
        g.redTeam = (ArrayList<ParseUser>) o.get("redTeamNames");
        g.blueTeam = (ArrayList<ParseUser>) o.get("blueTeamNames");
        return g;
    }

    public static List<Game> getGameListFromParse(List<ParseObject> objects) {
        List<Game> l = new ArrayList<>();

        for (ParseObject o : objects)
            l.add(parseObjectToGame(o));

        return l;
    }

    public static Game getObjectFromParse(String objectId) {
        ParseQuery q = ParseQuery.getQuery("Game");
        try {
            return parseObjectToGame(q.get(objectId));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
