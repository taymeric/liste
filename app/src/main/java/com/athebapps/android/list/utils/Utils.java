package com.athebapps.android.list.utils;

import android.graphics.Typeface;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;


public class Utils {

    /** Sets the Toolbar font to the provided Typeface */
    public static void styleToolbar(Toolbar toolbar, Typeface typeface) {
        // this is gross but toolbar doesn't expose it's children
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View rawView = toolbar.getChildAt(i);
            if (!(rawView instanceof TextView)) {
                continue;
            }
            TextView textView = (TextView) rawView;
            textView.setTypeface(typeface);
        }
    }
}
