package com.example.kevin.flagwars;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class ChooseGameModeActivity extends AppCompatActivity {

    protected Button mCreateGameButton, mJoinGameButton;

    private String[] mDrawerItems;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private Firebase fireRef;

    final Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this.getApplicationContext());
        setContentView(R.layout.activity_choose_game_mode);
        fireRef = new Firebase("https://flagwar.firebaseio.com/");

        mCreateGameButton = (Button) findViewById(R.id.createGameBT);
        mJoinGameButton = (Button) findViewById(R.id.joinGameBT);

        mCreateGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (fireRef.getAuth() == null) {
                    // Don't have current user
                    Intent i = new Intent(ChooseGameModeActivity.this, LoginActivity.class);
                    i.putExtra("gameMode", "createGame");
                    startActivity(i);
                } else {
                    // Current user is logged in
                    Intent i = new Intent(ChooseGameModeActivity.this, CreateGameActivity.class);
                    startActivity(i);
                }

            }
        });

        mJoinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fireRef.getAuth() == null) {
                    // Don't have current user
                    Intent i = new Intent(ChooseGameModeActivity.this, LoginActivity.class);
                    i.putExtra("gameMode", "joinGame");
                    startActivity(i);
                } else {
                    // Current user is logged in
                    Intent i = new Intent(ChooseGameModeActivity.this, JoinGameActivity.class);
                    startActivity(i);
                }
            }
        });
        mTitle = "test";


        if (fireRef.getAuth() == null) {
            mDrawerItems = new String[]{"Log In", "Settings", "Rules"};
        } else {
            mDrawerItems = new String[]{ImportantMethods.getUserName(), "Settings", "Rules", "Log Out"};
        }

        //mDrawerItems = getResources().getStringArray(R.array.drawer_list);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerItems));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());



        mDrawerToggle = new CustomActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout)         /* DrawerLayout object */
                 {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle("FlagWars");
                //mJoinGameButton.setVisibility(View.VISIBLE);
                //mCreateGameButton.setVisibility(View.VISIBLE);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle("FlagWars");
                //mJoinGameButton.setVisibility(View.GONE);
                //mCreateGameButton.setVisibility(View.GONE);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {

        if (position == 0) {
            if (fireRef.getAuth() == null) {
                Intent i = new Intent(ChooseGameModeActivity.this, LoginActivity.class);
                i.putExtra("gameMode", "fromNavDrawer");
                startActivity(i);
            } else {
                //profile page
            }

        } else if (position == 1) {
            //settings page
        } else if (position == 2) {
            //rules page
        } else if (position == 3) {
           new AlertDialog.Builder(context)
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            fireRef.unauth();
                            Intent i = new Intent(ChooseGameModeActivity.this, ChooseGameModeActivity.class);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mDrawerLayout.closeDrawer(mDrawerList);
                        }
                    })
                    .show();

        }

        // Create a new fragment and specify the planet to show based on position
        /*Fragment fragment = new PlanetFragment();
        Bundle args = new Bundle();
        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        fragment.setArguments(args);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();*/
        //Toast.makeText(this, R.string.app_name, Toast.LENGTH_SHORT).show();

        // Highlight the selected item, update the title, and close the drawer
        //mDrawerList.setItemChecked(position, true);
        //setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
            //Toast.makeText(this, R.string.app_name, Toast.LENGTH_SHORT).show();

            // Highlight the selected item, update the title, and close the drawer
            //mDrawerList.setItemChecked(position, true);
            //setTitle(mPlanetTitles[position]);
            //mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    public class CustomActionBarDrawerToggle extends ActionBarDrawerToggle {
        public  CustomActionBarDrawerToggle(Activity mActivity,DrawerLayout mDrawerLayout)
        {
            super(mActivity, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        }
    }

}
