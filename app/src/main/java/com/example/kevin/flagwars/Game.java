package com.example.kevin.flagwars;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
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
            names.add((u.getUsername() != null) ? u.getUsername() : u.getEmail());

        return names;
    }

    public ArrayList<String> getBlueTeamNames() {
        ArrayList<String> names = new ArrayList<>();

        for (ParseUser u : this.blueTeam)
            names.add((u.getUsername() != null) ? u.getUsername() : u.getEmail());

        return names;
    }

    public boolean addToRedTeam(ParseUser user) {
        return this.redTeam.add(user);
    }

    public boolean addToBlueTeam(ParseUser user) {
        return this.blueTeam.add(user);
    }

    public ParseObject toParseObject() {
        if (this.object != null) {
            return this.object;
        } else {
            final ParseObject p = ParseObject.create("Game");
            p.add("name", this.name);
            p.add("numPlayers", this.numPlayers);
            p.add("flagLocations", this.flagLocations);
            p.add("redTeamNames", this.redTeam);
            p.add("blueTeamNames", this.blueTeam);
            p.add("anchorLocation", this.anchorLocation);
            p.pinInBackground();
            this.objectId = p.getObjectId();
            this.object = p;
            return p;
        }
    }

    public void saveInParse() {
        ParseObject o = (this.object == null) ? this.toParseObject() : this.object;
        o.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null)
                    Log.d("Parse Game Save", "Parse game saved successfully in background");
                else
                    e.printStackTrace();
            }
        });
    }

    /************************ STATIC HELPER METHODS ************************/

    public static Game parseObjectToGame(ParseObject o) {
        return new Game(o.getString("name"), o.getInt("numPlayers"),
                (ArrayList<ParseGeoPoint>) o.get("flagLocations"),
                (ArrayList<ParseUser>) o.get("redTeamNames"),
                (ArrayList<ParseUser>) o.get("blueTeamNames"));
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
