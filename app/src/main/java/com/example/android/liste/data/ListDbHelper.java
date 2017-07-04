package com.example.android.liste.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Locale;

/**
 * Created by aymeric on 17-04-27.
 */

public class ListDbHelper extends SQLiteOpenHelper {

    // To be incremented every time the database schema is changed.
    public static final int DATABASE_VERSION = 2;

    public static final String DATABASE_NAME = "list.db";

    public static final String SQL_CREATE_LIST_ENTRIES =
            "CREATE TABLE " + ListContract.ListEntry.TABLE_NAME + " ("
            + ListContract.ListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ListContract.ListEntry.COLUMN_STRING + " TEXT UNIQUE)";

    public static final String SQL_DELETE_LIST_ENTRIES =
            "DROP TABLE IF EXISTS " + ListContract.ListEntry.TABLE_NAME;

    public static final String SQL_CREATE_HISTORY_ENTRIES =
            "CREATE TABLE " + ListContract.HistoryEntry.TABLE_NAME + " ("
                    + ListContract.HistoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ListContract.HistoryEntry.COLUMN_STRING + " TEXT UNIQUE)";

    public static final String SQL_DELETE_HISTORY_ENTRIES =
            "DROP TABLE IF EXISTS " + ListContract.HistoryEntry.TABLE_NAME;


    public ListDbHelper(Context context) {
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
