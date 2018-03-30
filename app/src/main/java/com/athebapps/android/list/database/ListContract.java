package com.athebapps.android.list.database;

import android.net.Uri;
import android.provider.BaseColumns;


/**
 * The contract defines the constants of the database which contains two tables.
 */
public class ListContract {

    /** The authority for the Content Provider */
    public static final String CONTENT_AUTHORITY = "com.athebapps.android.list";
    /** Identifier of the list table (to append to the base Uri) */
    static final String PATH_LIST = "list";
    /** Identifier of the history table (to append to the base Uri) */
    static final String PATH_HISTORY = "history";
    /** All Uris will be formed using this base */
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private ListContract() {}

    /** Defines the table contents storing the current grocery list.
     *  BaseColumns adds the unique _ID column name. */
    public static class ListEntry implements BaseColumns {

        /** The Uri identifying this table in the Content Provider. */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_LIST);
        /** Column corresponding to the name of the product */
        public static final String COLUMN_PRODUCT = "product";
        /** Column corresponding to the annotation of the product */
        public static final String COLUMN_ANNOTATION = "annotation";
        /** Column corresponding to the priority of the product */
        public static final String COLUMN_PRIORITY = "priority";
        /** COLUMN_PRIORITY value for high priority */
        public static final int HIGH_PRIORITY_PRODUCT = 1;
        /** COLUMN_PRIORITY value for default priority */
        public static final int DEFAULT_PRIORITY_PRODUCT = 2;
        /** COLUMN_PRIORITY value for low priority */
        public static final int LOW_PRIORITY_PRODUCT = 3;
        /** Internal name for the table. */
        static final String TABLE_NAME = "list";
    }

    /** Defines the table contents storing the history of previously entered items. */
    public static class HistoryEntry implements BaseColumns {

        /** The Uri identifying this table in the Content Provider. */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_HISTORY);
        /** The only column for the history table: the text corresponding to the name of the product
         *  of the history. There is no normalization of the database: the name of the products are
         *  repeated in both tables. The reason is it is possible to have a product in one of the
         *  tables and not int the other. */
        public static final String COLUMN_PRODUCT = "product";
        /** Internal name for the table. */
        static final String TABLE_NAME = "history";
    }
}
