package com.example.android.liste;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;


public class HelpPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    public static HelpPreferenceDialogFragmentCompat newInstance(Preference preference) {
        HelpPreferenceDialogFragmentCompat fragment = new HelpPreferenceDialogFragmentCompat();
        Bundle bundle = new Bundle(1);
        bundle.putString("key", preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
    }
}
