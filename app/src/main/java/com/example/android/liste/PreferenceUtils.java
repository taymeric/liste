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

    /*
     * In this method, the parameter 'key' differentiate between the RecyclerView for the list
     * and the RecyclerView for the history.
     */
    static float getTextSize(
            Context context, SharedPreferences sharedPreferences, String key) {
        float size;
        String sizeString = sharedPreferences.getString(
                key,
                context.getString(R.string.pref_size_big_value));
        if (sizeString.equals(context.getString(R.string.pref_size_big_value))) {
            size = context.getResources().getDimension(R.dimen.text_big);
        } else {
            size = context.getResources().getDimension(R.dimen.text_small);
        }
        return size;
    }

    static String getFont(Context context, SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(context.getString(R.string.pref_font_key),
                context.getString(R.string.pref_font_normal_value));
    }

    /*
     * In this method, the parameter 'key' differentiate between the RecyclerView for the list
     * and the RecyclerView for the history.
     */
    static RecyclerView.LayoutManager getLayout(
            Context context, SharedPreferences sharedPreferences, String key) {

        String value = sharedPreferences.getString(
                key,
                context.getString(R.string.pref_layout_linear_value));
        if (value.equals(context.getString(R.string.pref_layout_grid_value))) {
            return new GridLayoutManager(context, 2);
        } else if (value.equals(context.getString(R.string.pref_layout_staggered_grid_value))) {
            return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        }
        else
            return new LinearLayoutManager(context);
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
        editor.putBoolean(context.getString(R.string.alarm_on), is_on);
        if (is_on && time != null) editor.putString(context.getString(R.string.alarm_time), time);
        editor.apply();
    }

    static boolean isAlarmOn(Context context, SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(context.getString(R.string.alarm_on), false);
    }

    static String getAlarmTime(Context context, SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(context.getString(R.string.alarm_time), "");
    }
}
