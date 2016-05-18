package com.example.kevin.flagwars;

import com.firebase.client.Firebase;

public class ImportantMethods {
    private static Firebase fireRef = ImportantMethods.getFireBase();
    public static Firebase getFireBase(){
        return new Firebase("https://flagwar.firebaseio.com/");
    }


    public static String emailToUsername(String str){
        return str.substring(0, str.indexOf("@"));
    }

    public static void addNewUser(User user){
        fireRef.child("User").child(fireRef.getAuth().getUid()).setValue(user);
        fireRef.child("User").child(fireRef.getAuth().getUid()).child("username").setValue(user.username);
    }
}
