package com.example.android.liste.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The contract defines the constants of the database which contains two tables.
 */

public class ListContract {

    // The name of the Content Provider
    public static final String CONTENT_AUTHORITY = "com.example.android.liste";
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
        // Three columns for now corrresponding to: the name of the product, an annotation and the priority.
        public static final String COLUMN_PRODUCT = "product";
        public static final String COLUMN_ANNOTATION = "annotation";
        public static final String COLUMN_PRIORITY = "priority";
        // Possibles values for priority column
        public static final int HIGH_PRIORITY_PRODUCT = 1;
        public static final int DEFAULT_PRIORITY_PRODUCT = 2;
        public static final int LOW_PRIORITY_PRODUCT = 3;
        // Internal name for the table.
        static final String TABLE_NAME = "list";
    }

    // Defines the table contents storing the history of previously entered items.
    public static class HistoryEntry implements BaseColumns {

        // The Uri identifying this table in the Content Provider.
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_HISTORY);
        // The names of the columns of the table are defined here.
        // Only one column for now : the text corresponding to the name of the product of the history.
        public static final String COLUMN_PRODUCT = "product";
        // Internal name for the table.
        static final String TABLE_NAME = "history";
    }
}
