package com.example.android.liste;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.liste.data.ListContract;

/**
 * Adapter class to manage display of items in the recycler view for the list.
 */

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    public static final String TAG = "ListAdapter.java";

    private Cursor mCursor;
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private float mTextSize;

    // A Context is needed for PreferenceUtils methods.
    public ListAdapter(Context context) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mTextSize = PreferenceUtils.getTextSizeFromPrefs(
                mContext, mSharedPreferences, mContext.getString(R.string.pref_list_size_key));
        setHasStableIds(true);
    }

    // Creates new views
    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replaces the contents of a view
    @Override
    public void onBindViewHolder(ListAdapter.ViewHolder holder, int position) {
        // Gets element at position and replaces the contents of the view with that element
        if (mCursor.moveToPosition(position)) {
            String s = mCursor.getString(mCursor.getColumnIndex(ListContract.ListEntry.COLUMN_STRING));
            holder.mTextView.setText(s);
            holder.mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
            // A tag containing the Id of the element in the table is needed
            // to handle delete-on-swipe from the List activity.
            int i = mCursor.getInt(mCursor.getColumnIndex(ListContract.ListEntry._ID));
            holder.itemView.setTag(i);
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

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public void reloadSize() {
        mTextSize = PreferenceUtils.getTextSizeFromPrefs(
                mContext, mSharedPreferences, mContext.getString(R.string.pref_list_size_key));
    }

    // Provides a reference to the view(s) for each data item
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in our case
        public TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.item_text);
        }
    }
}
