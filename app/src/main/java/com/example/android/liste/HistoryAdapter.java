package com.example.android.liste;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.android.liste.database.ListContract;


/**
 * Adapter class to manage display of items in the recycler view for the history.
 */
class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    /** Identifies a normal layout */
    private static final int NORMAL_LAYOUT = 1;

    /* Identifies a compact layout */
    private static final int COMPACT_LAYOUT = 2;

    /* Context object needed for using PreferenceUtils methods as well as
     * getting a reference to SharedPreferences */
    final private Context mContext;

    /* SharedPreferences object for using PreferenceUtils methods that get Preferences
     * for layout */
    final private SharedPreferences mSharedPreferences;
    /* Reference to an implementation of the interface that handles click on a ViewHolder */
    final private HistoryAdapterOnClickHandler mClickHandler;
    /* Type of Layout for the RecyclerView. Used to adjust the layout of a ViewHolder.
     * Possible values: PreferenceUtils.NORMAL_LAYOUT_ITEM or PreferenceUtils.COMPACT_LAYOUT_ITEM */
    private int mCurrentLayout;
    /* The Cursor that references the actual data that populates the RecyclerView.
     * Can be null before data has been loaded. */
    private Cursor mCursor;

    /* Array that indicated if a product at a given position is checked or not */
    private boolean[] isChecked;

    /** @param context  needed for PreferenceUtils methods.
     *  @param clickHandler used to interact with History activity. */
    HistoryAdapter(Context context, HistoryAdapterOnClickHandler clickHandler) {

        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        boolean isCompactLayout = mSharedPreferences.getBoolean(
                mContext.getString(R.string.pref_history_compact_layout_key), true);
        if (isCompactLayout)
            mCurrentLayout = COMPACT_LAYOUT;
        else
            mCurrentLayout = NORMAL_LAYOUT;

        mClickHandler = clickHandler;

        // Handles initialization of isChecked array
        resetIsChecked();

        // Optimization
        setHasStableIds(true);
    }

    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // There are two possible layouts for the RecyclerView: Normal or Compact.
        // For each of these two layouts, the layout of a ViewHolder is different.
        View v;
        if (viewType == NORMAL_LAYOUT) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history_normal, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history_compact, parent, false);
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(HistoryAdapter.ViewHolder holder, int position) {
        // Gets element at 'position' and replaces the contents of the view with that element
        if (mCursor.moveToPosition(position)) {
            String s = mCursor.getString(mCursor.getColumnIndex(ListContract.HistoryEntry.COLUMN_PRODUCT));
            holder.mCheckBox.setText(s);

            holder.mCheckBox.setOnCheckedChangeListener(null);
            holder.mCheckBox.setChecked(isChecked[position]);
            final int thisPosition = position;
            holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    isChecked[thisPosition] = b;
                    mCursor.moveToPosition(thisPosition);
                    String id = mCursor.getString(mCursor.getColumnIndex(ListContract.HistoryEntry._ID));
                    String txt = mCursor.getString(mCursor.getColumnIndex(ListContract.HistoryEntry.COLUMN_PRODUCT));
                    mClickHandler.onClick(id, txt);
                }
            });
        }
    }

    @Override
    public int getItemViewType (int position) {
        return mCurrentLayout;
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(mCursor.getColumnIndex(ListContract.HistoryEntry._ID));
    }

    /** Updates the value of the cursor that points to data to be displayed in the RecyclerView */
    void swapCursor(Cursor cursor) {
        mCursor = cursor;
        resetIsChecked();
        notifyDataSetChanged();
    }

    /** Initializes or reset array that indicates if a product is currently checked */
    private void resetIsChecked() {
        if (mCursor != null)
            isChecked = new boolean[mCursor.getCount()];
        else
            isChecked = null;
    }

    /** Updates the layout of the RecyclerView to the most recent value from the user's preferences */
    void reloadLayout() {
        boolean isCompactLayout = mSharedPreferences.getBoolean(
                mContext.getString(R.string.pref_history_compact_layout_key), true);
        if (isCompactLayout)
            mCurrentLayout = COMPACT_LAYOUT;
        else
            mCurrentLayout = NORMAL_LAYOUT;
        notifyDataSetChanged();
    }

    /* Interface to provide a way to handle both addition and deletion by the corresponding activity.*/
    interface HistoryAdapterOnClickHandler {
        /*
         * @param id the _id in the list SQL table of the clicked item
         * @param txt the name of the product for the clicked item
         */
        void onClick(String id, String txt);
    }

    /** Our ViewHolder for Recycling purpose */
    class ViewHolder extends RecyclerView.ViewHolder {

        /* A Checkbox with a text corresponding to the name of a product */
        final CheckBox mCheckBox;

        ViewHolder(final View itemView) {
            super(itemView);
            mCheckBox = itemView.findViewById(R.id.item_product);
        }
    }
}

