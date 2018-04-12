package com.athebapps.android.list;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.athebapps.android.list.database.ListContract;


/**
 * Utility methods related to SharedPreferences,
 */
class PreferenceUtils {

    /** @return  the RecyclerView.LayoutManager for the list activity, according to the user's preferences. */
    static RecyclerView.LayoutManager getListLayoutManager(
            Context context, SharedPreferences sharedPreferences) {
        if (sharedPreferences.getBoolean(context.getString(R.string.pref_list_compact_layout_key),
                context.getResources().getBoolean(R.bool.list_layout_compact_default)))
            return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        else
            return new LinearLayoutManager(context);
    }

    /** @return the RecyclerView.LayoutManager for the history activity, according to the user's preferences. */
    static RecyclerView.LayoutManager getHistoryLayoutManager(
            Context context, SharedPreferences sharedPreferences) {
        if (sharedPreferences.getBoolean(context.getString(R.string.pref_history_compact_layout_key),
                context.getResources().getBoolean(R.bool.history_layout_compact_default)))
            return new GridLayoutManager(context, 2);
        else
            return new LinearLayoutManager(context);
    }

    /** @return the font name set in preferences for the list activity.
     *  Possible values: 'sans-serif' or 'casual' */
    static String getFont(Context context, SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(context.getString(R.string.pref_font_key),
                context.getString(R.string.pref_font_roboto_value));
    }

    /** @return the String representation of the sort order set in preferences,
     * Possible values: 'priority ASC, product COLLATE LOCALIZED ASC'
     * or 'product COLLATE LOCALIZED ASC' */
    static String getSortOrder(Context context, SharedPreferences sharedPreferences) {
        String sortOrder = sharedPreferences.getString(
                context.getString(R.string.pref_sort_order_key),
                context.getString(R.string.pref_sort_order_name_value));
        if (sortOrder.equals(context.getString(R.string.pref_sort_order_priority_value))) {
            sortOrder = ListContract.ListEntry.COLUMN_PRIORITY + " ASC, "
                    + ListContract.ListEntry.COLUMN_PRODUCT + " COLLATE LOCALIZED ASC";
        } else {
            // Add COLLATE LOCALIZED to deal with special characters.
            sortOrder = ListContract.ListEntry.COLUMN_PRODUCT + " COLLATE LOCALIZED ASC";
        }
        return sortOrder;
    }

    /** Sets the alarm 'on' or 'off'. If 'on', also saves the time as a String representation. */
    static void setAlarm(Context context, SharedPreferences sharedPreferences, boolean is_on, String time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.list_reminder_alarm_on), is_on);
        if (is_on && time != null) editor.putString(context.getString(R.string.list_reminder_alarm_time), time);
        editor.apply();
    }

    /** @return true if a reminder is scheduled, false otherwise */
    static boolean isAlarmOn(Context context, SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(context.getString(R.string.list_reminder_alarm_on), false);
    }

    /** @return the String representation of the time set for the alarm */
    static String getAlarmTime(Context context, SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(context.getString(R.string.list_reminder_alarm_time), "");
    }

    /** Sets the Toolbar font to the provided Typeface */
    static void styleToolbar(Toolbar toolbar, Typeface typeface) {
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
