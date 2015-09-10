package com.example.calebmacdonaldblack.myapplication;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * Created by calebmacdonaldblack on 11/09/15.
 */
public class Prefs extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        addPreferencesFromResource(R.xml.prefs);
    }
}
