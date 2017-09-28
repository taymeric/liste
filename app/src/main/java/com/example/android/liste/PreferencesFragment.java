package com.example.android.liste;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;


/**
 * Fragment that displays the user settings.
 */
public class PreferencesFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        addPreferencesFromResource(R.xml.preferences);

        // Go through all of the preferences, and set up their preference summary.

        PreferenceScreen prefScreen = getPreferenceScreen();
        SharedPreferences sharedPreferences = prefScreen.getSharedPreferences();

        for (int i = 0; i < prefScreen.getPreferenceCount(); i++) {

            Preference p1 = prefScreen.getPreference(i);
            if (p1 instanceof PreferenceCategory) {
                // If the preference is a category, browse all the sub-preferences.
                // (The top-level preferences in our preference fragment are all categories.)
                PreferenceCategory cat = (PreferenceCategory) p1;

                for (int j = 0; j < cat.getPreferenceCount(); j++) {

                    Preference p2 = cat.getPreference(j);
                    if (p2 instanceof ListPreference) {
                        String value = sharedPreferences.getString(p2.getKey(), "");
                        setPreferenceSummary(p2, value);
                    }
                }
            }

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Preference preference = findPreference(s);
        if (preference != null) {
            if (preference instanceof ListPreference) {
                // Every time a list preference is changed, the Fragment needs to update the summary
                // for this preference.
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment fragment;
        // HelpDialogPreference is used to display a dialog fot the help/info section
        if (preference instanceof HelpDialogPreference) {
            fragment = HelpPreferenceDialogFragmentCompat.newInstance(preference);
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(),
                    "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else super.onDisplayPreferenceDialog(preference);
    }

    /* Sets the summary for a given preference passed as parameter */
    private void setPreferenceSummary(Preference preference, String value) {
        if (preference instanceof ListPreference) {
            // For list preferences, figure out the label of the selected value
            // and set the summary to that label.
            // The array Entries stores the labels.
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
    }
}
