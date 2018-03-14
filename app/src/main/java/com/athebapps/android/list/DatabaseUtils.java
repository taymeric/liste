package com.athebapps.android.list;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.athebapps.android.list.database.ListContract;
import com.athebapps.android.list.database.ListQueryHandler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Utility methods for operations that require access to the database,
 * using the Content Provider or Cursors.
 */
class DatabaseUtils {

    private static final String TAG = "DatabaseUtils.java";

    /** Inserts a product into the list table.
     *  Called when a product is entered from the ActionView in the AppBar of ListActivity.
     *  If the product is already in the table, it is not added a second time as COLUMN_PRODUCT in
     *  list table is UNIQUE.
     *  @param listQueryHandler needed to perform insertion with ContentProvider on background thread
     *  @param product the name of the product to be inserted */
    static void insertProductIntoListTable(ListQueryHandler listQueryHandler, String product) {

        ContentValues values = new ContentValues();
        values.put(ListContract.ListEntry.COLUMN_PRODUCT, product);
        values.put(ListContract.ListEntry.COLUMN_PRIORITY,
                ListContract.ListEntry.DEFAULT_PRIORITY_PRODUCT);
        listQueryHandler.startInsert(
                ListQueryHandler.INSERTION_LIST, null, ListContract.ListEntry.CONTENT_URI, values);
    }

    /** Inserts a product into the history table.
     *  Called when a product is entered from the ActionView in the AppBar of ListActivity.
     *  If the product is already in the table, it is not added a second time as COLUMN_PRODUCT in
     *  history table is UNIQUE.
     *  @param listQueryHandler needed to perform insertion with ContentProvider on background thread
     *  @param product the name of the product to be inserted */
    static void insertProductIntoHistoryTable(ListQueryHandler listQueryHandler, String product) {

        ContentValues values = new ContentValues();
        values.put(ListContract.HistoryEntry.COLUMN_PRODUCT, product);
        listQueryHandler.startInsert(
                ListQueryHandler.INSERTION_HISTORY, null, ListContract.HistoryEntry.CONTENT_URI, values);
    }

    /**
     * Deletes a product from the list table identified by its id.
     * The values corresponding to this product (name, priority, annotation) are saved and returned.
     * @param listQueryHandler needed to perform insertion with ContentProvider on background thread
     * @param cu the cursor pointing to the product in the table to delete
     * @return (product, priority, annotation) of the deleted product
     */
    static @Nullable ArrayList<String> deleteProductFromListTable(ListQueryHandler listQueryHandler, Cursor cu) {

        if (cu!=null && cu.moveToFirst()) {

            // Save values for result
            String product = cu.getString(cu.getColumnIndex(ListContract.ListEntry.COLUMN_PRODUCT));
            int priority = cu.getInt(cu.getColumnIndex(ListContract.ListEntry.COLUMN_PRIORITY));
            String annotation = cu.getString(cu.getColumnIndex(ListContract.ListEntry.COLUMN_ANNOTATION));
            String id = cu.getString(cu.getColumnIndex(ListContract.ListEntry._ID));
            Uri uri = ListContract.ListEntry.CONTENT_URI;
            uri = uri.buildUpon().appendPath(id).build();

            ArrayList<String> deletedValues = new ArrayList<>();
            deletedValues.add(product);
            deletedValues.add(String.valueOf(priority));
            deletedValues.add(annotation);

            cu.close();

            // Call for actual deletion
            listQueryHandler.startDelete(ListQueryHandler.DELETION_LIST, null, uri, null, null);

            return deletedValues;
        }
        else return null;
    }

    /**
     * Updates the priority and the annotation of a product of the list table.
     * @param listQueryHandler needed to perform insertion with ContentProvider on background thread
     * @param uri the uri identifying the product
     * @param priority the new priority for the product
     * @param annotation the new annotation for the product
     */
    static void updateProductPriorityAndAnnotation(ListQueryHandler listQueryHandler, Uri uri, int priority, String annotation) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(ListContract.ListEntry.COLUMN_PRIORITY, priority);
        contentValues.put(ListContract.ListEntry.COLUMN_ANNOTATION, annotation);

