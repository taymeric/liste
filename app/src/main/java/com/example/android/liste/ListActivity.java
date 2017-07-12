package com.example.android.liste;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.android.liste.data.ListContract;

import java.util.Calendar;


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
        SharedPreferences.OnSharedPreferenceChangeListener,
        ListAdapter.ListAdapterOnClickListener
{

    public static final int DEFAULT_PRIORITY = 2;
    public static final int HIGH_PRIORITY = 1;
    private static final int LIST_LOADER_ID = 77;
    private static final int HISTORY_LOADER_ID = 88;
    private static final int LIST_NOTIFICATION_ID = 101;

    private FloatingActionButton mFab;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ItemTouchHelper.SimpleCallback mSimpleCallback;
    private ListAdapter mAdapter;
    private SharedPreferences mSharedPreferences;
    private AutoCompleteTextView mAutoCompleteTextView;

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
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mLayoutManager = PreferenceUtils.getLayoutFromPrefs(this, mSharedPreferences, getString(R.string.pref_list_layout_key));
        mRecyclerView.setLayoutManager(mLayoutManager);
        //mRecyclerView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        mAdapter = new ListAdapter(this, this);
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

        // ItemTouchHelper for delete on swipe
        mSimpleCallback = new ItemTouchHelper.SimpleCallback(0,
                PreferenceUtils.getDirectionFromPrefs(this, mSharedPreferences)) {

            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // The Adapter stores the Id of the element in the viewHolder
                int id = (int) viewHolder.itemView.getTag();
                deleteEntry(id);
            }
        };
        new ItemTouchHelper(mSimpleCallback).attachToRecyclerView(mRecyclerView);

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
        setupAlarmButtons(menu);
        setupActionView(menu);
        return true;
    }

    private void setupActionView(Menu menu) {

        final MenuItem menuItem = menu.findItem(R.id.action_add);
        View v = menuItem.getActionView();

        MenuItemCompat.OnActionExpandListener expandListener = new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (mAutoCompleteTextView != null) {
                    mAutoCompleteTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            mAutoCompleteTextView.clearFocus();
                            mAutoCompleteTextView.setText("");
                            final InputMethodManager inputMethodManager
                                    = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(findViewById(R.id.main).getWindowToken(), 0);
                        }
                    });
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (mAutoCompleteTextView != null) {
                    mAutoCompleteTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            mAutoCompleteTextView.requestFocusFromTouch();
                            final InputMethodManager inputMethodManager
                                    = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.showSoftInput(mAutoCompleteTextView, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
                return true;
            }
        };
        MenuItemCompat.setOnActionExpandListener(menuItem, expandListener);

        mAutoCompleteTextView = v.findViewById(R.id.add_text_view);

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

        mAutoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String text = mAutoCompleteTextView.getText().toString();
                if (!text.equals("")) {
                    insertValueIntoTables(text);
                    mAutoCompleteTextView.setText("");
                }
                menuItem.collapseActionView();
                return true;
            }
        });

        if (mCursorAdapter != null) mAutoCompleteTextView.setAdapter(mCursorAdapter);
        mCursorAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor cursor) {
                return cursor.getString(cursor.getColumnIndex(ListContract.HistoryEntry.COLUMN_STRING));
            }
        });
    }

    private void setupAlarmButtons(Menu menu) {
        boolean isAlarmSet = mSharedPreferences.getBoolean(getString(R.string.alarm_on), false);
        MenuItem alarmSettingButton = menu.findItem(R.id.action_notify);
        alarmSettingButton.setVisible(!isAlarmSet);
        MenuItem alarmCancelButton = menu.findItem(R.id.action_alarm_info);
        alarmCancelButton.setVisible(isAlarmSet);
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

    private void insertValueIntoTables(String value) {
        // Add value to the list table with a default priority, shows a message if it is already there
        ContentValues values = new ContentValues();
        values.put(ListContract.ListEntry.COLUMN_STRING, value);
        values.put(ListContract.ListEntry.COLUMN_PRIORITY, DEFAULT_PRIORITY);
        Uri uri = getContentResolver().insert(ListContract.ListEntry.CONTENT_URI, values);
        if (uri != null && uri.equals(Uri.EMPTY)) {
            showMessage(value + " " + getString(R.string.already));
        }
        // Add text to the history
        ContentValues values2 = new ContentValues();
        values2.put(ListContract.HistoryEntry.COLUMN_STRING, value);
        getContentResolver().insert(ListContract.HistoryEntry.CONTENT_URI, values2);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_clear:
                deleteListEntries();
                return true;
            case R.id.action_notify:
                showNotificationTimePicker();
                return true;
            case R.id.action_alarm_info:
                showNotificationCancelingDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteListEntries() {
        if (mAdapter.getItemCount() != 0) {
            new AlertDialog.Builder(ListActivity.this)
                    .setMessage(getString(R.string.message_confirm_clear_list))
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getContentResolver().delete(ListContract.ListEntry.CONTENT_URI, null, null);
                            // Make sure FAB is visible as scrolling is not possible anymore
                            // when the list is empty
                            if (!mFab.isShown()) mFab.show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create().show();
        }
    }

    private void deleteEntry(int id) {
        String stringId = Integer.toString(id);

        Uri uri = ListContract.ListEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(stringId).build();

        getContentResolver().delete(uri, null, null);

        // Make sure the FAB is visible as scrolling up may not be possible anymore
        // as elements are deleted.
        if (!mFab.isShown()) mFab.show();
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
        int id = loader.getId();
        if (id == LIST_LOADER_ID) {
            mAdapter.swapCursor(null);
        } else if (id == HISTORY_LOADER_ID) {
            mCursorAdapter.swapCursor(null);
        }
    }

    /**
     * Shows a toast message.
     */
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * If a preference has been modified, makes the necessary calls in order to update display
     */
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
        } else if (s.equals(getString(R.string.pref_direction_key))) {
            updateItemTouchHelper();
        } else if (s.equals(getString(R.string.alarm_on))) {
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onClick(int id) {
        updatePriority(id);
    }

    private void updatePriority(int id) {

        // Check which is the current value for priority
        String stringId = Integer.toString(id);
        Uri uri = ListContract.ListEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(stringId).build();
        String[] columns = { ListContract.ListEntry.COLUMN_PRIORITY };
        String selection = ListContract.ListEntry._ID + "=?";
        String [] selectionArgs = new String[] { stringId };
        Cursor cursor = getContentResolver().query(uri, columns, selection, selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {
            int priority = cursor.getInt(cursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRIORITY));
            if (priority == DEFAULT_PRIORITY) priority = HIGH_PRIORITY;
            else priority = DEFAULT_PRIORITY;

            ContentValues contentValues = new ContentValues();
            contentValues.put(ListContract.ListEntry.COLUMN_PRIORITY, priority);
            getContentResolver().update(uri, contentValues, null, null);
            cursor.close();
        }
    }

    private void updateItemTouchHelper() {
        int direction = PreferenceUtils.getDirectionFromPrefs(this, mSharedPreferences);
        mSimpleCallback.setDefaultSwipeDirs(direction);
    }

    private void cancelReminder() {
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, LIST_NOTIFICATION_ID);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, getNotification());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(getString(R.string.alarm_on), false);
        editor.commit();
        invalidateOptionsMenu();
    }

    private void showNotificationCancelingDialog() {
        String time = mSharedPreferences.getString(getString(R.string.alarm_time) , "00:00");
        new AlertDialog.Builder(ListActivity.this)
                .setMessage(getString(R.string.alarm_set_message) + " " + time + ".")
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(R.string.remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cancelReminder();
                    }
                })
                .create().show();
    }

    private void activateReminderDelay(int delay) {

        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, LIST_NOTIFICATION_ID);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, getNotification());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //delay = 10000; // 10 seconds
        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private void showNotificationTimePicker() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        final TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                activateReminderTime(hour, minute);
                String time = hour + ":" + minute;
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(getString(R.string.alarm_time), time);
                editor.commit();
            }
        };

        TimePickerDialog pickerDialog = new TimePickerDialog(
                this,
                listener,
                hour,
                minute,
                DateFormat.is24HourFormat(this));

        pickerDialog.show();
    }

    private void activateReminderTime(int hour, int minute) {

        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, LIST_NOTIFICATION_ID);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, getNotification());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(getString(R.string.alarm_on), true);
        editor.commit();
        invalidateOptionsMenu();
    }

    // Method for creating the Notification object
    private Notification getNotification() {
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder)
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_shopping_basket_white_24dp)
                        .setContentTitle(getString(R.string.notification_title))
                        .setContentText(getString(R.string.notification_text))
                        .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .setAutoCancel(true);

        // Creates an explicit intent for an Activity in the app
        Intent resultIntent = new Intent(this, ListActivity.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        return mBuilder.build();
    }
}