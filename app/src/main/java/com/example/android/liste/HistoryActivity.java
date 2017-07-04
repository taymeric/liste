package com.example.android.liste;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.android.liste.data.ListContract;

import java.util.HashMap;
import java.util.Iterator;

import static android.R.attr.keySet;


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

    public static final int HISTORY_LOADER_ID = 99;
    public static final String TAG = "HistoryActivity.java";

    private FloatingActionButton mFab;
    private RecyclerView mRecyclerView;
    private View mEmptyView;
    private HistoryAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SharedPreferences mSharedPreferences;

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
            }
        });

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Set up the Recycler View with its Adapter
        mRecyclerView = (RecyclerView) findViewById(R.id.history_recyclerview);
        mLayoutManager = PreferenceUtils.getLayoutFromPrefs(this, mSharedPreferences, getString(R.string.pref_history_layout_key));
        mRecyclerView.setLayoutManager(mLayoutManager);
        //mRecyclerView.setBackgroundColor(PreferenceUtils.getHistoryColorFromPrefs(this, mSharedPreferences));
        mAdapter = new HistoryAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        mEmptyView = findViewById(R.id.empty_view);
        setRecyclerViewVisibility();

        getLoaderManager().initLoader(HISTORY_LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_options, menu);

        // Set up the 'Clear selection' so that it is only visible if there are selected elements.
        MenuItem button_clear = menu.findItem(R.id.action_clear_selection);
        if (selectedIds.isEmpty())
            button_clear.setVisible(false);
        else
            button_clear.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_clear_selection:
                deleteSelectedEntries();
                return true;
            case R.id.action_clear_all:
                deleteAllHistoryEntries();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteSelectedEntries() {
        new AlertDialog.Builder(HistoryActivity.this)
                .setMessage(getString(R.string.message_confirm_clear_selected))
                .setPositiveButton(R.string.confirm_clear, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int nb = 0;
                        Uri uri;
                        // Iterate through all the Ids contained in the HasMap of selected elements
                        // and remove elements with those Ids from the history table.
                        for (String id : selectedIds.keySet()) {
                            uri = ListContract.HistoryEntry.CONTENT_URI;
                            uri = uri.buildUpon().appendPath(id).build();
                            nb += getContentResolver().delete(uri, null, null);
                        }
                        if (nb == 1)
                            showMessage(nb + " " + getString(R.string.element_cleared));
                        else
                            showMessage(nb + " " + getString(R.string.elements_cleared));
                        selectedIds.clear();
                        setFabVisibility();
                        setRecyclerViewVisibility();
                        mAdapter.clearSelection();
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(R.string.cancel_clear, null)
                .create().show();
    }

    private void deleteAllHistoryEntries() {
        if (mAdapter.getItemCount() != 0) {
            new AlertDialog.Builder(HistoryActivity.this)
                    .setMessage(getString(R.string.message_confirm_clear_history))
                    .setPositiveButton(R.string.confirm_clear, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getContentResolver().delete(ListContract.HistoryEntry.CONTENT_URI, null, null);
                        }
                    })
                    .setNegativeButton(R.string.cancel_clear, null)
                    .create().show();
        }
    }

    private void addSelectedEntries() {
        Uri uri = ListContract.ListEntry.CONTENT_URI;
        int nb = 0;
        ContentValues cv;
        Uri newuri;
        // Iterate through all the text values contained in the HasMap of selected elements
        // and add elements with those texts to the list table.
        for (String value: selectedIds.values()) {
            cv = new ContentValues();
            cv.put(ListContract.ListEntry.COLUMN_STRING, value);
            newuri = getContentResolver().insert(uri, cv);
            if (newuri != null && !newuri.equals(Uri.EMPTY)) nb++;
        }

        switch(nb) {
            case 0:
                showMessage(getString(R.string.no_new_element));
                break;
            case 1:
                showMessage(nb + " " + getString(R.string.one_new_element));
            default:
                showMessage(nb + " " + getString(R.string.several_new_elements));
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case HISTORY_LOADER_ID :
                String order = ListContract.HistoryEntry.COLUMN_STRING + " COLLATE LOCALIZED ASC";
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
        mAdapter.swapCursor(cursor);
        setRecyclerViewVisibility();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
        setRecyclerViewVisibility();
    }

    /**
     * Shows a toast message.
     */
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Callback method of HistoryAdapter.AdapterOnClickHandler.
    // Allows HistoryAdapter to update the HashMap containing the selected items.
    @Override
    public void onClick(String id, String txt) {
        if (selectedIds.containsKey(id))
            selectedIds.remove(id);
        else selectedIds.put(id, txt);
        setFabVisibility();
    }

    // Updates the visibility of the Floating Action Button.
    // If selection is empty, the FAB is invisible.
    private void setFabVisibility() {
        if (!selectedIds.isEmpty() && !mFab.isShown()) {
            mFab.show();
            invalidateOptionsMenu();
        }
        else if (selectedIds.isEmpty() && mFab.isShown()) {
            mFab.hide();
            invalidateOptionsMenu();
        }
    }

    // If there are no elements in the history, display a message in an 'empty' view
    private void setRecyclerViewVisibility() {
        if (mAdapter.getItemCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
        } else {
            mEmptyView.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}
