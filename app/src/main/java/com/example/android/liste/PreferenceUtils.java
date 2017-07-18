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
 * Utility methods to get settings values in accordance with preference settings
 */
class PreferenceUtils {

    /*
     * In this method, the parameter 'key' differentiate between the RecyclerView for the list
     * and the RecyclerView for the history.
     */
    static float getTextSizeFromPrefs(
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

    /*
     * In this method, the parameter 'key' differentiate between the RecyclerView for the list
     * and the RecyclerView for the history.
     */
    static RecyclerView.LayoutManager getLayoutFromPrefs(
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

    static String getSortOrderFromPrefs(Context context, SharedPreferences sharedPreferences) {
        String sortOrder = sharedPreferences.getString(
                context.getString(R.string.pref_sort_order_key),
                context.getString(R.string.pref_sort_order_name_value));
        if (sortOrder.equals(context.getString(R.string.pref_sort_order_priority_value))) {
            sortOrder = ListContract.ListEntry.COLUMN_PRIORITY + " ASC, "
                    + ListContract.ListEntry.COLUMN_STRING + " COLLATE LOCALIZED ASC";
        } else {
            // Add COLLATE LOCALIZED to deal with special characters.
            sortOrder = ListContract.ListEntry.COLUMN_STRING + " COLLATE LOCALIZED ASC";
        }
        return sortOrder;
    }

    static int getDirectionFromPrefs(Context context, SharedPreferences sharedPreferences) {
        String directionString = sharedPreferences.getString(
                context.getString(R.string.pref_direction_key),
                context.getString(R.string.pref_direction_right_value));
        if (directionString.equals(context.getString(R.string.pref_direction_right_value)))
            return ItemTouchHelper.RIGHT;
        else if (directionString.equals(context.getString(R.string.pref_direction_left_value)))
            return ItemTouchHelper.LEFT;
        else return ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
    }

    // Sets the value of 'alarm_on' which indicated if a reminder is planned.
    // If it is on (true), then also sets the planned time as a String representation.
    static void setAlarmIndicator(Context context, SharedPreferences sharedPreferences, boolean is_on, String time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.alarm_on), is_on);
        if (is_on && time != null) editor.putString(context.getString(R.string.alarm_time), time);
        editor.apply();
    }

    // Sets the selected day of the alarm between the three possibles options:
    // today, tomorrow, aftertomorrow
    static void setDayOfAlarm(Context context, SharedPreferences sharedPreferences, String day) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.day_alarm), day);
        editor.apply();
    }

    // Sets the value in real time of the day of the alarm
    static void setRealDayOfAlarm(Context context, SharedPreferences sharedPreferences, String realDay) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.real_day_alarm), realDay);
        editor.apply();
    }
}
