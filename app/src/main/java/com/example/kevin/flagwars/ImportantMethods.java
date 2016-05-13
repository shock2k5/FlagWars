package com.example.kevin.flagwars;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by E&D on 5/13/2016.
 */
public class ImportantMethods {
    private static Firebase ref;
    private static User user;

    public static String getUserName(){
        ref = new Firebase("https://flagwar.firebaseio.com/");
        String uid = ref.getAuth().getUid();
        ref.child("User/uid/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        return user.username;
    }
}
