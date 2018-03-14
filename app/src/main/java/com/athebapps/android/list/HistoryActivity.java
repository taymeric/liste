package com.athebapps.android.list;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.athebapps.android.list.database.ListContract;

import java.util.HashMap;


/**
 * HistoryActivity displays the products in the history table, which stores products that have
 * been previously entered in the list by the user.
 * The user can:
 *  - select one or several items to add to the current list table
 *  - select one or several items to remove from the history table
 */
public class HistoryActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        HistoryAdapter.HistoryAdapterOnClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener {

    /* Helps the LoaderManager identify the loader for the history */
    private static final int HISTORY_LOADER_ID = 1000;

    /* A Floating Action Button that appears when at least one element has been selected and that
     * is used to confirm the addition of the selected element(s) to the list */
    private FloatingActionButton mFab;

    /* A View displayed in place of the RecyclerView is empty (when the table does not contain
     * any element) */
    private View mEmptyView;

    /* A ProgressBar that is only 'visible' when the history is loading (otherwise it is 'gone')*/
    private ProgressBar mProgressBar;

    /* A Reference to the Shared Preferences of the app, used to get the user's preferences */
    private SharedPreferences mSharedPreferences;

    /* The RecyclerView that displays the items of the history */
    private RecyclerView mRecyclerView;

    /* The LayoutManager that defines if the RecyclerView has one or two columns, depending on the
     * user's preferences */
    private RecyclerView.LayoutManager mLayoutManager;

    /* The Adapter that binds the data from the history table to the Recycler View */
    private HistoryAdapter mAdapter;

    /* An HashMap is used to store (text, id) pairs of selected elements.
     * id is used for deletion and text is used for insertion. */
    private HashMap<String, String> selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setTitle(getString(R.string.history_title));

        if (savedInstanceState != null)
            //noinspection unchecked
            selected = (HashMap<String, String >) savedInstanceState.getSerializable("selected");
        else
            selected = new HashMap<>();

        mFab = (FloatingActionButton) findViewById(R.id.floatingActionButtonHistory);
        updateFabVisibility();
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Clicking the Floating Action Button adds selected entries to the list
                // and closes the History activity.
                addSelectedProducts();
                finish();
            }
        });

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // Set up the Recycler View with its Adapter
        mRecyclerView = (RecyclerView) findViewById(R.id.history_recycler_view);
        mLayoutManager = PreferenceUtils.getHistoryLayoutManager(this, mSharedPreferences);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new HistoryAdapter(this, selected, this);
        mRecyclerView.setAdapter(mAdapter);

        // By default, the empty view's visibility is set to 'gone'
        mEmptyView = findViewById(R.id.empty_view);

        // Make the progress bar visible until the history has finished loading
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        getLoaderManager().initLoader(HISTORY_LOADER_ID, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the map of selected elements
        outState.putSerializable("selected", selected);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_history, menu);

        // Only show the menu item for deletion if at least one element is selected
        MenuItem trash = menu.findItem(R.id.action_clear);
        if (selected != null) trash.setVisible(!selected.isEmpty());
        else trash.setVisible(false);

        MenuItem compact_layout = menu.findItem(R.id.action_compact_layout);
        MenuItem normal_layout = menu.findItem(R.id.action_normal_layout);
        if (mSharedPreferences.getBoolean(getString(R.string.pref_history_compact_layout_key), true)) {
            compact_layout.setVisible(false);
            normal_layout.setVisible(true);
        } else {
            compact_layout.setVisible(true);
            normal_layout.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_clear:
                deleteSelectedProducts();
                return true;
            case R.id.action_compact_layout:
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean(getString(R.string.pref_history_compact_layout_key), true);
                editor.apply();
                invalidateOptionsMenu();
                return true;
            case R.id.action_normal_layout:
                SharedPreferences.Editor editor2 = mSharedPreferences.edit();
                editor2.putBoolean(getString(R.string.pref_history_compact_layout_key), false);
                editor2.apply();
                invalidateOptionsMenu();
                return true;
            case android.R.id.home:
                showConfirmationDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        updateEmptyViewVisibility();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProgressBar.setVisibility(View.VISIBLE);
        mAdapter.swapCursor(null);
        updateEmptyViewVisibility();
    }

    @Override
    public void onClick() {
        updateFabVisibility();
        invalidateOptionsMenu();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.pref_history_compact_layout_key))) {
            mLayoutManager = PreferenceUtils.getHistoryLayoutManager(this, mSharedPreferences);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mAdapter.reloadLayout();
        }
    }

    /* Deletes selection from the history table */
    private void deleteSelectedProducts() {
        new AlertDialog.Builder(HistoryActivity.this)
                .setMessage(getResources().getQuantityString(R.plurals.history_clear_selection_title, selected.size()))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new AsyncTask<Void, Void, Integer>() {
                            @Override
                            protected Integer doInBackground(Void... voids) {
                                return DatabaseUtils.deleteProductsFromHistoryTable(HistoryActivity.this, selected);
                            }

                            @Override
                            protected void onPostExecute(Integer integer) {
                                super.onPostExecute(integer);
                                showMessage(getResources().getQuantityString(R.plurals.history_products_cleared_message, integer, integer));
                                selected.clear();
                                updateFabVisibility();
                                mAdapter.notifyDataSetChanged();
                                invalidateOptionsMenu();
                            }
                        }.execute();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create().show();
    }

    /* Shows a dialog that gives the choice to insert selected elements to the list or
     * to discard the selection. Used when leaving the activity. */
    private void showConfirmationDialog() {
        if (selected != null && !selected.isEmpty()) {
            new AlertDialog.Builder(HistoryActivity.this)
                    .setMessage(getResources().getQuantityString(R.plurals.history_add_selected_products, selected.size()))
                    .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            addSelectedProducts();
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

    /* Inserts selected products to the list table and sets the result of the activity. */
    @SuppressLint("StaticFieldLeak")
    private void addSelectedProducts() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                return DatabaseUtils.insertProductsIntoListTable(HistoryActivity.this, selected);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                Intent intent = new Intent();
                if (integer == 0)
                    intent.putExtra(getString(R.string.history_message_to_list),
                            getString(R.string.list_no_new_product_message));
                else
                    intent.putExtra(getString(R.string.history_message_to_list),
                            getResources().getQuantityString(R.plurals.list_new_products_message, integer, integer));
                setResult(RESULT_OK, intent);
            }
        }.execute();


    }

    /* Updates the visibility of the Floating Action Button.
     * If selection is empty, the FAB is invisible. */
    private void updateFabVisibility() {
        if (!selected.isEmpty()) mFab.show();
        else mFab.hide();
    }

    /* Updates the visibility of the Empty View. */
    private void updateEmptyViewVisibility() {
        if (mAdapter.getItemCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    /* Shows a short Snackbar message */
    private void showMessage(String message) {
        Snackbar.make(findViewById(R.id.history), message, Snackbar.LENGTH_SHORT).show();
    }
}