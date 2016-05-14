package com.example.kevin.flagwars;

import android.location.Location;
import android.location.LocationManager;
import android.provider.ContactsContract;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Adi on 4/27/16.
 */

public class Game {
    /**
     *  public is true private is false
     */
    private String name;
    private int numPlayers;
    private ArrayList<User> redTeam, blueTeam;
    private Location redFlag, blueFlag;
    private Location anchorLocation = null;

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
        ref.child("redTeam").setValue(this.redTeam.toArray());

        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation("anchorLocation",
                new GeoLocation(anchorLocation.getLatitude(), anchorLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, FirebaseError error) {
                if (error != null) {
                    System.err.println("There was an error saving the location to GeoFire: " + error);
                } else {
                    System.out.println("Location saved on server successfully!");
                }
            }
        });
        geoFire.setLocation("redFlag",
                new GeoLocation(redFlag.getLatitude(), redFlag.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, FirebaseError error) {
                        if (error != null) {
                            System.err.println("There was an error saving the location to GeoFire: " + error);
                        } else {
                            System.out.println("Location saved on server successfully!");
                        }
                    }
                });
        geoFire.setLocation("blueFlag",
                new GeoLocation(blueFlag.getLatitude(), blueFlag.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, FirebaseError error) {
                        if (error != null) {
                            System.err.println("There was an error saving the location to GeoFire: " + error);
                        } else {
                            System.out.println("Location saved on server successfully!");
                        }
                    }
                });

        // TODO arraylists to map
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
                GeoFire geoFire = new GeoFire(ref);
                geoFire.getLocation("anchorLocation", new LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location) {
                        game.anchorLocation = new Location(LocationManager.GPS_PROVIDER);
                        game.anchorLocation.setLatitude(location.latitude);
                        game.anchorLocation.setLongitude(location.longitude);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        System.err.println("There was an error getting the location from Firebase: " + firebaseError);
                    }
                });
                geoFire.getLocation("redFlag", new LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location) {
                        game.redFlag = new Location(LocationManager.GPS_PROVIDER);
                        game.redFlag.setLatitude(location.latitude);
                        game.redFlag.setLongitude(location.longitude);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        System.err.println("There was an error getting the location from Firebase: " + firebaseError);
                    }
                });
                geoFire.getLocation("blueFlag", new LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location) {
                        game.blueFlag = new Location(LocationManager.GPS_PROVIDER);
                        game.blueFlag.setLatitude(location.latitude);
                        game.blueFlag.setLongitude(location.longitude);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        System.err.println("There was an error getting the location from Firebase: " + firebaseError);
                    }
                });

                // TODO convert map to arraylist
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.err.println("There was an error getting the Game from Firebase: " + firebaseError);
            }
        });
        return game;
    }
}
