package com.example.kevin.flagwars;

import android.media.Image;

/**
 * Created by Adi on 4/6/16.
 */
public class User {
    String name, username, password, email, phoneNumber;
    Image photo;

    public User(String name, String username, String password, String email, String phoneNumber) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        // TODO set photo to be some default photo if not initialized
    }

    public User(String name, String username, String password, String email, String phoneNumber, Image photo) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.photo = /*(photo == null) ? defaultPhoto : photo;*/ photo;
    }
}