package com.example.kevin.flagwars;

import android.util.Log;

import com.firebase.client.AuthData;
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

    public String getEmail() { return this.email; }

    public String getPassword() { return this.password; }

    public String getUsername() { return this.username; }

    public void copyUser(User copy) {
        this.email = copy.getEmail();
        this.password = copy.getPassword();
        this.username = copy.getUsername();
    }

    /****************************** STATIC METHOD ******************************/

    public static User authDataToUser() {
        final Firebase ref = new Firebase("https://flagwar.firebaseio.com/");
        final User user = new User();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User temp = dataSnapshot.child("User/" + ref.getAuth().getUid()).getValue(User.class);
                user.copyUser(temp);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("Firebase Read Error", "Occurred in User/authDataToUser", firebaseError.toException());
            }
        });
        return user;
    }
}
