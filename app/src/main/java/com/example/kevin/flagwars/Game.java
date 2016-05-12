package com.example.kevin.flagwars;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
    private ArrayList<ParseUser> redTeamNames, blueTeamNames;
    private ParseObject object = null;

    public Game(String name, int numPlayers, ArrayList<ParseGeoPoint> flagLocations,
                ArrayList<ParseUser> redTeamNames, ArrayList<ParseUser> blueTeamNames) {
        this.name = name;
        this.numPlayers = numPlayers;
        this.flagLocations = flagLocations;
        this.redTeamNames = redTeamNames;
        this.blueTeamNames = blueTeamNames;
    }

    public String getName() {
        return this.name;
    }

    public int getNumPlayers() {
        return this.numPlayers;
    }

    public ParseGeoPoint getRedFlagLocation() {
        return this.flagLocations.get(0);
    }

    public ParseGeoPoint getBlueFlagLocation() {
        return this.flagLocations.get(1);
    }

    public ParseGeoPoint getLocation() { return (getRedFlagLocation() != null) ? getRedFlagLocation() : getBlueFlagLocation(); }

    public ArrayList<ParseUser> getRedTeamNames() {
        return this.redTeamNames;
    }

    public ArrayList<ParseUser> getBlueTeamNames() {
        return this.blueTeamNames;
    }

    public boolean addToRedTeam(ParseUser user) {
        return this.redTeamNames.add(user);
    }

    public boolean addToBlueTeam(ParseUser user) {
        return this.blueTeamNames.add(user);
    }

    public ParseObject toParseObject() {
        if (this.object != null) {
            return this.object;
        } else {
            ParseObject p = ParseObject.create("Game");
            p.add("name", this.name);
            p.add("numPlayers", this.numPlayers);
            p.add("flagLocations", this.flagLocations);
            p.add("redTeamNames", this.redTeamNames);
            p.add("blueTeamNames", this.blueTeamNames);
            this.objectId = p.getObjectId();
            this.object = p;
            return p;
        }
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
