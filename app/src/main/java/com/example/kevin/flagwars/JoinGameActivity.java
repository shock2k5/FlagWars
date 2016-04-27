package com.example.kevin.flagwars;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Currency;

public class JoinGameActivity extends AppCompatActivity {

    ListView mGameListView;
    TextView mEnterCodeTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        mGameListView = (ListView) findViewById(R.id.gameListView);
        mEnterCodeTextView = (TextView) findViewById(R.id.enterCodeEditText);


    }
}
