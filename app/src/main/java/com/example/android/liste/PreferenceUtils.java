package com.example.android.liste;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.android.liste.data.ListContract;

/**
 * Utility methods to get settings values in accordance with preference settings
 */
public class PreferenceUtils {

    public static int getHistoryColorFromPrefs(Context context, SharedPreferences sharedPreferences) {
        int color;
        String colorString = sharedPreferences.getString(
                context.getString(R.string.pref_color_key),
                context.getString(R.string.pref_color_green_value));
        if (colorString.equals(context.getString(R.string.pref_color_green_value))) {
            color = ContextCompat.getColor(context, R.color.colorGreen);
        } else {
            color = ContextCompat.getColor(context, android.R.color.white);
        }
        return color;
    }

    /*
     * In this method, the parameter 'key' differentiate between the RecylerView for the list
     * and the RecyclerView for the history.
     */
    public static float getTextSizeFromPrefs(
            Context context, SharedPreferences sharedPreferences, String key) {
        float size;
        String sizeString = sharedPreferences.getString(
                key,
                context.getString(R.string.pref_size_small_value));
        if (sizeString.equals(context.getString(R.string.pref_size_big_value))) {
            size = context.getResources().getDimension(R.dimen.text_big);
        } else {
            size = context.getResources().getDimension(R.dimen.text_small);
        }
        return size;
    }

    /*
     * In this method, the parameter 'key' differentiate between the RecylerView for the list
     * and the RecyclerView for the history.
     */
    public static RecyclerView.LayoutManager getLayoutFromPrefs(
            Context context, SharedPreferences sharedPreferences, String key) {

        String value = sharedPreferences.getString(
                key,
                context.getString(R.string.pref_layout_linear_value));
        if (value.equals(context.getString(R.string.pref_layout_grid_value))) {
            int orientation = context.getResources().getConfiguration().orientation;
            // Returns a GridLayout with more columns in landscape mode to accomodate
            // the larger width.
            if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                return new GridLayoutManager(context, 3);
            else
                return new GridLayoutManager(context, 2);
        }
        else
            return new LinearLayoutManager(context);
    }

    public static String getSortOrderFromPrefs(Context context, SharedPreferences sharedPreferences) {
        String sortOrder = sharedPreferences.getString(
                context.getString(R.string.pref_sort_order_key),
                context.getString(R.string.pref_sort_order_name_value));
        if (sortOrder.equals(context.getString(R.string.pref_sort_order_added_value))) {
            sortOrder = ListContract.ListEntry._ID + " ASC";
        } else {
            // Add COLLATE LOCALIZED to deal with special characters.
            sortOrder = ListContract.ListEntry.COLUMN_STRING + " COLLATE LOCALIZED ASC";
        }
        return sortOrder;
    }
}
