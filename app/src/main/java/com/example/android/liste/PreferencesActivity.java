package com.example.android.liste;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Nesting activity for our PreferenceFragment
 * */
public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
    }
}
