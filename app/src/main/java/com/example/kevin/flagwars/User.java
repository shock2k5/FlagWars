package com.example.kevin.flagwars;

@SuppressWarnings("unused")
public class User {
    protected String username, name;

    public User() { }

    public User(String name){
        this.username = name;
        this.name = ImportantMethods.emailToUsername(name);
    }

    public String getName() { return this.name; }

    public boolean equals(User other){
        return other != null && this.username.equals(other.username);
    }

    public String toString(){
        return name;
    }
}