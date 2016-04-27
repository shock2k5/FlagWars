package com.example.kevin.flagwars;

import com.parse.ParseGeoPoint;
import java.util.ArrayList;

/**
 * Created by Adi on 4/27/16.
 */
public class Game {
    /**
     *  public is true private is false
     */
    boolean visibility;
    String name;
    ParseGeoPoint location;
    int numPlayers;
    int timeLimit;
    ArrayList<String> playerNames;
    ParseGeoPoint[] flagLocations;

    public Game(String name, boolean visibility, ParseGeoPoint location,
                int numPlayers, int timeLimit, ArrayList<String> playerNames, ParseGeoPoint[] flagLocations) {
        this.name = name;
        this.visibility = visibility;
        this.location = location;
        this.numPlayers = numPlayers;
        this.timeLimit = timeLimit;
        this.playerNames = playerNames;
    }
}
