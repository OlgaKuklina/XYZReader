package com.example.android.xyzreader.adapter;

import android.app.Activity;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.xyzreader.R;
import com.example.android.xyzreader.data.ArticleLoader;

/**
 * Created by olgakuklina on 2016-05-04.
 */
public class ListItemsAdapter extends RecyclerView.Adapter<ListItemsAdapter.ViewHolder> {
    public ListItemsAdapter(Activity activity, Cursor cursor) {

        mActivity = activity;
        mInflater = LayoutInflater.from(activity);
        mCursor = cursor;
    }

    public interface OnListItemClickListener {
        void onListItemSelected(long articleId);

        OnListItemClickListener listner = new OnListItemClickListener() {
            @Override public void onListItemSelected(long itemId) {}
        };
    }
    private OnListItemClickListener mListener = OnListItemClickListener.listner;
    private final Activity mActivity;
    private final LayoutInflater mInflater;
    private final Cursor mCursor;

    public ListItemsAdapter setListener(@NonNull OnListItemClickListener listener) {
        mListener = listener;
        return this;
    }
    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case

            public TextView titleView;
            public TextView subtitleView;

            public ViewHolder(View v) {
                super(v);
                titleView = (TextView) v.findViewById(R.id.article_title);
                subtitleView = (TextView) v.findViewById(R.id.article_subtitle);
            }
        }


    @Override
    public ListItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

        final ViewHolder vh = new ViewHolder(view);

        return vh;


    }

    @Override
    public void onBindViewHolder(ListItemsAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}