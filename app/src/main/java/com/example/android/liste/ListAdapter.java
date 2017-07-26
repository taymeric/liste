package com.example.android.liste;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
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

    final private Context mContext;
    final private SharedPreferences mSharedPreferences;
    final private ListAdapterOnClickListener mListAdapterOnClickListener;
    private final Drawable mHighPriorityMark;
    private final Drawable mLowPriorityMark;
    private int mCurrentLayout;
    private Cursor mCursor;
    private String mFontFamily;

    // A Context is needed for PreferenceUtils methods.
    ListAdapter(Context context, ListAdapterOnClickListener listener) {
        mContext = context;
        mListAdapterOnClickListener = listener;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        mFontFamily = PreferenceUtils.getFont(mContext, mSharedPreferences);

        mCurrentLayout = PreferenceUtils.getListLayoutType(mContext, mSharedPreferences);

        float iconSize = PreferenceUtils.getIconSize(mContext, mSharedPreferences);

        mHighPriorityMark = ContextCompat.getDrawable(mContext, R.drawable.ic_priority_exclamation);
        mHighPriorityMark.setBounds(new Rect(0, 0, (int) iconSize, (int) iconSize));

        mLowPriorityMark = ContextCompat.getDrawable(mContext, R.drawable.ic_priority_question);
        mLowPriorityMark.setBounds(new Rect(0, 0, (int) iconSize, (int) iconSize));

        setHasStableIds(true);
    }

    @Override
    public int getItemViewType (int position) {
        return mCurrentLayout;
    }

    // Creates new views
    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == PreferenceUtils.NORMAL_LAYOUT_ITEM) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_normal, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_compact, parent, false);
        }
        return new ViewHolder(v);
    }

    // Replaces the contents of a view
    @Override
    public void onBindViewHolder(ListAdapter.ViewHolder holder, int position) {
        // Gets element at position and replaces the contents of the view with that element
        if (mCursor.moveToPosition(position)) {

            String product = mCursor.getString(mCursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRODUCT));
            String annotation = mCursor.getString(mCursor.getColumnIndex(ListContract.ListEntry.COLUMN_ANNOTATION));
            int priority = mCursor.getInt(mCursor.getColumnIndex(ListContract.ListEntry.COLUMN_PRIORITY));

            holder.mAnnotationTextView.setVisibility(View.GONE);
            if (annotation != null) {
                annotation = annotation.trim();
                if (!annotation.equals("")) {
                    holder.mAnnotationTextView.setVisibility(View.VISIBLE);
                    holder.mAnnotationTextView.setText(annotation);
                }
            }

            holder.mProductTextView.setText(product);

            holder.mProductTextView.setTypeface(Typeface.create(mFontFamily, Typeface.NORMAL));
            holder.mAnnotationTextView.setTypeface(Typeface.create(mFontFamily, Typeface.NORMAL));

            if (priority == ListActivity.HIGH_PRIORITY)
                holder.mProductTextView.setCompoundDrawables(null, null, mHighPriorityMark, null);
            else if (priority == ListActivity.LOW_PRIORITY)
                holder.mProductTextView.setCompoundDrawables(null, null, mLowPriorityMark, null);
            else
                holder.mProductTextView.setCompoundDrawables(null, null, null, null);

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

    void reloadFont() {
        mFontFamily = PreferenceUtils.getFont(mContext, mSharedPreferences);
        notifyDataSetChanged();
    }

    void reloadLayout() {
        mCurrentLayout = PreferenceUtils.getListLayoutType(mContext, mSharedPreferences);
        notifyDataSetChanged();
    }

    interface ListAdapterOnClickListener {
        void onClick(int id);
    }

    // Provides a reference to the view(s) for each data item
    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mProductTextView;
        final TextView mAnnotationTextView;
        int id;

        ViewHolder(View itemView) {
            super(itemView);
            mProductTextView = itemView.findViewById(R.id.item_product);
            mAnnotationTextView = itemView.findViewById(R.id.item_annotation);

            itemView.setOnTouchListener(new View.OnTouchListener() {
                final private GestureDetector gestureDetector = new GestureDetector(
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
