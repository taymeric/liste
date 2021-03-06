package com.athebapps.android.list;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.athebapps.android.list.database.ListContract;


/**
 * Adapter class to manage the display of items from the database table 'list' in a recycler view.
 */
class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    /** Identifies a normal layout */
    private static final int ONE_COLUMN_LAYOUT = 1;

    /* Identifies a compact layout */
    private static final int TWO_COLUMNS_LAYOUT = 2;

    /* Context object needed for using PreferenceUtils methods and Context.getString() as well as
     * getting a reference to SharedPreferences */
    final private Context mContext;

    /* SharedPreferences object for using PreferenceUtils methods that get Preferences
     * for font and layout */
    final private SharedPreferences mSharedPreferences;

    /* Reference to an implementation of the interface that handles click on a ViewHolder */
    final private ListAdapterOnClickHandler mListAdapterOnClickHandler;

    /* The Cursor that references the actual data that populates the RecyclerView.
     * Can be null before data has been loaded. */
    private Cursor mCursor;

    /* Type of Layout for the RecyclerView. Used to adjust the layout of a ViewHolder.
     * Possible values: PreferenceUtils.NORMAL_LAYOUT_ITEM or PreferenceUtils.COMPACT_LAYOUT_ITEM */
    private int mCurrentLayout;

    /* Font used for TextViews in a ViewHolder. */
    private Typeface mTypeface;

    /** @param context  needed for PreferenceUtils methods and other operations.
     *  @param clickHandler used to interact with List activity. */
    ListAdapter(Context context, ListAdapterOnClickHandler clickHandler) {
        mContext = context;
        mListAdapterOnClickHandler = clickHandler;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        String layoutType = mSharedPreferences.getString(
                mContext.getString(R.string.pref_list_layout_key),
                mContext.getString(R.string.pref_list_layout_two_columns_value));
        if (layoutType.equals(mContext.getString(R.string.pref_list_layout_two_columns_value)))
            mCurrentLayout = TWO_COLUMNS_LAYOUT;
        else
            mCurrentLayout = ONE_COLUMN_LAYOUT;

        // Optimization
        setHasStableIds(true);
    }

    @Override
    public int getItemViewType (int position) {
        return mCurrentLayout;
    }

    @NonNull
    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // There are two possible layouts for the RecyclerView: Normal or Compact.
        // For each of these two layouts, the layout of a ViewHolder is different.
        View v;
        if (viewType == ONE_COLUMN_LAYOUT) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_normal, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_compact, parent, false);
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ListAdapter.ViewHolder holder, int position) {
        // Gets element at 'position' and replaces the content of the view with that element
        if (mCursor.moveToPosition(position)) {

            String product = mCursor.getString(mCursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRODUCT));
            String annotation = mCursor.getString(mCursor.getColumnIndex(ListContract.ListEntry.COLUMN_ANNOTATION));
            int priority = mCursor.getInt(mCursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRIORITY));

            holder.mAnnotationTextView.setVisibility(View.GONE);
            if (annotation != null && !annotation.equals("")) {
                holder.mAnnotationTextView.setVisibility(View.VISIBLE);
                holder.mAnnotationTextView.setText(annotation);
            }

            holder.mProductTextView.setText(product);

            if (mTypeface != null) {
                holder.mProductTextView.setTypeface(mTypeface);
                holder.mAnnotationTextView.setTypeface(mTypeface);
                holder.mPriorityView.setTypeface(mTypeface);
            }

            switch (priority) {
                case ListContract.ListEntry.HIGH_PRIORITY_PRODUCT:
                    holder.mPriorityView.setText(mContext.getString(R.string.list_high_priority_mark));
                    holder.mPriorityView.setVisibility(View.VISIBLE);
                    break;
                case ListContract.ListEntry.LOW_PRIORITY_PRODUCT:
                    holder.mPriorityView.setText(mContext.getString(R.string.list_low_priority_mark));
                    holder.mPriorityView.setVisibility(View.VISIBLE);
                    break;
                default: // Default priority : no mark.
                    // For compact layout, we save space by using View.GONE when the priority marker is
                    // not visible. For normal layout, View.INVISIBLE gives a better alignment.
                    if (mCurrentLayout == ONE_COLUMN_LAYOUT)
                        holder.mPriorityView.setVisibility(View.INVISIBLE);
                    else
                        holder.mPriorityView.setVisibility(View.GONE);
                    break;
            }

            // A tag containing the Id of the element in the table is needed
            // to handle delete-on-swipe from the List activity.
            holder.id = mCursor.getInt(mCursor.getColumnIndex(ListContract.ListEntry._ID));
            holder.itemView.setTag(holder.id);
        }
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(mCursor.getColumnIndex(ListContract.ListEntry._ID));
    }

    /** Updates the value of the cursor that points to data to be displayed in the RecyclerView */
    void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    /** Updates the font value for TextViews to the most recent value from the user's preferences */
    void reloadFont(Typeface typeface) {
        mTypeface = typeface;
        notifyDataSetChanged();
    }

    /** Updates the layout of the RecyclerView to the most recent value from the user's preferences */
    void reloadLayout() {
        String layoutType = mSharedPreferences.getString(
                mContext.getString(R.string.pref_list_layout_key),
                mContext.getString(R.string.pref_list_layout_two_columns_value));
        if (layoutType.equals(mContext.getString(R.string.pref_list_layout_two_columns_value)))
            mCurrentLayout = TWO_COLUMNS_LAYOUT;
        else
            mCurrentLayout = ONE_COLUMN_LAYOUT;
        notifyDataSetChanged();
    }

    /** The following interface declares the 'onClick' method that should be overridden
     * by classes using a ListAdapter to provide an implementation of the set of actions
     * performed when an item of the RecyclerView is clicked. */
    interface ListAdapterOnClickHandler {
        /**
         * @param id the _id in the list SQL table of the clicked item
         * */
        void onClick(int id);
    }

    /** Our ViewHolder for Recycling purpose */
    class ViewHolder extends RecyclerView.ViewHolder {

        /* Name of the product */
        final TextView mProductTextView;

        /* Annotation for the product */
        final TextView mAnnotationTextView;

        /* Priority marker for the product */
        final TextView mPriorityView;

        /* id in the table (_ID column) of the item to be displayed */
        int id;

        ViewHolder(View itemView) {
            super(itemView);
            mProductTextView = itemView.findViewById(R.id.item_product);
            mAnnotationTextView = itemView.findViewById(R.id.item_annotation);
            mPriorityView = itemView.findViewById(R.id.item_priority);

            itemView.setOnTouchListener(new View.OnTouchListener() {

                final private GestureDetector gestureDetector =
                                new GestureDetector(
                                        mContext,
                                        new GestureDetector.SimpleOnGestureListener() {
                                            /*@Override
                                            public void onLongPress(MotionEvent e) {
                                                if (id > 0)
                                                    mListAdapterOnClickHandler.onClick(id);
                                                super.onLongPress(e);
                                            }*/

                                            @Override
                                            public boolean onSingleTapConfirmed(MotionEvent e) {
                                                if (id > 0)
                                                    mListAdapterOnClickHandler.onClick(id);
                                                return super.onSingleTapConfirmed(e);
                                            }
                                        });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    v.performClick();
                    return true;
                }
            });
        }
    }
}
