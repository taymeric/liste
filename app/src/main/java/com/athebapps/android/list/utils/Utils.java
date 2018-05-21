package com.athebapps.android.list.utils;

import android.graphics.Typeface;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;


public class Utils {

    /** Sets the Toolbar font to the provided Typeface. */
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

    ///** Gets the current date as a String representation, formatted according to the locale. */
    /*public static String getDate(Context context) {
        Calendar c = Calendar.getInstance();
        Date date = c.getTime();
        java.text.DateFormat formatter = DateFormat.getDateFormat(context);
        return formatter.format(date);
    }*/

}
