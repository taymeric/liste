package com.example.android.liste;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

import com.example.android.liste.data.ListContract;
import com.example.android.liste.data.ListQueryHandler;

/**
 * Utility methods for operations that require access to the database.
 */
class DataUtils {

    /** Inserts a product to both list and history tables.
     *  Called when a product is entered from the ActionView in the AppBar of ListActivity.
     *  @param listQueryHandler needed to perform insertion with ContentProvider
     *  @param product the name of the product to be inserted */
    static void insertProductIntoBothTables(ListQueryHandler listQueryHandler, String product) {

        // Add product to the list table with a default priority
        ContentValues values = new ContentValues();
        values.put(ListContract.ListEntry.COLUMN_PRODUCT, product);
        values.put(ListContract.ListEntry.COLUMN_PRIORITY,
                ListContract.ListEntry.DEFAULT_PRIORITY_PRODUCT);
        listQueryHandler.startInsert(
                ListQueryHandler.INSERTION_LIST, null, ListContract.ListEntry.CONTENT_URI, values);

        // Add product to the history table
        values = new ContentValues();
        values.put(ListContract.HistoryEntry.COLUMN_PRODUCT, product);
        listQueryHandler.startInsert(
                ListQueryHandler.INSERTION_HISTORY, null, ListContract.HistoryEntry.CONTENT_URI, values);
    }

    /** @return a Notification object containing:
     *  - the number of products in the list in its title
     *  - the list of products in its body */
    static Notification createNotification(Context context) {

        Uri uri = ListContract.ListEntry.CONTENT_URI;
        // For notifications, the whole list may not be entirely visible, so we sort by priority,
        // regardless of the user's preference, in order to get important products first.
        String sortOrder = ListContract.ListEntry.COLUMN_PRIORITY + " ASC, "
                + ListContract.ListEntry.COLUMN_PRODUCT + " COLLATE LOCALIZED ASC";
        // We also don't want to show annotations to save space.
        String[] projection  = {ListContract.ListEntry.COLUMN_PRODUCT,
                ListContract.ListEntry.COLUMN_PRIORITY};

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder);

        // Retrieve number of products on the list to set title of notification
        int nbOfProducts;
        if (cursor != null) nbOfProducts = cursor.getCount();
        else nbOfProducts = 0;
        String title = context.getResources().getQuantityString(R.plurals.list_reminder_notification_title, nbOfProducts, nbOfProducts);

        // Build a String representation of all the products in the list by iterating through the cursor
        StringBuilder builder = new StringBuilder();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                builder.append(cursor.getString(cursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRODUCT)));
                builder.append(", ");
            }
            // Don't forget to close the cursor
            cursor.close();
        }
        String list = builder.toString();
        // Remove extra ', ' unless the list was empty
        if (!list.isEmpty()) list = list.substring(0, list.length()-2);

        // Creates an explicit intent for an Activity in the app
        Intent resultIntent = new Intent(context, ListActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //mBuilder.setContentIntent(resultPendingIntent);

        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder)
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_shopping_basket_white_24dp)
                        .setContentTitle(title)
                        .setContentText(list)
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        //.setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .addAction(R.drawable.ic_open_in_new_white_24dp,
                                context.getString(R.string.list_reminder_notification_button),
                                resultPendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(list));

        return mBuilder.build();
    }

    /* @return a String representation of the whole list with products, annotations and priorities
     *  to be used when sharing the list by mail. */
    /*static String getListAsStringForEmail(Context context, SharedPreferences sharedPreferences) {

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
                if (p == ListContract.ListEntry.HIGH_PRIORITY_PRODUCT)
                    list = list + " " + context.getString(R.string.list_high_priority_mark);
                else if (p == ListContract.ListEntry.LOW_PRIORITY_PRODUCT)
                    list = list + " " + context.getString(R.string.list_low_priority_mark);

                list = list + "\n";
            }
            cursor.close();
        }

        return list;
    }*/
}
