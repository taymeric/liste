package com.example.android.liste;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

import com.example.android.liste.data.ListContract;
import com.example.android.liste.data.ListQueryHandler;

import static com.example.android.liste.ListActivity.DEFAULT_PRIORITY;
import static com.example.android.liste.ListActivity.HIGH_PRIORITY;
import static com.example.android.liste.ListActivity.LOW_PRIORITY;

/**
 * Utility methods for database-related operations.
 */

class DataUtils {

    static void insertProductIntoBothTables(ListQueryHandler listQueryHandler, String product) {

        // Add value to the list table with a default priority
        ContentValues values = new ContentValues();
        values.put(ListContract.ListEntry.COLUMN_PRODUCT, product);
        values.put(ListContract.ListEntry.COLUMN_PRIORITY, DEFAULT_PRIORITY);
        listQueryHandler.startInsert(ListQueryHandler.INSERTION_LIST, null, ListContract.ListEntry.CONTENT_URI, values);

        // Add text to the history table
        values = new ContentValues();
        values.put(ListContract.HistoryEntry.COLUMN_PRODUCT, product);
        listQueryHandler.startInsert(ListQueryHandler.INSERTION_HISTORY, null, ListContract.HistoryEntry.CONTENT_URI, values);
    }

    static Notification getNotification(Context context) {

        Uri uri = ListContract.ListEntry.CONTENT_URI;
        // For notifications, the whole list may not be entirely visible, so we sort by priority,
        // regardless of the user's preference, in order to get important products first.
        String sortOrder = ListContract.ListEntry.COLUMN_PRIORITY + " ASC, "
                + ListContract.ListEntry.COLUMN_PRODUCT + " COLLATE LOCALIZED ASC";
        // We also don't want to show annotations to save space.
        String[] projection  = {ListContract.ListEntry.COLUMN_PRODUCT, ListContract.ListEntry.COLUMN_PRIORITY};

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder);

        // Retrieve number of products on the list to set title of notification
        int nbOfProducts;
        if (cursor != null) nbOfProducts = cursor.getCount();
        else nbOfProducts = 0;
        String title = context.getResources().getQuantityString(R.plurals.notification_title, nbOfProducts, nbOfProducts);

        // Build a String representation of all the products in the list by iterating through the cursor
        String list = "";
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list += cursor.getString(cursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRODUCT))
                        + ", ";
            }
            // Don't forget to close the cursor
            cursor.close();
        }
        // Remove extra ', ' unless the list was empty
        if (!list.isEmpty()) list = list.substring(0, list.length()-2);

        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder)
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_shopping_basket_white_24dp)
                        .setContentTitle(title)
                        .setContentText(list)
                        .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_VIBRATE);

        // Creates a largeIcon for the expanded view
        Resources res = context.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ic_shopping_basket_big_white_24px);
        mBuilder.setLargeIcon(largeIcon);

        // Creates an explicit intent for an Activity in the app
        Intent resultIntent = new Intent(context, ListActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        // Create the Expanded style
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(list));

        return mBuilder.build();
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
                if (annotation != null && !annotation.equals("")) list += " (" + annotation + ")";

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
