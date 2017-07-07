package com.example.android.liste.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Locale;

class ListDbHelper extends SQLiteOpenHelper {

    // To be incremented every time the database schema is changed.
    private static final int DATABASE_VERSION = 4;

    private static final String DATABASE_NAME = "list.db";

    private static final String SQL_CREATE_LIST_ENTRIES =
            "CREATE TABLE " + ListContract.ListEntry.TABLE_NAME + " ("
            + ListContract.ListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ListContract.ListEntry.COLUMN_STRING + " TEXT NOT NULL, "
            + ListContract.ListEntry.COLUMN_PRIORITY + " INTEGER NOT NULL, "
            + " UNIQUE (" + ListContract.ListEntry.COLUMN_STRING + ") ON CONFLICT IGNORE);";

    private static final String SQL_DELETE_LIST_ENTRIES =
            "DROP TABLE IF EXISTS " + ListContract.ListEntry.TABLE_NAME;

    private static final String SQL_CREATE_HISTORY_ENTRIES =
            "CREATE TABLE " + ListContract.HistoryEntry.TABLE_NAME + " ("
                    + ListContract.HistoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ListContract.HistoryEntry.COLUMN_STRING + " TEXT NOT NULL, "
                    + " UNIQUE (" + ListContract.HistoryEntry.COLUMN_STRING + ") ON CONFLICT IGNORE);";

    private static final String SQL_DELETE_HISTORY_ENTRIES =
            "DROP TABLE IF EXISTS " + ListContract.HistoryEntry.TABLE_NAME;


    ListDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database is created for the first time.
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.setLocale(Locale.getDefault());
        sqLiteDatabase.execSQL(SQL_CREATE_LIST_ENTRIES);
        sqLiteDatabase.execSQL(SQL_CREATE_HISTORY_ENTRIES);
    }

    // Called whenever DATABASE_VERSION changes
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_LIST_ENTRIES);
        sqLiteDatabase.execSQL(SQL_DELETE_HISTORY_ENTRIES);
        onCreate(sqLiteDatabase);
    }
}
