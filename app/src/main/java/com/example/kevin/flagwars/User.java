package com.example.kevin.flagwars;

/**
 * Created by Adi on 5/13/16.
 */
public class User {
    protected String email, password, username;

    public User() {
        email = null;
        password = null;
        username = "null";
    }

    public String getEmail() { return this.email; }

    public String getPassword() { return this.password; }

    public String getUsername() { return this.username; }
}