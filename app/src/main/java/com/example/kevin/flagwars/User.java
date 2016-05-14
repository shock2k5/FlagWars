package com.example.kevin.flagwars;

/**
 * Created by Adi on 5/13/16.
 */
public class User {
    protected String username, name;

    public User() { }

    public User(String name){
        this.username = name;
        this.name = ImportantMethods.emailToUsername(name);
    }

    public String getUsername() { return this.username; }

    public String getName() { return this.name; }

    public boolean equals(User other){
        if(other == null) return false;
        return this.username.equals(other.username);
    }
    public String toString(){
        return name;
    }
}