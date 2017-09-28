package com.example.android.liste.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/** ListContentProvider handles database related operations in the application. */
public class ListContentProvider extends ContentProvider {

    /* UriMatcher code for list table match */
    private static final int LIST = 100;

    /* UriMatcher code for list table item match */
    private static final int LIST_ID = 200;

    /* UriMatcher code for history table match */
    private static final int HISTORY = 300;

    /* UriMatcher code for history table item match */
    private static final int HISTORY_ID = 400;

    /* UriMatcher will match a given Uri with these templates and return a code to identify
     * the table and the specificity of the Uri (single row or full table). */
    final private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(ListContract.CONTENT_AUTHORITY, ListContract.PATH_LIST, LIST);
        sUriMatcher.addURI(ListContract.CONTENT_AUTHORITY, ListContract.PATH_LIST + "/#", LIST_ID);
        sUriMatcher.addURI(ListContract.CONTENT_AUTHORITY, ListContract.PATH_HISTORY, HISTORY);
        sUriMatcher.addURI(ListContract.CONTENT_AUTHORITY, ListContract.PATH_HISTORY + "/#", HISTORY_ID);
    }

    private ListDbHelper mListDbHelper;

    @Override
    public boolean onCreate() {
        mListDbHelper = new ListDbHelper(getContext());
        return true;
    }

    // required
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    // Used by Cursor Loaders to query data for a table.
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        SQLiteDatabase db = mListDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case LIST:
                cursor = db.query(ListContract.ListEntry.TABLE_NAME,
                        strings,
                        s,
                        strings1,
                        null,
                        null,
                        s1);
                break;
            case LIST_ID:
                String selection = ListContract.ListEntry._ID + "=?";
                String [] selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = db.query(ListContract.ListEntry.TABLE_NAME,
                        strings,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        s1);
                break;
            case HISTORY:
                cursor = db.query(ListContract.HistoryEntry.TABLE_NAME,
                        strings,
                        s,
                        strings1,
                        null,
                        null,
                        s1);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Set a notification URI on the Cursor and return that Cursor
        // The Notification Uri allows the cursor to be notified of changes
        // by the other methods (delete, insert, update) that call notifyChange(uri)
        if (getContext() != null) cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the desired Cursor
        return cursor;
    }

    // Used by both activities to add data to both tables.
    // Checks for conflicts : if the inserted string is already in the table then returns Uri.EMPTY
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase db = mListDbHelper.getWritableDatabase();
        Uri returnUri = Uri.EMPTY;
        long id = 0 ;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case LIST:
                try {
                    id = db.insertWithOnConflict(ListContract.ListEntry.TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
                    if (id != -1) {
                        // If id == -1, don't update the value of returnUri from Uri.EMPTY
                        // in order to notify of a duplication (and no insertion).
                        returnUri = ContentUris.withAppendedId(ListContract.ListEntry.CONTENT_URI, id);
                    }
                }
                catch (SQLiteConstraintException exception) {
                    // There is a duplication, don't update returnUri
                }
                break;
            case HISTORY:
                try {
                    id = db.insertWithOnConflict(ListContract.HistoryEntry.TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
                    if (id != -1) {
                        returnUri = ContentUris.withAppendedId(ListContract.HistoryEntry.CONTENT_URI, id);
                    }
                }
                catch (SQLiteConstraintException exception) {
                    // There is a duplication, don't update returnUri
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (id > 0) {
            if (getContext() != null) getContext().getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }

    // Used by both activities to delete an entries or all of them from their respective table.
    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        SQLiteDatabase db = mListDbHelper.getWritableDatabase();
        int rowsDeleted;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case LIST:
                rowsDeleted = db.delete(ListContract.ListEntry.TABLE_NAME, s, strings);
                break;
            case LIST_ID:
                String selection = ListContract.ListEntry._ID + "=?";
                String [] selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = db.delete(ListContract.ListEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case HISTORY:
                rowsDeleted = db.delete(ListContract.HistoryEntry.TABLE_NAME, s, strings);
                break;
            case HISTORY_ID:
                String selection2 = ListContract.HistoryEntry._ID + "=?";
                String [] selectionArgs2 = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = db.delete(ListContract.HistoryEntry.TABLE_NAME, selection2, selectionArgs2);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            if (getContext() != null) getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    // Update is only used for LIST_ID when updating priority or annotation
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        SQLiteDatabase db = mListDbHelper.getWritableDatabase();
        int rowsUpdated;
        int match = sUriMatcher.match(uri);
        switch(match) {
            case LIST_ID:
                String selection = ListContract.ListEntry._ID + "=?";
                String [] selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsUpdated = db.update(ListContract.ListEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case HISTORY_ID:
                String selection2 = ListContract.HistoryEntry._ID + "=?";
                String [] selectionArgs2 = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsUpdated = db.update(ListContract.HistoryEntry.TABLE_NAME, contentValues, selection2, selectionArgs2);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            if (getContext() != null) getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    // Returns the number of new rows
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {

        Uri newUri;
        int nb = 0;
        for (ContentValues cv : values) {
            newUri = insert(uri, cv);
            if (newUri != null && !newUri.equals(Uri.EMPTY)) nb++;
        }
        return nb;
    }
}
