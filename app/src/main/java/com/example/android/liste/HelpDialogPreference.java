package com.example.android.liste;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

/** Custom implementation of DialogPreference used in the PreferenceFragment for the Help dialog. */
public class HelpDialogPreference extends DialogPreference {

    public HelpDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.dialog_help);
        setIcon(R.drawable.ic_help_black_24dp);
        setPositiveButtonText(context.getString(android.R.string.ok));
        setNegativeButtonText(null);
    }
}
