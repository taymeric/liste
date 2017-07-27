package com.example.android.liste;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
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
import android.os.Vibrator;
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
import java.util.Date;
import java.util.List;


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

    public static final int HIGH_PRIORITY = 1;
    public static final int DEFAULT_PRIORITY = 2;
    public static final int LOW_PRIORITY = 3;
    private static final int LIST_LOADER_ID = 77;
    private static final int HISTORY_LOADER_ID = 88;
    private static final int LIST_NOTIFICATION_ID = 101;
    private static final int HISTORY_FOR_RESULT = 44;

    private FloatingActionButton mFab;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ItemTouchHelper.SimpleCallback mSimpleCallback;
    private ListAdapter mAdapter;
    private SharedPreferences mSharedPreferences;
    private AutoCompleteTextView mAutoCompleteTextView;
    private ProgressBar mProgressBar;
    private ListQueryHandler mListQueryHandler;

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
            startActivityForResult(intent, HISTORY_FOR_RESULT);
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        // Set up the Recycler View and its Adapter
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mLayoutManager = PreferenceUtils.getListLayout(this, mSharedPreferences);
        mRecyclerView.setLayoutManager(mLayoutManager);
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
                PreferenceUtils.getSwipeDirection(this, mSharedPreferences)) {

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

        mListQueryHandler = new ListQueryHandler(getContentResolver());

        getLoaderManager().initLoader(LIST_LOADER_ID, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == HISTORY_FOR_RESULT) {
            if (resultCode == RESULT_OK) {
                mFab.setImageResource(R.drawable.ic_check_white_24dp);
                mFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ListActivity.this, R.color.colorPrimaryDark)));
                String message = data.getStringExtra(getString(R.string.history_message));
                showMessageWithFabCallback(message);
            }
        }
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
                R.layout.row_completion,
                null,
                new String[] {ListContract.HistoryEntry.COLUMN_PRODUCT},
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
                    DataUtils.insertProductIntoBothTables(mListQueryHandler, text);
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
                return cursor.getString(cursor.getColumnIndex(ListContract.HistoryEntry.COLUMN_PRODUCT));
            }
        });
    }

    private void setupAlarmButtons(Menu menu) {
        boolean isAlarmSet = PreferenceUtils.isAlarmOn(this, mSharedPreferences);
        MenuItem alarmSettingButton = menu.findItem(R.id.action_notify);
        alarmSettingButton.setVisible(!isAlarmSet);
        MenuItem alarmCancelButton = menu.findItem(R.id.action_alarm_info);
        alarmCancelButton.setVisible(isAlarmSet);
    }

    // Helper method to get a Cursor that points to elements of the history table
    // that match the string passed as parameter.
    private Cursor getCursor(CharSequence str) {
        String select = ListContract.HistoryEntry.COLUMN_PRODUCT + " LIKE ? ";
        String[]  selectArgs = { "%" + str + "%"};
        String[] contactsProjection = new String[] {
                ListContract.HistoryEntry._ID,
                ListContract.HistoryEntry.COLUMN_PRODUCT};
        return getContentResolver().query(ListContract.HistoryEntry.CONTENT_URI, contactsProjection, select, selectArgs, null);
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
                showDatePicker();
                return true;
            case R.id.action_alarm_info:
                showNotificationCancelingDialog();
                return true;
            case R.id.action_email:
                sendByEmail();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteListEntries() {
        if (mAdapter.getItemCount() != 0) {
            new AlertDialog.Builder(ListActivity.this)
                    .setMessage(getString(R.string.message_confirm_clear_list))
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mListQueryHandler.startDelete(ListQueryHandler.DELETION_LIST, null, ListContract.ListEntry.CONTENT_URI, null, null);
                            // Make sure FAB is visible as deletion affects scrolling
                            if (!mFab.isShown()) mFab.show();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .create().show();
        } else {
            showMessage(getString(R.string.empty_list));
        }
    }

    private void deleteEntry(int id) {
        String stringId = Integer.toString(id);

        Uri uri = ListContract.ListEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(stringId).build();

        ContentResolver cr = getContentResolver();

        // Save values for undo operation
        String[] projection  = { ListContract.ListEntry.COLUMN_PRODUCT, ListContract.ListEntry.COLUMN_PRIORITY, ListContract.ListEntry.COLUMN_ANNOTATION };
        Cursor cu = cr.query(uri, projection, null, null, null);

        if (cu!=null && cu.moveToFirst()) {
            String product = cu.getString(cu.getColumnIndex(ListContract.ListEntry.COLUMN_PRODUCT));
            int priority = cu.getInt(cu.getColumnIndex(ListContract.ListEntry.COLUMN_PRIORITY));
            String annotation = cu.getString(cu.getColumnIndex(ListContract.ListEntry.COLUMN_ANNOTATION));
            cu.close();

            showMessageWithAction("'" + product + "' " + getString(R.string.delete_product), product, priority, annotation);
        }

        cr.delete(uri, null, null);

        // Make sure the FAB is visible as scrolling up may not be possible anymore
        // as elements are deleted.
        if (!mFab.isShown()) mFab.show();
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        if (id == LIST_LOADER_ID) {
            // Swap cursor in order to display List items when List Loader has finished
            mProgressBar.setVisibility(View.GONE);
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
            mProgressBar.setVisibility(View.VISIBLE);
            mAdapter.swapCursor(null);
        } else if (id == HISTORY_LOADER_ID) {
            mCursorAdapter.swapCursor(null);
        }
    }

    /**
     * Shows a short Snackbar message.
     */
    private void showMessage(String message) {
        Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_SHORT)
                .show();
    }

    /**
     * Shows a short Snackbar message and set the Floating Action Button image back
     */
    private void showMessageWithFabCallback(String message) {
        Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        mFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ListActivity.this, R.color.colorAccent)));
                        mFab.setImageResource(R.drawable.ic_shopping_basket_white_24dp);
                    }
                })
                .show();
    }

    /**
     * Shows long Snackbar message with an Action
     */
    private void showMessageWithAction(final String message, final String product, final int priority, final String annotation) {
        Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ContentValues cv = new ContentValues();
                        cv.put(ListContract.ListEntry.COLUMN_PRODUCT, product);
                        cv.put(ListContract.ListEntry.COLUMN_PRIORITY, priority);
                        if (annotation != null) cv.put(ListContract.ListEntry.COLUMN_ANNOTATION, annotation);
                        mListQueryHandler.startInsert(ListQueryHandler.INSERTION_LIST, null, ListContract.ListEntry.CONTENT_URI, cv);
                    }
                })
                .show();
    }

    /**
     * If a preference has been modified, makes the necessary calls in order to update display
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.pref_list_layout_key))) {
            mLayoutManager = PreferenceUtils.getListLayout(this, mSharedPreferences);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mAdapter.reloadLayout();
            mAdapter.notifyDataSetChanged();
            if (!mFab.isShown()) mFab.show();
        } else if (s.equals(getString(R.string.pref_sort_order_key))) {
            getLoaderManager().restartLoader(LIST_LOADER_ID, null, this);
        } else if (s.equals(getString(R.string.pref_direction_key))) {
            updateItemTouchHelper();
        } else if (s.equals(getString(R.string.alarm_on))) {
            invalidateOptionsMenu();
        } else if (s.equals(getString(R.string.pref_font_key))) {
            mAdapter.reloadFont();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(int id) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(25);
        showEditionDialog(id);
    }

    private void updateItemTouchHelper() {
        int direction = PreferenceUtils.getSwipeDirection(this, mSharedPreferences);
        mSimpleCallback.setDefaultSwipeDirs(direction);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        final DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                showTimePicker(year, month, day);
            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, listener, year, month, day);

        // Create View for Custom title
        LayoutInflater inflater = getLayoutInflater();
        final ViewGroup nullParent = null;
        View v = inflater.inflate(R.layout.dialog_picker_date_title, nullParent);

        datePickerDialog.setCustomTitle(v);

        datePickerDialog.show();

        // This call is after show() because Button are not created before...
        Button confirmButton = datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (confirmButton != null) confirmButton.setText(getString(R.string.next));
    }

    private void showTimePicker(final int year, final int month, final int day) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        final TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
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
        View v = inflater.inflate(R.layout.dialog_picker_time_title, nullParent);

        timePickerDialog.setCustomTitle(v);

        timePickerDialog.show();
    }

    private void setNotificationTime(int year, int month, int day, int hour, int minute) {

        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, LIST_NOTIFICATION_ID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        long millis = cal.getTimeInMillis();

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, millis, pendingIntent);

        String time = DateUtils.formatDateTime(this, millis, DateUtils.FORMAT_SHOW_TIME|DateUtils.FORMAT_SHOW_DATE);

        PreferenceUtils.setAlarm(this, mSharedPreferences, true, time);

        showMessage(getString(R.string.alarm_set_message) + " " + time);

        invalidateOptionsMenu();
    }

    private void showNotificationCancelingDialog() {
        String time = PreferenceUtils.getAlarmTime(this, mSharedPreferences);
        new AlertDialog.Builder(ListActivity.this)
                .setMessage(getString(R.string.alarm_set_dialog_message) + " " + time + ".")
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(R.string.deactivate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cancelReminder();
                    }
                })
                .create().show();
    }

    private void cancelReminder() {
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        PreferenceUtils.setAlarm(this, mSharedPreferences, false, null);

        invalidateOptionsMenu();

        showMessage(getString(R.string.alarm_canceled));
    }

    private void sendByEmail() {
        if (mAdapter.getItemCount() != 0) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " " + getDate());
            intent.putExtra(Intent.EXTRA_TEXT, DataUtils.getListAsStringForEmail(this, mSharedPreferences));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } else {
            showMessage(getString(R.string.empty_list));
        }
    }

    private String getDate() {
        Calendar c = Calendar.getInstance();
        Date date = c.getTime();
        java.text.DateFormat formatter = DateFormat.getDateFormat(this);
        return formatter.format(date);
    }

    private void showEditionDialog(int id) {

        final AlertDialog alertDialog;

        String stringId = Integer.toString(id);
        Uri contentUri = ListContract.ListEntry.CONTENT_URI;
        final Uri uri = contentUri.buildUpon().appendPath(stringId).build();

        String[] columns = { ListContract.ListEntry.COLUMN_PRODUCT, ListContract.ListEntry.COLUMN_PRIORITY, ListContract.ListEntry.COLUMN_ANNOTATION};
        String selection = ListContract.ListEntry._ID + "=?";
        String [] selectionArgs = new String[] { stringId };
        Cursor cursor = getContentResolver().query(uri, columns, selection, selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {

            String product = cursor.getString(cursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRODUCT));
            int priority = cursor.getInt(cursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRIORITY));
            String annotation = cursor.getString(cursor.getColumnIndex(ListContract.ListEntry.COLUMN_ANNOTATION));

            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_edition, null);
            final RadioButton radioButton1 = view.findViewById(R.id.button_1);
            final RadioButton radioButton2 = view.findViewById(R.id.button_2);
            final RadioButton radioButton3 = view.findViewById(R.id.button_3);
            switch(priority) {
                case HIGH_PRIORITY:
                    radioButton1.setChecked(true);
                    break;
                case LOW_PRIORITY:
                    radioButton3.setChecked(true);
                    break;
                default:
                    radioButton2.setChecked(true);
            }
            final EditText editText = view.findViewById(R.id.dialog_edit_text);
            if (annotation != null) editText.setText(annotation);
            else editText.setText("");

            alertDialog = new AlertDialog.Builder(ListActivity.this)
                    .setMessage(product + " " + getString(R.string.additional_information))
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            int newPriority;
                            if (radioButton1.isChecked()) newPriority = HIGH_PRIORITY;
                            else if (radioButton3.isChecked()) newPriority = LOW_PRIORITY;
                            else newPriority = DEFAULT_PRIORITY;

                            String newAnnotation = editText.getText().toString().trim();

                            ContentValues contentValues = new ContentValues();
                            contentValues.put(ListContract.ListEntry.COLUMN_PRIORITY, newPriority);
                            contentValues.put(ListContract.ListEntry.COLUMN_ANNOTATION, newAnnotation);

                            mListQueryHandler.startUpdate(ListQueryHandler.UPDATE_LIST, null, uri, contentValues, null, null);
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
}
