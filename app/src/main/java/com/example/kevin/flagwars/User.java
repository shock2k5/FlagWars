package com.example.kevin.flagwars;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

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

    public User(String name){
        username = name;
    }

    public String getEmail() { return this.email; }

    public String getPassword() { return this.password; }

    public String getUsername() { return this.username; }
}