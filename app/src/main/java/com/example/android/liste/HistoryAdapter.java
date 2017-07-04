package com.example.android.liste;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.liste.data.ListContract;

import java.util.HashSet;

/**
 * Adapter class to manage display of items in the recycler view for the history.
 */

class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public  static final String TAG = "HistoryAdapter.java";

    private Cursor mCursor;
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private float mTextSize;
    private AdapterOnClickHandler mClickHandler;

    // A HashSet that contains selected elements identified by their position
    private HashSet<Integer> mSelectedPositions;

    // A Context is needed for PreferenceUtils methods.
    // An AdapterOnClickHandler is used to interact with History activity.
    HistoryAdapter(Context context, AdapterOnClickHandler clickHandler) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mTextSize = PreferenceUtils.getTextSizeFromPrefs(
                mContext, mSharedPreferences, mContext.getString(R.string.pref_history_size_key));
        mSelectedPositions = new HashSet<>();
        mClickHandler = clickHandler;
        setHasStableIds(true);
    }

    // Creates new views
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.history_item, parent, false);
        return new ViewHolder(v);
    }

    // Replaces the contents of a view
    @Override
    @SuppressLint("PrivateResource")
    public void onBindViewHolder(HistoryAdapter.ViewHolder holder, int position) {
        // Gets element at position and replaces the contents of the view with that element
        if (mCursor.moveToPosition(position)) {
            String s = mCursor.getString(mCursor.getColumnIndex(ListContract.HistoryEntry.COLUMN_STRING));
            holder.mTextView.setText(s);
            holder.mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);

            // Selection of an element is visually materialized by a different background color
            if (mSelectedPositions.contains(position))
                holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccentLight));
            else
                holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.background_material_light));
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
        id = mCursor.getLong(mCursor.getColumnIndex(ListContract.HistoryEntry._ID));
        return id;
    }

    void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    // Selected elements constitute the HashSet
    void clearSelection() {
        mSelectedPositions.clear();
    }

    // Provides a way to interact with an activity implementing this interface
    interface AdapterOnClickHandler {
        void onClick(String id, String txt);
    }

    // Provides a reference to the view(s) for each data item
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in our case
        TextView mTextView;

        ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.item_text);
            itemView.setOnClickListener(this);
        }

        // The onClick callback provides a way to both update the HashSet containing the selected
        // positions (in order to display the correct background) and use the AdapterOnClickHandler
        // to update the data structure identifying the selected items.
        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);

            if (mSelectedPositions.contains(adapterPosition))
                mSelectedPositions.remove(adapterPosition);
            else
                mSelectedPositions.add(adapterPosition);
            notifyItemChanged(adapterPosition);

            String id = mCursor.getString(mCursor.getColumnIndex(ListContract.HistoryEntry._ID));
            String txt = mCursor.getString(mCursor.getColumnIndex(ListContract.HistoryEntry.COLUMN_STRING));
            mClickHandler.onClick(id, txt);
        }
    }
}

