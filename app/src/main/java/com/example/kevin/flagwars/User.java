package com.example.kevin.flagwars;

/**
 * Created by Adi on 5/13/16.
 */
public class User {
    protected String username, uid = null;

    public User() { }

    public User(String name){
        this.username = name;
    }

    public String getUsername() { return this.username; }

    public String getUid() { return this.uid; }
}