package com.example.android.liste;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.android.liste.data.ListContract;
import com.example.android.liste.data.ListQueryHandler;

import java.util.Calendar;


/**
 * ListActivity is the main Activity of the app and displays the products of the 'list' table
 * in a RecyclerView.
 * From this RecyclerView, the user can:
 *  - Swipe left and/or right an element to delete it from the list
 *  - Long-press an element to open a dialog to set the product's priority and an optional annotation
 * Additionally, from the AppBar, the user can:
 *  - Add a new element to the list (and to the two database tables: list and history)
 *  - Delete the whole list
 *  - Set up a scheduled notification
 *  - Send the content of the list by email
 *  - Access the settings (ie SettingsActivity)
 * Finally, the user can start HistoryActivity by tapping the Floating Action Button.
 */
public class ListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener,
        ListAdapter.ListAdapterOnClickHandler
{
    /* Helps the LoaderManager identify the loader for the list */
    private static final int LIST_LOADER_ID = 100;

    /* Used by NotificationManager to identify the notification within the app */
    private static final int NOTIFICATION_ID = 200;

    /* Used by onActivityResult to identifies that the result comes from HistoryActivity */
    private static final int HISTORY_FOR_RESULT_ID = 300;

    /* The Floating Action Button that launches HistoryActivity */
    private FloatingActionButton mFab;

    /* A ProgressBar that is only 'visible' when the list is loading (otherwise it is 'gone')*/
    private ProgressBar mProgressBar;

    /* A View displayed in place of the RecyclerView is empty (when the table does not contain
    * any element) */
    private View mEmptyView;

    /* A Reference to the Shared Preferences of the app, used by many methods to adjust the behavior
     * of the app to the user's preferences */
    private SharedPreferences mSharedPreferences;

    /* The RecyclerView that displays the items of the list */
    private RecyclerView mRecyclerView;

    /* The LayoutManager that defines if the RecyclerView has one or two columns, depending on the
     * user's preferences */
    private RecyclerView.LayoutManager mLayoutManager;

    /* The Adapter that binds the data from the list table to the Recycler View */
    private ListAdapter mAdapter;

    /* A field in the AppBar that allows typing new products to add to the list and suggests products
    * from the history */
    private AutoCompleteTextView mAutoCompleteTextView;

    /* Used to perform Content Provider operations on a background thread */
    private ListQueryHandler mListQueryHandler;

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
            startActivityForResult(intent, HISTORY_FOR_RESULT_ID);
            }
        });

        // Set up the RecyclerView and its Adapter
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mLayoutManager = PreferenceUtils.getListLayoutManager(this, mSharedPreferences);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ListAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        // The ItemTouchHelper class manages deletion of a RecyclerView item that is swiped
        ItemTouchHelper.SimpleCallback mSimpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // The Adapter class stores the id of the element as a tag in the viewHolder
                int id = (int) viewHolder.itemView.getTag();
                deleteSingleEntry(id);
            }
        };
        new ItemTouchHelper(mSimpleCallback).attachToRecyclerView(mRecyclerView);

        // Create the ListQueryHandler used to perform Content Provider operations
        mListQueryHandler = new ListQueryHandler(getContentResolver());

        // By default, the empty view's visibility is set to 'gone'
        mEmptyView = findViewById(R.id.empty_view);

        // Make the progress bar visible until the list has finished loading
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        // Initiate loading of the list to populate the RecyclerView
        getLoaderManager().initLoader(LIST_LOADER_ID, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_list, menu);
        setupNotificationButtons(menu);
        setupAddButton(menu);

        // Only show one of the layout change buttons
        MenuItem compact_layout = menu.findItem(R.id.action_compact_layout);
        MenuItem normal_layout = menu.findItem(R.id.action_normal_layout);
        if (mSharedPreferences.getBoolean(getString(R.string.pref_list_compact_layout_key), true)) {
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
            case R.id.action_preferences:
                Intent preferencesIntent = new Intent(this, PreferencesActivity.class);
                startActivity(preferencesIntent);
                return true;
            case R.id.action_clear:
                showDeleteAllDialog();
                return true;
            case R.id.action_compact_layout:
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean(getString(R.string.pref_list_compact_layout_key), true);
                editor.apply();
                invalidateOptionsMenu();
                return true;
            case R.id.action_normal_layout:
                SharedPreferences.Editor editor2 = mSharedPreferences.edit();
                editor2.putBoolean(getString(R.string.pref_list_compact_layout_key), false);
                editor2.apply();
                invalidateOptionsMenu();
                return true;
            case R.id.action_notify:
                showNotificationSetupDialogs();
                return true;
            case R.id.action_alarm_info:
                showScheduledNotificationInformationDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LIST_LOADER_ID:
                String sortOrder = PreferenceUtils.getSortOrder(this, mSharedPreferences);
                // Returns a new CursorLoader
                return new CursorLoader(
                        this,    // Parent activity context
                        ListContract.ListEntry.CONTENT_URI,    // Table to query
                        null,    // Projection to return
                        null,    // No selection clause
                        null,    // No selection arguments
                        sortOrder    // Default sort order
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
            mProgressBar.setVisibility(View.GONE);
            mAdapter.swapCursor(data);
            updateEmptyViewVisibility();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int id = loader.getId();
        if (id == LIST_LOADER_ID) {
            mProgressBar.setVisibility(View.VISIBLE);
            mAdapter.swapCursor(null);
            updateEmptyViewVisibility();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == HISTORY_FOR_RESULT_ID) {
            if (resultCode == RESULT_OK) {
                String message = data.getStringExtra(getString(R.string.history_message_to_list));
                showMessageWithFabChange(message);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // If a preference has been modified, makes the necessary calls in order to update the display
        if (s.equals(getString(R.string.pref_list_compact_layout_key))) {
            mLayoutManager = PreferenceUtils.getListLayoutManager(this, mSharedPreferences);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mAdapter.reloadLayout();
        } else if (s.equals(getString(R.string.pref_sort_order_key))) {
            getLoaderManager().restartLoader(LIST_LOADER_ID, null, this);
        } else if (s.equals(getString(R.string.list_notification_alarm_on))) {
            invalidateOptionsMenu();
        } else if (s.equals(getString(R.string.pref_font_key))) {
            mAdapter.reloadFont();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(int id, boolean long_press) {
        if (long_press) {
            mRecyclerView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            showEditionDialog(id);
        } else {
            deleteSingleEntry(id);
        }
    }

    /* This method makes sure that when a notification is scheduled, an informative menu item appears on
     * the AppBar and that the menu entry for settings up a notification does not appear anymore.  */
    private void setupNotificationButtons(Menu menu) {
        boolean isAlarmSet = PreferenceUtils.isAlarmOn(this, mSharedPreferences);
        MenuItem alarmSettingButton = menu.findItem(R.id.action_notify);
        alarmSettingButton.setVisible(!isAlarmSet);
        MenuItem alarmCancelButton = menu.findItem(R.id.action_alarm_info);
        alarmCancelButton.setVisible(isAlarmSet);
    }

    /* This method sets up the button that allows adding elements to the list.
     * The button is an ActionView which reveals when expanded an AutoCompleteTextView
     * that provides suggestions based on the history table. */
    private void setupAddButton(Menu menu) {

        final MenuItem menuItem = menu.findItem(R.id.action_add);
        View v = menuItem.getActionView();

        MenuItemCompat.OnActionExpandListener expandListener = new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // When the ActionView is collapsed, hide the soft keyboard and clear the TextView field.
                if (mAutoCompleteTextView != null) {
                    mAutoCompleteTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            mAutoCompleteTextView.clearFocus();
                            mAutoCompleteTextView.setText("");
                            final InputMethodManager inputMethodManager
                                    = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (inputMethodManager != null)
                                inputMethodManager.hideSoftInputFromWindow(findViewById(R.id.main).getWindowToken(), 0);
                        }
                    });
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // When the ActionView is expanded, display the soft keyboard and request focus
                if (mAutoCompleteTextView != null) {
                    mAutoCompleteTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            mAutoCompleteTextView.requestFocusFromTouch();
                            final InputMethodManager inputMethodManager
                                    = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (inputMethodManager != null)
                                inputMethodManager.showSoftInput(mAutoCompleteTextView, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
                return true;
            }
        };
        MenuItemCompat.setOnActionExpandListener(menuItem, expandListener);

        mAutoCompleteTextView = v.findViewById(R.id.add_text_view);

        // A SimpleCursorAdapter to help populate the list of suggestions with entries from the history table.
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.row_completion,
                null, //cursor
                new String[]{ListContract.HistoryEntry.COLUMN_PRODUCT},
                new int[]{android.R.id.text1},
                0
        );

        // Filter for suggestions from the history table based on the input character sequence.
        cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                return getCursor(str);
            } });

        // Inserts the typed character sequence as an entry to both tables, when the user types 'enter'
        mAutoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String text = mAutoCompleteTextView.getText().toString();
                if (!text.equals("")) {
                    DataUtils.insertProductIntoBothTables(mListQueryHandler, text);
                    mAutoCompleteTextView.setText("");
                }
                return true;
            }
        });

        mAutoCompleteTextView.setAdapter(cursorAdapter);
        // In order to display the correct suggestion, we need to implement the conversion from Cursor
        // to the desired String field to display.
        cursorAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor cursor) {
                return cursor.getString(cursor.getColumnIndex(ListContract.HistoryEntry.COLUMN_PRODUCT));
            }
        });
    }

    /* Helper method to get a Cursor that points to elements of the history table
     * that match the string passed as parameter. */
    private Cursor getCursor(CharSequence str) {
        String select = ListContract.HistoryEntry.COLUMN_PRODUCT + " LIKE ? ";
        String[]  selectArgs = { "%" + str + "%"};
        String[] projection = new String[] {
                ListContract.HistoryEntry._ID,
                ListContract.HistoryEntry.COLUMN_PRODUCT};
        return getContentResolver().query(ListContract.HistoryEntry.CONTENT_URI, projection, select, selectArgs, null);
    }

    /* Shows a popup dialog that allows to add or edit an annotation to a product of the list,
     * as well as changing the priority setting for that product.
     * @param id the actual _ID of the product */
    private void showEditionDialog(int id) {

        final AlertDialog alertDialog;

        String stringId = Integer.toString(id);
        Uri contentUri = ListContract.ListEntry.CONTENT_URI;
        final Uri uri = contentUri.buildUpon().appendPath(stringId).build();

        String[] columns = { ListContract.ListEntry.COLUMN_PRODUCT, ListContract.ListEntry.COLUMN_PRIORITY,
                ListContract.ListEntry.COLUMN_ANNOTATION};
        String selection = ListContract.ListEntry._ID + "=?";
        String [] selectionArgs = new String[] { stringId };
        // Here, we don't use AsyncQueryHandler because we need to use the result of the query
        // for the creation of the UI which is done right away.
        Cursor cursor = getContentResolver().query(uri, columns, selection, selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {

            String product = cursor.getString(cursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRODUCT));
            int priority = cursor.getInt(cursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRIORITY));
            String annotation = cursor.getString(cursor.getColumnIndex(ListContract.ListEntry.COLUMN_ANNOTATION));

            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_edition, null);
            final RadioButton radioButton1 = view.findViewById(R.id.button_1);
            final RadioButton radioButton2 = view.findViewById(R.id.button_2);
            final RadioButton radioButton3 = view.findViewById(R.id.button_3);
            switch(priority) {
                case ListContract.ListEntry.HIGH_PRIORITY_PRODUCT:
                    radioButton1.setChecked(true);
                    break;
                case ListContract.ListEntry.LOW_PRIORITY_PRODUCT:
                    radioButton3.setChecked(true);
                    break;
                default:
                    radioButton2.setChecked(true);
            }
            final EditText editText = view.findViewById(R.id.dialog_edit_text);
            if (annotation != null) editText.setText(annotation);
            else editText.setText("");

            alertDialog = new AlertDialog.Builder(ListActivity.this)
                    .setMessage(product)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            int newPriority;
                            if (radioButton1.isChecked())
                                newPriority = ListContract.ListEntry.HIGH_PRIORITY_PRODUCT;
                            else if (radioButton3.isChecked())
                                newPriority = ListContract.ListEntry.LOW_PRIORITY_PRODUCT;
                            else newPriority = ListContract.ListEntry.DEFAULT_PRIORITY_PRODUCT;

                            String newAnnotation = editText.getText().toString().trim();

                            ContentValues contentValues = new ContentValues();
                            contentValues.put(ListContract.ListEntry.COLUMN_PRIORITY, newPriority);
                            contentValues.put(ListContract.ListEntry.COLUMN_ANNOTATION, newAnnotation);

                            mListQueryHandler.startUpdate(ListQueryHandler.UPDATE_LIST, null, uri,
                                    contentValues, null, null);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            alertDialog.show();

            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                    return true;
                }
            });

            cursor.close();
        }
    }

    /* Deletes a single entry from the list based on its id in the table.*/
    private void deleteSingleEntry(int id) {
        String stringId = Integer.toString(id);

        Uri uri = ListContract.ListEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(stringId).build();

        String[] projection  = { ListContract.ListEntry.COLUMN_PRODUCT,
                ListContract.ListEntry.COLUMN_PRIORITY, ListContract.ListEntry.COLUMN_ANNOTATION };

        Cursor cu = getContentResolver().query(uri, projection, null, null, null);

        if (cu!=null && cu.moveToFirst()) {
            // Save values for undo operation
            String product = cu.getString(cu.getColumnIndex(ListContract.ListEntry.COLUMN_PRODUCT));
            int priority = cu.getInt(cu.getColumnIndex(ListContract.ListEntry.COLUMN_PRIORITY));
            String annotation = cu.getString(cu.getColumnIndex(ListContract.ListEntry.COLUMN_ANNOTATION));
            cu.close();
            // Call for actual deletion
            mListQueryHandler.startDelete(ListQueryHandler.DELETION_LIST, null, uri, null, null);
            // Show confirmation message along with 'undo' button
            showMessageWithUndoAction(
                    getResources().getString(R.string.list_removed_product_message, product),
                    product, priority, annotation);
        }
    }

    /* Shows a confirmation dialog to delete all entries from the list. */
    private void showDeleteAllDialog() {
        if (mAdapter.getItemCount() != 0) {
            new AlertDialog.Builder(ListActivity.this)
                    .setMessage(getString(R.string.list_clear_title))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // The 'yes' button has been clicked, call for actual deletion
                            mListQueryHandler.startDelete(ListQueryHandler.DELETION_LIST, null,
                                    ListContract.ListEntry.CONTENT_URI, null, null);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .create().show();
        } else {
            showMessage(getString(R.string.list_empty_message));
        }
    }

    /* Nesting method launching the first step of the notification setup. */
    private void showNotificationSetupDialogs() {
        showDatePicker();
    }

    /* Shows a dialog for the selection of the date of the notification. */
    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        final DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // When the date for the notification has been selected, launch the second step of the notification setup.
                showTimePicker(year, month, day);
            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, listener, year, month, day);

        // Create View for Custom title
        LayoutInflater inflater = getLayoutInflater();
        final ViewGroup nullParent = null;
        View v = inflater.inflate(R.layout.dialog_picker_title, nullParent);
        TextView tv = v.findViewById(R.id.text_title);
        tv.setText(getString(R.string.list_notification_date_picker_message));

        datePickerDialog.setCustomTitle(v);

        datePickerDialog.show();

        // This call is after show() because Button are not created before
        Button confirmButton = datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (confirmButton != null) confirmButton.setText(getString(R.string.list_notification_pickers_next_button));
    }

    /* Shows a dialog for the selection of the time for the notification. The previously selected date
     * is passed as parameter. */
    private void showTimePicker(final int year, final int month, final int day) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        final TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                // When the time for the notification has been selected along with the date, launch the
                // final step of the notification setup.
                setNotificationTime(year, month, day, hour, minute);
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                listener,
                hour,
                minute,
                DateFormat.is24HourFormat(this));

        // Create View for Custom title
        LayoutInflater inflater = getLayoutInflater();
        final ViewGroup nullParent = null;
        View v = inflater.inflate(R.layout.dialog_picker_title, nullParent);
        TextView tv = v.findViewById(R.id.text_title);
        tv.setText(getString(R.string.list_notification_time_picker_message));

        timePickerDialog.setCustomTitle(v);

        timePickerDialog.show();
    }

    /* Sets the time of the scheduled notification according to the time and date passed
     * as parameters. */
    private void setNotificationTime(int year, int month, int day, int hour, int minute) {

        // Creation of the PendingIntent that will be used when the alarm is triggered
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID_KEY, NOTIFICATION_ID);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        long millis = cal.getTimeInMillis();

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) alarmManager.set(AlarmManager.RTC_WAKEUP, millis, pendingIntent);

        String time = DateUtils.formatDateTime(this, millis, DateUtils.FORMAT_SHOW_TIME|DateUtils.FORMAT_SHOW_DATE);

        PreferenceUtils.setAlarm(this, mSharedPreferences, true, time);

        showMessage(getResources().getString(R.string.list_notification_set_message, time));

        invalidateOptionsMenu();
    }

    /* Shows a dialog with the time and date of the scheduled notification, as well as an option
     * to cancel this notification. */
    private void showScheduledNotificationInformationDialog() {
        String time = PreferenceUtils.getAlarmTime(this, mSharedPreferences);
        new AlertDialog.Builder(ListActivity.this)
                .setMessage(getResources().getString(R.string.list_notification_information_dialog_message, time))
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(R.string.list_menu_deactivate_notification, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cancelScheduledNotification();
                    }
                })
                .create().show();
    }

    /* Cancels the scheduled notification and recreates the menu. */
    private void cancelScheduledNotification() {
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) alarmManager.cancel(pendingIntent);

        PreferenceUtils.setAlarm(this, mSharedPreferences, false, null);

        invalidateOptionsMenu();

        showMessage(getString(R.string.list_notification_canceled_message));
    }

    /* Sends an implicit intent to a mail app with the content of the list as the body of the mail. */
    /*private void sendByEmail() {
        if (mAdapter.getItemCount() != 0) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_SUBJECT,
                    getResources().getString(R.string.list_email_title, getDate()));
            intent.putExtra(Intent.EXTRA_TEXT,
                    DataUtils.getListAsStringForEmail(this, mSharedPreferences));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } else {
            showMessage(getString(R.string.list_empty_message));
        }
    }*/

    /* Gets the current date as a String representation. */
    /*private String getDate() {
        Calendar c = Calendar.getInstance();
        Date date = c.getTime();
        java.text.DateFormat formatter = DateFormat.getDateFormat(this);
        return formatter.format(date);
    }*/

    /* Updates the visibility of the Empty View. */
    private void updateEmptyViewVisibility() {
        if (mAdapter.getItemCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    /* Shows a short Snackbar message. */
    private void showMessage(String message) {
        Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_SHORT)
                .show();
    }

    /* Shows a long Snackbar message and changes the Floating Action Button icon. */
    private void showMessageWithFabChange(String message) {

        // Change FAB icon to a check mark
        mFab.setImageResource(R.drawable.ic_check_white_24dp);
        mFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ListActivity.this, R.color.colorPrimaryDark)));

        // Show a long Snackbar with the message
        Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        // Change FAB icon back to basket when the message has ended
                        mFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ListActivity.this, R.color.colorAccent)));
                        mFab.setImageResource(R.drawable.ic_shopping_basket_white_24dp);
                    }
                })
                .show();
    }

    /* Shows a long Snackbar message with an 'undo' action. */
    private void showMessageWithUndoAction(String message, final String product, final int priority,
                                           final String annotation) {
        Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG)
                .setAction(getString(android.R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // When the 'undo' button is clicked, add the item whose attributes
                        // are passed as parameter to the list.
                        ContentValues cv = new ContentValues();
                        cv.put(ListContract.ListEntry.COLUMN_PRODUCT, product);
                        cv.put(ListContract.ListEntry.COLUMN_PRIORITY, priority);
                        if (annotation != null)
                            cv.put(ListContract.ListEntry.COLUMN_ANNOTATION, annotation);
                        mListQueryHandler.startInsert(ListQueryHandler.INSERTION_LIST,
                                null, ListContract.ListEntry.CONTENT_URI, cv);
                    }
                })
                .show();
    }
}