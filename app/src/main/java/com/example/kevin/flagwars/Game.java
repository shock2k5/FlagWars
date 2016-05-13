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
    private ParseObject object = null;
    private ParseGeoPoint anchorLocation = null;

    public Game(String name, int numPlayers, ArrayList<ParseGeoPoint> flagLocations,
                ArrayList<ParseUser> redTeam, ArrayList<ParseUser> blueTeam) {
        this.name = name;
        this.numPlayers = numPlayers;
        this.flagLocations = flagLocations;
        this.redTeam = redTeam;
        this.blueTeam = blueTeam;
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
        this.flagLocations.add(0, loc);
        if (this.anchorLocation == null)
            this.anchorLocation = getLocation();
    }

    public void setBlueFlagLocations(ParseGeoPoint loc) {
        this.flagLocations.add(1, loc);
        if (this.anchorLocation == null)
            this.anchorLocation = getLocation();
    }

    public ParseGeoPoint getRedFlagLocation() {
        return (this.flagLocations.size() > 0) ? this.flagLocations.get(0) : null;
    }

    public ParseGeoPoint getBlueFlagLocation() {
        return (this.flagLocations.size() > 0) ? this.flagLocations.get(1) : null;
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
        return this.blueTeam.add(user);
    }

    public boolean removeFromRedTeam(ParseUser user) {
        return this.redTeam.remove(user);
    }

    public boolean removeFromBlueTeam(ParseUser user) {
        return this.blueTeam.remove(user);
    }

    public ParseObject toParseObject() {
        if (this.object != null) {
            return this.object;
        } else {
            final ParseObject p = ParseObject.create("Game");
            p.put("name", this.name);
            p.put("numPlayers", this.numPlayers);
            p.put("flagLocations", this.flagLocations);
            p.put("redTeamNames", this.redTeam);
            p.put("blueTeamNames", this.blueTeam);
            if (anchorLocation != null){
                p.put("anchorLocation", this.anchorLocation);
            }
            this.object = p;
            this.saveInParse();
            return p;
        }
    }

    public void saveInParse() {
        ParseObject o;
        if (this.object == null){
            o = this.toParseObject();
        } else{
            o = this.object;
        }
        try {
            o.save();
            this.objectId = o.getObjectId();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /************************ STATIC HELPER METHODS ************************/

    public static Game parseObjectToGame(ParseObject o) {
        Game g = new Game(o.getString("name"), o.getInt("numPlayers"),
                (ArrayList<ParseGeoPoint>) o.get("flagLocations"),
                (ArrayList<ParseUser>) o.get("redTeamNames"),
                (ArrayList<ParseUser>) o.get("blueTeamNames"));
        g.objectId = o.getObjectId();
        g.object = o;
        g.anchorLocation = o.getParseGeoPoint("anchorLocation");
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