        listQueryHandler.startUpdate(ListQueryHandler.UPDATE_LIST, null, uri,
                contentValues, null, null);
    }

    /**
     * Inserts several products into the list table.
     * @param context needed to get access to Content Resolver
     * @param products the (id, name) pairs of the products to be deleted, only id is used here
     * @return the number of products actually added to the list (not duplicates)
     */
    static int insertProductsIntoListTable(Context context, HashMap<String, String> products) {

        Uri uri = ListContract.ListEntry.CONTENT_URI;
        ContentValues[] cv_all = new ContentValues[products.size()];
        // Iterate through all the products in the HashMap
        // and add the name  (values) of these products to the list table.
        int i = 0;
        for (String value: products.keySet()) {
            ContentValues cv = new ContentValues();
            cv.put(ListContract.ListEntry.COLUMN_PRODUCT, value);
            cv.put(ListContract.ListEntry.COLUMN_PRIORITY, ListContract.ListEntry.DEFAULT_PRIORITY_PRODUCT);
            cv_all[i] = cv;
            i++;
        }

        // Here we use bulkInsert() to insert multiples lines at once as it is more efficient than
        // calling insert() multiple times. We could also use ContentProviderOperations.
        // AsyncQueryHandler does not allow bulkInsert therefore this implementation is not perfect
        // but I want to keep it as an example. Same for ContentOperations used for deletion below.
        return context.getContentResolver().bulkInsert(uri, cv_all);
    }

    /**
     * Deletes several products from the history table.
     * @param context needed to get access to Content Resolver
     * @param products the (id, name) pairs of the products to be deleted, only id is used here
     * @return the number of products deleted from history
     */
    static int deleteProductsFromHistoryTable(Context context, HashMap<String, String> products) {

        // We use ContentProviderOperations to delete the selected products in one
        // batch operation.
        // First, we iterate through all the Ids contained in the HashMap of selected
        // products and create our list of deletion operations to be performed.
        int nb = 0;
        Uri uri;
        ArrayList<ContentProviderOperation> deleteOperations = new ArrayList<>();
        ContentProviderOperation operation;
        for (String id : products.values()) {
            uri = ListContract.HistoryEntry.CONTENT_URI;
            uri = uri.buildUpon().appendPath(id).build();
            operation = ContentProviderOperation.newDelete(uri).build();
            deleteOperations.add(operation);
        }
        try {
            ContentProviderResult[] results = context.getContentResolver().applyBatch(ListContract.CONTENT_AUTHORITY, deleteOperations);

            // Check for results of deletion operations
            for (ContentProviderResult result : results) {
                nb += result.count;
            }

        }
        catch (RemoteException | OperationApplicationException exception) {
            Log.d(TAG, "Exception while deleting products from history");
        }

        return nb;
    }

    /**
     * Creates a notification object containing:
     *  - a title with the number of products
     *  - a body with the list of products
     *  - a button that launches the app
     * @param context used to get access to Content Resolver
     * @return the full Notification object (to be sent to the Notification Manager)
     */
    static Notification createNotificationFromListProducts(Context context) {

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
        String title;

        if (nbOfProducts == 0) title = context.getString(R.string.list_notification_title_empty);
        else title = context.getResources().getQuantityString(R.plurals.list_notification_title, nbOfProducts, nbOfProducts);

        // Build a String representation of all the products in the list by iterating through the cursor
        StringBuilder stringBuilder = new StringBuilder();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                stringBuilder.append(cursor.getString(cursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRODUCT)));
                stringBuilder.append(", ");
            }
            // Don't forget to close the cursor
            cursor.close();
        }
        String list = stringBuilder.toString();
        // Remove extra ', ' unless the list was empty
        if (!list.isEmpty()) list = list.substring(0, list.length()-2);

        // Creates an explicit intent for an Activity in the app
        Intent resultIntent = new Intent(context, ListActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder)
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_shopping_basket_white_24dp)
                        .setContentTitle(title)
                        .setContentText(list)
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        //.setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .addAction(R.drawable.ic_open_in_new_white_24dp,
                                context.getString(R.string.list_notification_button),
                                resultPendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(list));

        return notificationBuilder.build();
    }

    /**
     * Formats the list as a String to be used in the body of an email.
     * @param context used to access String values
     * @param cursor a Cursor pointing to the table representing the list
     * @return the String representation of the whole list with products, annotations and priorities.
     */
    @NonNull
    static String formatListForEmail(Context context, Cursor cursor) {

        StringBuilder list = new StringBuilder();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.append("- ");
                list.append(cursor.getString(cursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRODUCT)));

                String annotation = cursor.getString(cursor.getColumnIndex(ListContract.ListEntry.COLUMN_ANNOTATION));
                if (annotation != null && !annotation.equals("")) {
                    list.append(" (");
                    list.append(annotation);
                    list.append(")");
                }

                int p = cursor.getInt(cursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRIORITY));
                if (p == ListContract.ListEntry.HIGH_PRIORITY_PRODUCT) {
                    list.append(" ");
                    list.append(context.getString(R.string.list_high_priority_mark));
                }
                else if (p == ListContract.ListEntry.LOW_PRIORITY_PRODUCT) {
                    list.append(" ");
                    list.append(context.getString(R.string.list_low_priority_mark));
                }

                list.append("\n");
            }
            cursor.close();
        }
        return list.toString();
    }
}
