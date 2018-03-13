package com.athebapps.android.list;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

/** Custom implementation of DialogPreference used in the PreferenceFragment for the About dialog. */
public class AboutDialogPreference extends DialogPreference {

    public AboutDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.dialog_about);
        setIcon(R.drawable.ic_info_black_24dp);
        setPositiveButtonText(context.getString(android.R.string.ok));
        setNegativeButtonText(null);
    }
}
