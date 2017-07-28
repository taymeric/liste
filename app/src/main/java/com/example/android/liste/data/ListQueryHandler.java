package com.example.android.liste.data;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;

/**
 * AsyncQueryHandler is used from the main thread to perform simple Content Provider operations
 * on a background thread.
 */
public class ListQueryHandler extends AsyncQueryHandler {

    public static final int INSERTION_LIST = 1;
    public static final int INSERTION_HISTORY = 2;
    public static final int DELETION_LIST = 3;
    public static final int UPDATE_LIST = 4;

    public ListQueryHandler(ContentResolver cr) {
        super(cr);
    }

}
