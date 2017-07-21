package com.example.android.liste;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import com.example.android.liste.data.ListContract;

import static com.example.android.liste.ListActivity.DEFAULT_PRIORITY;
import static com.example.android.liste.ListActivity.HIGH_PRIORITY;
import static com.example.android.liste.ListActivity.LOW_PRIORITY;

/**
 * Utility methods for database-related operations.
 */

class DataUtils {

    static void insertProductIntoBothTables(Context context, String product) {

        // Add value to the list table with a default priority
        ContentValues values = new ContentValues();
        values.put(ListContract.ListEntry.COLUMN_PRODUCT, product);
        values.put(ListContract.ListEntry.COLUMN_PRIORITY, DEFAULT_PRIORITY);
        context.getContentResolver().insert(ListContract.ListEntry.CONTENT_URI, values);

        // Add text to the history table
        values = new ContentValues();
        values.put(ListContract.HistoryEntry.COLUMN_PRODUCT, product);
        context.getContentResolver().insert(ListContract.HistoryEntry.CONTENT_URI, values);
    }

    static String getListAsStringForNotification(Context context, SharedPreferences sharedPreferences) {

        String list = "";

        Uri uri = ListContract.ListEntry.CONTENT_URI;

        // For notifications, the whole list may not be entirely visible, so we sort by priority,
        // regardless of the user's preference, in order to get important products first.
        String sortOrder = ListContract.ListEntry.COLUMN_PRIORITY + " ASC, "
                + ListContract.ListEntry.COLUMN_PRODUCT + " COLLATE LOCALIZED ASC";
        // We also don't want to show annotations to save space.
        String[] projection  = {ListContract.ListEntry.COLUMN_PRODUCT, ListContract.ListEntry.COLUMN_PRIORITY};

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list += cursor.getString(cursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRODUCT))
                            + ", ";
            }
            cursor.close();
        }
        // Remove extra ', ' unless the list was empty
        if (!list.isEmpty()) list = list.substring(0, list.length()-2);

        return list;
    }

    static String getListAsStringForEmail(Context context, SharedPreferences sharedPreferences) {

        String list = "";

        Uri uri = ListContract.ListEntry.CONTENT_URI;

        // In the case of email, we want the sort order to be the same as the one in the app.
        String sortOrder = PreferenceUtils.getSortOrder(context, sharedPreferences);
        // We also want to show the full list with annotations.
        String[] projection  = {ListContract.ListEntry.COLUMN_PRODUCT,
                ListContract.ListEntry.COLUMN_PRIORITY, ListContract.ListEntry.COLUMN_ANNOTATION};

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list += "- " + cursor.getString(cursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRODUCT));

                String annotation = cursor.getString(cursor.getColumnIndex(ListContract.ListEntry.COLUMN_ANNOTATION));
                if (annotation != null && !annotation.equals("")) list += " ( " + annotation + " )";

                int p = cursor.getInt(cursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRIORITY));
                if (p == HIGH_PRIORITY) list = list + " !";
                else if (p == LOW_PRIORITY) list = list + " ?";

                list = list + "\n";
            }
            cursor.close();
        }

        return list;
    }
}
