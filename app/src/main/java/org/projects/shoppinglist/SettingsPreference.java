package org.projects.shoppinglist;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

// Here we are extending the standard preference fragment.
public class SettingsPreference extends PreferenceFragment {

    // These values are specified in the preference.xml file
    // They needs to correspond exactly to those in the preference.xml file.
    private static String SETTINGS_GENDERKEY = "male";
    private static String SETTINGS_NAMEKEY = "name";

    // This is static methods - meaning they always exists.
    // Then we don't have to create an instance of this class to get the values.
    public static boolean isMale(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTINGS_GENDERKEY, true);
    }

    public static String getName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SETTINGS_NAMEKEY, "");
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Adding the preferences from the preference.xml.
        addPreferencesFromResource(R.xml.preference);
    }
}