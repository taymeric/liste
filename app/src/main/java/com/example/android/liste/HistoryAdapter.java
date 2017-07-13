package com.example.android.liste;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.android.liste.data.ListContract;


/**
 * Adapter class to manage display of items in the recycler view for the history.
 */

class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    final private float mTextSize;
    final private AdapterOnClickHandler mClickHandler;
    private Cursor mCursor;

    // A Context is needed for PreferenceUtils methods.
    // An AdapterOnClickHandler is used to interact with History activity.
    HistoryAdapter(Context context, AdapterOnClickHandler clickHandler) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mTextSize = PreferenceUtils.getTextSizeFromPrefs(
                context, sharedPreferences, context.getString(R.string.pref_history_size_key));
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
            holder.mCheckBox.setText(s);
            holder.mCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
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

    // Provides a way to interact with an activity implementing this interface
    interface AdapterOnClickHandler {
        void onClick(String id, String txt);
    }

    // Provides a reference to the view(s) for each data item
    class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in a checkbox
        final CheckBox mCheckBox;

        ViewHolder(final View itemView) {
            super(itemView);
            mCheckBox = itemView.findViewById(R.id.item_text);
            // The OnCheckedChange callback uses the AdapterOnClickHandler
            // to update the data structure identifying the selected items.
            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    int adapterPosition = getAdapterPosition();
                    mCursor.moveToPosition(adapterPosition);
                    String id = mCursor.getString(mCursor.getColumnIndex(ListContract.HistoryEntry._ID));
                    String txt = mCursor.getString(mCursor.getColumnIndex(ListContract.HistoryEntry.COLUMN_STRING));
                    mClickHandler.onClick(id, txt);
                }
            });
        }
    }
}

