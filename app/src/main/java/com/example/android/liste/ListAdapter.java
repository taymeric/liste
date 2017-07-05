package com.example.android.liste;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.liste.data.ListContract;


/**
 * Adapter class to manage display of items in the recycler view for the list.
 */

class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    public static final String TAG = "ListAdapter.java";

    private Cursor mCursor;
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private float mTextSize;
    private Drawable mPriorityMark;
    private ListAdapterOnClickListener mListAdapterOnClickListener;

    // A Context is needed for PreferenceUtils methods.
    ListAdapter(Context context, ListAdapterOnClickListener listener) {
        mContext = context;
        mListAdapterOnClickListener = listener;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mTextSize = PreferenceUtils.getTextSizeFromPrefs(
                mContext, mSharedPreferences, mContext.getString(R.string.pref_list_size_key));
        mPriorityMark = ContextCompat.getDrawable(mContext, R.drawable.ic_priority_high_black_24dp);
        mPriorityMark.setBounds(new Rect(0, 0, (int) mTextSize, (int) mTextSize));
        mPriorityMark.setAlpha(127);
        setHasStableIds(true);
    }

    // Creates new views
    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    // Replaces the contents of a view
    @Override
    public void onBindViewHolder(ListAdapter.ViewHolder holder, int position) {
        // Gets element at position and replaces the contents of the view with that element
        if (mCursor.moveToPosition(position)) {

            String s = mCursor.getString(mCursor.getColumnIndex(ListContract.ListEntry.COLUMN_STRING));
            holder.mTextView.setText(s);
            holder.mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);

            int p = mCursor.getInt(mCursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRIORITY));
            if (p == ListActivity.HIGH_PRIORITY)
                holder.mTextView.setCompoundDrawables(null, null, mPriorityMark, null);
            else
                holder.mTextView.setCompoundDrawables(null, null, null, null);

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
        long id;
        mCursor.moveToPosition(position);
        id = mCursor.getLong(mCursor.getColumnIndex(ListContract.ListEntry._ID));
        return id;
    }

    void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    void reloadSize() {
        mTextSize = PreferenceUtils.getTextSizeFromPrefs(
                mContext, mSharedPreferences, mContext.getString(R.string.pref_list_size_key));
    }

    interface ListAdapterOnClickListener {
        void onClick(int id);
    }

    // Provides a reference to the view(s) for each data item
    class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in our case
        TextView mTextView;
        int id;

        ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.item_text);
            itemView.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(
                        mContext, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        if (id > 0)
                            mListAdapterOnClickListener.onClick(id);
                        return true;
                    }
                });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });
        }
    }
}
