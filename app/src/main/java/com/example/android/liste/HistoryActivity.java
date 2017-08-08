package com.example.android.liste;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.example.android.liste.data.ListContract;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * HistoryActivity displays the items in the history table.
 * The user can:
 *  - select one or several items to add to the list table
 *  - select one or several items to remove from the history table
 *  - remove all items from the history table
 */

public class HistoryActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
            HistoryAdapter.AdapterOnClickHandler {

    private static final int HISTORY_LOADER_ID = 99;

    private FloatingActionButton mFab;
    private View mEmptyView;
    private ProgressBar mProgressBar;
    private HistoryAdapter mAdapter;

    // An HashMap is used to store (id, text) pairs of selected elements with no duplication.
    // id is used for deletion and text is used for insertion.
    private HashMap<String, String> selectedIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setTitle(getString(R.string.history_title));

        selectedIds = new HashMap<>();

        mFab = (FloatingActionButton) findViewById(R.id.floatingActionButtonHistory);
        mFab.hide();
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSelectedEntries();
                finish();
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Set up the Recycler View with its Adapter
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.history_recycler_view);
        RecyclerView.LayoutManager layoutManager = PreferenceUtils.getHistoryLayoutManager(this, sharedPreferences);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new HistoryAdapter(this, this);
        recyclerView.setAdapter(mAdapter);

        mEmptyView = findViewById(R.id.empty_view);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        getLoaderManager().initLoader(HISTORY_LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_history, menu);
        MenuItem trash = menu.findItem(R.id.action_clear);
        if (selectedIds != null) trash.setVisible(!selectedIds.isEmpty());
        else trash.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_clear:
                deleteSelectedEntries();
                return true;
            case android.R.id.home:
                showConfirmationDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteSelectedEntries() {
        new AlertDialog.Builder(HistoryActivity.this)
                .setMessage(getResources().getQuantityString(R.plurals.history_clear_selection_title, selectedIds.size()))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // We use ContentProviderOperations to delete the selected products in one
                        // batch operation.
                        // First, we iterate through all the Ids contained in the HashMap of selected
                        // products and create our list of deletion operations to be performed.
                        int nb = 0;
                        Uri uri;
                        ArrayList<ContentProviderOperation> deleteOperations = new ArrayList<>();
                        ContentProviderOperation operation;
                        for (String id : selectedIds.keySet()) {
                            uri = ListContract.HistoryEntry.CONTENT_URI;
                            uri = uri.buildUpon().appendPath(id).build();
                            operation = ContentProviderOperation.newDelete(uri).build();
                            deleteOperations.add(operation);
                        }
                        try {
                            ContentProviderResult[] results = getContentResolver().applyBatch(ListContract.CONTENT_AUTHORITY, deleteOperations);

                            // Check for results of deletion operations
                            for (ContentProviderResult result : results) {
                                nb += result.count;
                            }

                        }
                        catch (RemoteException | OperationApplicationException exception) {}

                        showMessage(getResources().getQuantityString(R.plurals.history_products_cleared_message, nb, nb));

                        selectedIds.clear();
                        setFabVisibility();
                        setEmptyViewVisibility();
                        mAdapter.notifyDataSetChanged();
                        invalidateOptionsMenu();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create().show();
    }

    private void showConfirmationDialog() {
        if (selectedIds != null && !selectedIds.isEmpty()) {
            new AlertDialog.Builder(HistoryActivity.this)
                    .setMessage(getResources().getQuantityString(R.plurals.history_add_selected_products, selectedIds.size()))
                    .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            addSelectedEntries();
                            finish();
                        }
                    })
                    .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setResult(RESULT_CANCELED, null);
                            finish();
                        }
                    })
                    .create().show();
        } else {
            finish();
        }
    }

    private void addSelectedEntries() {
        Uri uri = ListContract.ListEntry.CONTENT_URI;
        ContentValues[] cv_all = new ContentValues[selectedIds.size()];
        // Iterate through all the text values contained in the HasMap of selected elements
        // and add elements with those texts to the list table.
        int i = 0;
        for (String value: selectedIds.values()) {
            ContentValues cv = new ContentValues();
            cv.put(ListContract.ListEntry.COLUMN_PRODUCT, value);
            cv.put(ListContract.ListEntry.COLUMN_PRIORITY, ListActivity.DEFAULT_PRIORITY);
            cv_all[i] = cv;
            i++;
        }

        // Here we use bulkinsert() to insert multiples lines at once as it is more efficient than
        // calling insert() multiple times. We could also use ContentProviderOperations.
        // We don't use an AsyncQueryHandler as it is not straightforward to refactor this code
        // with it and I also don't know if it is suited for bulk operations.
        int nb = getContentResolver().bulkInsert(uri, cv_all);
        Intent intent = new Intent();
        if (nb == 0)
            intent.putExtra(getString(R.string.history_message_to_list), getString(R.string.list_no_new_product_message));
        else
            intent.putExtra(getString(R.string.history_message_to_list), getResources().getQuantityString(R.plurals.list_new_products_message, nb, nb));
        setResult(RESULT_OK, intent);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case HISTORY_LOADER_ID :
                String order = ListContract.HistoryEntry.COLUMN_PRODUCT + " COLLATE LOCALIZED ASC";
                return new CursorLoader(
                        this,    // Parent activity context
                        ListContract.HistoryEntry.CONTENT_URI,    // Table to query
                        null,    // Projection to return
                        null,    // No selection clause
                        null,    // No selection arguments
                        order    // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mProgressBar.setVisibility(View.GONE);
        mAdapter.swapCursor(cursor);
        setEmptyViewVisibility();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProgressBar.setVisibility(View.VISIBLE);
        mAdapter.swapCursor(null);
        setEmptyViewVisibility();
    }

    /**
     * Shows a short Snackbar message.
     */
    private void showMessage(String message) {
        Snackbar.make(findViewById(R.id.history), message, Snackbar.LENGTH_SHORT).show();
    }

    // Callback method of HistoryAdapter.AdapterOnClickHandler.
    // Allows HistoryAdapter to update the HashMap containing the selected items.
    @Override
    public void onClick(String id, String txt) {
        if (selectedIds.containsKey(id))
            selectedIds.remove(id);
        else selectedIds.put(id, txt);
        setFabVisibility();
        invalidateOptionsMenu();
    }

    // Updates the visibility of the Floating Action Button.
    // If selection is empty, the FAB is invisible.
    private void setFabVisibility() {
        if (!selectedIds.isEmpty() && !mFab.isShown()) {
            mFab.show();
        }
        else if (selectedIds.isEmpty() && mFab.isShown()) {
            mFab.hide();
        }
    }

    // If there are no elements in the history, display a message in an 'empty' view
    private void setEmptyViewVisibility() {
        if (mAdapter.getItemCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }
}
