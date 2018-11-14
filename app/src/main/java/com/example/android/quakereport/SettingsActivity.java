package com.example.android.quakereport;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity); //導入settings_activity佈局(Fragment)，整個settings頁面就變成一個Fragment
                                                    //以下再透過EarthquakePreferenceFragment導入外掛或Widget(settings_main佈局/PreferenceScreen)
    }

    public static class EarthquakePreferenceFragment extends PreferenceFragment {

        //Override the onCreate() method to use the settings_main XML resource that we defined earlier.
        //在onCreate中把剛剛做好的PreferenceScreen (settings_main.xml)加進去

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);
        }
    }

}