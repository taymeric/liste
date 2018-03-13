package com.athebapps.android.list.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Locale;


/**
 * A helper class for database creation and management.
 */
class ListDbHelper extends SQLiteOpenHelper {

    /* The database version number. To be incremented every time the database schema is changed. */
    private static final int DATABASE_VERSION = 5;

    /* The name of the file for the database */
    private static final String DATABASE_NAME = "list.db";

    /* SQL query corresponding to the creation of the list table */
    private static final String SQL_CREATE_LIST_ENTRIES =
            "CREATE TABLE " + ListContract.ListEntry.TABLE_NAME + " ("
                    + ListContract.ListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ListContract.ListEntry.COLUMN_PRODUCT + " TEXT NOT NULL, "
                    + ListContract.ListEntry.COLUMN_ANNOTATION + " TEXT, "
                    + ListContract.ListEntry.COLUMN_PRIORITY + " INTEGER NOT NULL, "
                    + " UNIQUE (" + ListContract.ListEntry.COLUMN_PRODUCT + ") ON CONFLICT IGNORE);";

    /* SQL query corresponding to the deletion of the list table */
    private static final String SQL_DELETE_LIST_ENTRIES =
            "DROP TABLE IF EXISTS " + ListContract.ListEntry.TABLE_NAME;

    /* SQL query corresponding to the creation of the history table */
    private static final String SQL_CREATE_HISTORY_ENTRIES =
            "CREATE TABLE " + ListContract.HistoryEntry.TABLE_NAME + " ("
                    + ListContract.HistoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ListContract.HistoryEntry.COLUMN_PRODUCT + " TEXT NOT NULL, "
                    + " UNIQUE (" + ListContract.HistoryEntry.COLUMN_PRODUCT + ") ON CONFLICT IGNORE);";

    /* SQL query corresponding to the deletion of the history table */
    private static final String SQL_DELETE_HISTORY_ENTRIES =
            "DROP TABLE IF EXISTS " + ListContract.HistoryEntry.TABLE_NAME;

    ListDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /* Called when the database is created for the first time. */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.setLocale(Locale.getDefault());
        sqLiteDatabase.execSQL(SQL_CREATE_LIST_ENTRIES);
        sqLiteDatabase.execSQL(SQL_CREATE_HISTORY_ENTRIES);
    }

    /* Called whenever DATABASE_VERSION changes */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_LIST_ENTRIES);
        sqLiteDatabase.execSQL(SQL_DELETE_HISTORY_ENTRIES);
        onCreate(sqLiteDatabase);
    }
}
