package com.athebapps.android.list.database;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;

/**
 * AsyncQueryHandler is used from the main thread to perform simple Content Provider operations
 * on a background thread. The class constants represent tokens that could be used to cancel a
 * given operation.
 */
public class ListQueryHandler extends AsyncQueryHandler {

    /** Identifies the insertion of a product in the list table */
    public static final int INSERTION_LIST = 1;

    /** Identifies the insertion of a product in the history table */
    public static final int INSERTION_HISTORY = 2;

    /** Identifies the deletion of a product from the list table */
    public static final int DELETION_LIST = 3;

    /** Identifies the update of a product of the list table*/
    public static final int UPDATE_LIST = 4;

    public ListQueryHandler(ContentResolver cr) {
        super(cr);
    }

}
