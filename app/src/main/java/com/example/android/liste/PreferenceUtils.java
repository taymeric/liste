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
 * Utility methods to get and set settings values of SharedPreferences
 */
class PreferenceUtils {

    static final int NORMAL_LAYOUT_ITEM = 1;
    static final int COMPACT_LAYOUT_ITEM = 2;

    static int getListLayoutType(Context context, SharedPreferences sharedPreferences) {
        String layout_value = sharedPreferences.getString(context.getString(R.string.pref_list_layout_key),
                context.getString(R.string.pref_layout_normal_value));
        if (layout_value.equals(context.getString(R.string.pref_layout_compact_value)))
            return COMPACT_LAYOUT_ITEM;
        else
            return NORMAL_LAYOUT_ITEM;
    }

    static int getHistoryLayoutType(Context context, SharedPreferences sharedPreferences) {
        String layout_value = sharedPreferences.getString(context.getString(R.string.pref_history_layout_key),
                context.getString(R.string.pref_layout_normal_value));
        if (layout_value.equals(context.getString(R.string.pref_layout_compact_value)))
            return COMPACT_LAYOUT_ITEM;
        else
            return NORMAL_LAYOUT_ITEM;
    }

    static RecyclerView.LayoutManager getListLayout(
            Context context, SharedPreferences sharedPreferences) {

        String value = sharedPreferences.getString(
                context.getString(R.string.pref_list_layout_key),
                context.getString(R.string.pref_layout_normal_value));
        if (value.equals(context.getString(R.string.pref_layout_compact_value)))
            return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        else
            return new LinearLayoutManager(context);
    }

    static RecyclerView.LayoutManager getHistoryLayout(
            Context context, SharedPreferences sharedPreferences) {

        String value = sharedPreferences.getString(
                context.getString(R.string.pref_history_layout_key),
                context.getString(R.string.pref_layout_normal_value));
        if (value.equals(context.getString(R.string.pref_layout_compact_value)))
            return new GridLayoutManager(context, 2);
        else
            return new LinearLayoutManager(context);
    }

    static String getFont(Context context, SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(context.getString(R.string.pref_font_key),
                context.getString(R.string.pref_font_normal_value));
    }

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

    // Sets the alarm 'on' or 'off'. If 'on', also saves the time as a String representation.
    static void setAlarm(Context context, SharedPreferences sharedPreferences, boolean is_on, String time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.list_reminder_alarm_on), is_on);
        if (is_on && time != null) editor.putString(context.getString(R.string.list_reminder_alarm_time), time);
        editor.apply();
    }

    static boolean isAlarmOn(Context context, SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(context.getString(R.string.list_reminder_alarm_on), false);
    }

    static String getAlarmTime(Context context, SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(context.getString(R.string.list_reminder_alarm_time), "");
    }
}
