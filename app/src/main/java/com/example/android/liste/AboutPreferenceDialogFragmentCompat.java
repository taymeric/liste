package com.example.android.liste;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

/** Necessary class for custom PreferenceDialog */
public class AboutPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    public static AboutPreferenceDialogFragmentCompat newInstance(Preference preference) {
        AboutPreferenceDialogFragmentCompat fragment = new AboutPreferenceDialogFragmentCompat();
        Bundle bundle = new Bundle(1);
        bundle.putString("key", preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
    }
}
