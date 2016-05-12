package com.example.kevin.flagwars;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adi on 4/27/16.
 */

public class Game {
    /**
     *  public is true private is false
     */
    boolean visibility;
    String name, code;
    ParseGeoPoint location;
    int numPlayers;
    int timeLimit;
    ArrayList<String> playerNames;
    ArrayList<ParseGeoPoint> flagLocations;

    public Game(String name, String code, boolean visibility, ParseGeoPoint location,
                int numPlayers, int timeLimit, ArrayList<String> playerNames,
                ArrayList<ParseGeoPoint> flagLocations) {
        this.name = name;
        this.code = code;
        this.visibility = visibility;
        this.location = location;
        this.numPlayers = numPlayers;
        this.timeLimit = timeLimit;
        this.playerNames = playerNames;
        this.flagLocations = flagLocations;
    }

    public Game(String name, String code, boolean visibility, ParseGeoPoint location,
                int numPlayers, int timeLimit, Object playerNames, Object flagLocations) {
        this.name = name;
        this.code = code;
        this.visibility = visibility;
        this.location = location;
        this.numPlayers = numPlayers;
        this.timeLimit = timeLimit;
        this.playerNames = (ArrayList<String>) playerNames;
        this.flagLocations = (ArrayList<ParseGeoPoint>) flagLocations;
    }

    public static List<String> getGameNames(List<Game> gameList) {
        List<String> names = new ArrayList<>();

        for (Game g : gameList)
            names.add(g.name);

        return names;
    }

    public static ParseObject convertGameToParseObject(Game g) {
        ParseObject o = ParseObject.create("Game");
        o.add("name", g.name);
        o.add("code", g.code);
        o.add("visibility", g.visibility);
        o.add("location", g.location);
        o.add("numPlayers", g.numPlayers);
        o.add("timeLimit", g.timeLimit);
        o.add("playerNames", g.playerNames);
        return o;
    }

    public static Game getObjectFromParse(String objectId) {
        ParseQuery q = ParseQuery.getQuery("Game");
        Game g = null;
        try {
            g = objectRetrievalSuccessful(q.get(objectId));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return g;
    }

    private static Game objectRetrievalSuccessful(ParseObject o) {
        return new Game(o.getString("name"), o.getString("code"), o.getBoolean("visibility"),
                o.getParseGeoPoint("location"), o.getInt("numPlayers"), o.getInt("timeLimit"),
                o.get("playerNames"), o.get("flagLocations"));
    }

    public static List<Game> parseObjectsToGames(List<ParseObject> objects) {
        List<Game> l = new ArrayList<>();

        for (ParseObject o : objects)
            l.add(objectRetrievalSuccessful(o));

        return l;
    }
}
