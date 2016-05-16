package com.example.kevin.flagwars;

import android.content.Context;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

/**
 * Created by Adi on 5/16/16.
 */
public class CurrentUser {
    private static User currentUser = null;
    private CurrentUser() {}
    public static User getCurrentUser(Context c) {
        if (currentUser == null)
            CurrentUser.setCurrentUser(c);
        return currentUser;
    }

    public static void setCurrentUser(Context c) {
        Firebase.setAndroidContext(c);
        Firebase ref = ImportantMethods.getFireBase();

        if (ref.getAuth() != null) {
            ref.child("User").child(ref.getAuth().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, ?> map = (HashMap<String, ?>) dataSnapshot.getValue();
                    currentUser = new User((String) map.get("username"));
                    Log.e("Acquired user", currentUser.getName());
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e("Firebase error", "Error getting current user", firebaseError.toException());
                }
            });
        }
    }
}
