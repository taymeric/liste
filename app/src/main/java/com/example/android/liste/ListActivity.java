package com.example.android.liste;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.Toast;

import com.example.android.liste.data.ListContract;

import static com.example.android.liste.R.id.recyclerView;

/**
 * ListActivity is the main Activity and displays the items of the 'list' table.
 * The User can:
 *  - Add a new element to the list by using the text field in the AppBar
 *  - Go to HistoryActivity by using the FAB
 *  - Delete individual elements from the list by swiping
 *  - Delete the whole list
 *  - Access the settings
 */
public class ListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener
{

    public static final int LIST_LOADER_ID = 77;
    public static final int HISTORY_LOADER_ID = 88;
    public static final String TAG = "ListActivity.java";

    private FloatingActionButton mFab;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ListAdapter mAdapter;
    private SharedPreferences mSharedPreferences;
    private SearchView mSearchView;

    // A CursorAdapter for suggestions from the history table when typing
    private SimpleCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        mFab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        // Set up the Recycler View and its Adapter
        mRecyclerView = (RecyclerView) findViewById(recyclerView);
        mLayoutManager = PreferenceUtils.getLayoutFromPrefs(this, mSharedPreferences, getString(R.string.pref_list_layout_key));
        mRecyclerView.setLayoutManager(mLayoutManager);
        //mRecyclerView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        mAdapter = new ListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        // Hides FAB when scrolling down
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);

                if (dy >0) {
                    // Scroll Down
                    if (mFab.isShown()) {
                        mFab.hide();
                    }
                }
                else if (dy <0) {
                    // Scroll Up
                    if (!mFab.isShown()) {
                        mFab.show();
                    }
                }
            }
        });

        // Delete-on-swipe implementation
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                // The Adapter stores the Id of the element in the viewHolder
                int id = (int) viewHolder.itemView.getTag();
                String stringId = Integer.toString(id);

                Uri uri = ListContract.ListEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();

                getContentResolver().delete(uri, null, null);

                // Make sure the FAB is visible as scrolling up may not be possible anymore
                if (!mFab.isShown()) mFab.show();
            }
        }).attachToRecyclerView(mRecyclerView);

        getLoaderManager().initLoader(LIST_LOADER_ID, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_options, menu);
        setupSearchView(menu);
        return true;
    }

    // The SearchView is the text field in the AppBar used to enter new elements.
    private void setupSearchView(Menu menu) {

        final MenuItem menuItem = menu.findItem(R.id.action_add);
        mSearchView = (SearchView) menuItem.getActionView();
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        // Hack to get suggestions starting from first character typed
        AutoCompleteTextView searchAutoCompleteTextView = (AutoCompleteTextView) mSearchView.findViewById(getResources().getIdentifier("search_src_text", "id", getPackageName()));
        searchAutoCompleteTextView.setThreshold(1);

        mCursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.hint_row,
                null,
                new String[] {ListContract.HistoryEntry.COLUMN_STRING},
                new int[] {android.R.id.text1},
                0
        );

        // Filter for suggestions from the history table.
        mCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                return getCursor(str);
            } });

        //mSearchView.setSuggestionsAdapter(mCursorAdapter);

        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(position);
                String text = cursor.getString(cursor.getColumnIndex(ListContract.HistoryEntry.COLUMN_STRING));
                mSearchView.setQuery(text, false);
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(position);
                String text = cursor.getString(cursor.getColumnIndex(ListContract.HistoryEntry.COLUMN_STRING));
                mSearchView.setQuery(text, false);
                return true;
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String texte) {
                if (!(texte == null || texte.equals(""))) {
                    // Add text to the list table, shows a message if it is already there
                    ContentValues values = new ContentValues();
                    values.put(ListContract.ListEntry.COLUMN_STRING, texte);
                    Uri uri = getContentResolver().insert(ListContract.ListEntry.CONTENT_URI, values);
                    if (uri != null && uri.equals(Uri.EMPTY)) {
                        showMessage(texte + " " + getString(R.string.already));
                    }
                    // Add text to the history
                    ContentValues values2 = new ContentValues();
                    values2.put(ListContract.HistoryEntry.COLUMN_STRING, texte);
                    Uri uri2 = getContentResolver().insert(ListContract.HistoryEntry.CONTENT_URI, values2);

                    mSearchView.setQuery("",false);
                }
                menuItem.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Initialize Loader for history table here to make sure the SearchView has been created.
        getLoaderManager().initLoader(HISTORY_LOADER_ID, null, this);
    }

    // Helper method to get a Cursor that points to elements of the history table
    // that match the string passed as parameter.
    private Cursor getCursor(CharSequence str) {
        String select = ListContract.HistoryEntry.COLUMN_STRING + " LIKE ? ";
        String[]  selectArgs = { "%" + str + "%"};
        String[] contactsProjection = new String[] {
                ListContract.HistoryEntry._ID,
                ListContract.HistoryEntry.COLUMN_STRING  };
        return getContentResolver().query(ListContract.HistoryEntry.CONTENT_URI, contactsProjection, select, selectArgs, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_settings :
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_clear :
                deleteListEntries();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteListEntries() {
        if (mAdapter.getItemCount() != 0) {
            new AlertDialog.Builder(ListActivity.this)
                    .setMessage(getString(R.string.message_confirm_clear_list))
                    .setPositiveButton(R.string.confirm_clear, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getContentResolver().delete(ListContract.ListEntry.CONTENT_URI, null, null);
                            // Make sure FAB is visible as scrolling is not possible anymore
                            // when the list is empty
                            if (!mFab.isShown()) mFab.show();
                        }
                    })
                    .setNegativeButton(R.string.cancel_clear, null)
                    .create().show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LIST_LOADER_ID:
                String sortOrder = PreferenceUtils.getSortOrderFromPrefs(this, mSharedPreferences);
                // Returns a new CursorLoader
                return new CursorLoader(
                        this,    // Parent activity context
                        ListContract.ListEntry.CONTENT_URI,    // Table to query
                        null,    // Projection to return
                        null,    // No selection clause
                        null,    // No selection arguments
                        sortOrder    // Default sort order
                );
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        if (id == LIST_LOADER_ID) {
            // Swap cursor in order to display List items when List Loader has finished
            mAdapter.swapCursor(data);
        } else if (id == HISTORY_LOADER_ID) {
            // Swap cursor in order to enable suggestions when History Loader has finished
            mCursorAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    /**
     * Shows a toast message.
     */
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /* If a preference has been modified, makes the necessary calls in order to update display */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.pref_list_size_key))) {
            mAdapter.reloadSize();
            mAdapter.notifyDataSetChanged();
        } else if (s.equals(getString(R.string.pref_list_layout_key))) {
            mLayoutManager = PreferenceUtils.getLayoutFromPrefs(this, mSharedPreferences, getString(R.string.pref_list_layout_key));
            mRecyclerView.setLayoutManager(mLayoutManager);
        } else if (s.equals(getString(R.string.pref_sort_order_key))) {
            getLoaderManager().restartLoader(LIST_LOADER_ID, null, this);
        }
    }
}
