package com.example.android.liste.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The contract defines the constants of the database which contains two tables.
 */

public class ListContract {

    // The name of the Content Provider
    static final String CONTENT_AUTHORITY = "com.example.android.liste";
    // Each table has a specific identifier appended to the base Uri.
    static final String PATH_LIST = "list";
    static final String PATH_HISTORY = "history";
    // All Uris will be formed using this base
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private ListContract() {}

    // Defines the table contents storing the current grocery list.
    // BaseColumns adds the unique _ID column name.
    public static class ListEntry implements BaseColumns {

        // The Uri identifying this table in the Content Provider.
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_LIST);
        // The names of the columns of the table are defined here.
        // Only one column for now : the text identifying the item on the list.
        public static final String COLUMN_STRING = "string";
        public static final String COLUMN_PRIORITY = "priority";
        // Internal name for the table.
        static final String TABLE_NAME = "list";
    }

    // Defines the table contents storing the history of previously entered items.
    public static class HistoryEntry implements BaseColumns {

        // The Uri identifying this table in the Content Provider.
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_HISTORY);
        // The names of the columns of the table are defined here.
        // Only one column for now : the text identifying the item of the history.
        public static final String COLUMN_STRING = "string";
        // Internal name for the table.
        static final String TABLE_NAME = "history";
    }
}
