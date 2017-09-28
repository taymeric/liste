package com.example.android.liste;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.example.android.liste.data.ListContract;


/**
 * Utility methods related to SharedPreferences,
 */
class PreferenceUtils {

    /** Identifies a normal layout */
    static final int NORMAL_LAYOUT_ITEM = 1;

    /* Identifies a compact layout */
    private static final int COMPACT_LAYOUT_ITEM = 2;

    /** @return the type of layout set in preferences for the list activity.
     *  Possibles values: NORMAL_LAYOUT_ITEM or COMPACT_LAYOUT_ITEM */
    static int getListLayoutType(Context context, SharedPreferences sharedPreferences) {
        boolean compact_layout_value = sharedPreferences.getBoolean(context.getString(R.string.pref_list_compact_layout_key),
                false);
        if (compact_layout_value)
            return COMPACT_LAYOUT_ITEM;
        else
            return NORMAL_LAYOUT_ITEM;
    }

    /** @return the type of layout set in preferences for the history activity
     *  Possibles values: NORMAL_LAYOUT_ITEM or COMPACT_LAYOUT_ITEM */
    static int getHistoryLayoutType(Context context, SharedPreferences sharedPreferences) {
        boolean compact_layout_value = sharedPreferences.getBoolean(context.getString(R.string.pref_history_compact_layout_key),
                true);
        if (compact_layout_value)
            return COMPACT_LAYOUT_ITEM;
        else
            return NORMAL_LAYOUT_ITEM;
    }

    /** @return  the RecyclerView.LayoutManager for the list activity. */
    static RecyclerView.LayoutManager getListLayoutManager(
            Context context, SharedPreferences sharedPreferences) {
        int type = getListLayoutType(context, sharedPreferences);
        if (type == COMPACT_LAYOUT_ITEM)
            return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        else
            return new LinearLayoutManager(context);
    }

    /** @return the RecyclerView.LayoutManager for the history activity. */
    static RecyclerView.LayoutManager getHistoryLayoutManager(
            Context context, SharedPreferences sharedPreferences) {
        int type = getHistoryLayoutType(context, sharedPreferences);
        if (type == COMPACT_LAYOUT_ITEM)
            return new GridLayoutManager(context, 2);
        else
            return new LinearLayoutManager(context);
    }

    /** @return the font name set in preferences for the list activity.
     *  Possible values: 'sans-serif' or 'casual' */
    static String getFont(Context context, SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(context.getString(R.string.pref_font_key),
                context.getString(R.string.pref_font_normal_value));
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

    /** @return the direction for swiping set in preferences.
     * Possibles values:
     * ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT or ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT*/
    static int getSwipeDirection(Context context, SharedPreferences sharedPreferences) {
        String directionString = sharedPreferences.getString(
                context.getString(R.string.pref_direction_key),
                context.getString(R.string.pref_direction_right_value));
        if (directionString.equals(context.getString(R.string.pref_direction_right_value)))
            return ItemTouchHelper.RIGHT;
        else if (directionString.equals(context.getString(R.string.pref_direction_left_value)))
            return ItemTouchHelper.LEFT;
        else return ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
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
}
