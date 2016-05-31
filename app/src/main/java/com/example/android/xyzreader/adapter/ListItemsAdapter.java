package com.example.android.xyzreader.adapter;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.xyzreader.R;
import com.example.android.xyzreader.data.ArticleLoader;
import com.example.android.xyzreader.data.ItemsContract;
import com.squareup.picasso.Picasso;

import static android.support.v4.app.ActivityCompat.startActivity;

/**
 * Created by olgakuklina on 2016-05-04.
 */
public class ListItemsAdapter extends RecyclerView.Adapter<ListItemsAdapter.ViewHolder> {

    public static final String TAG = ListItemsAdapter.class.getSimpleName();
    private final int screenWidth;

    public ListItemsAdapter(Activity activity, Cursor cursor) {

        mActivity = activity;
        mCursor = cursor;
        screenWidth = mActivity.getBaseContext().getResources().getDisplayMetrics().widthPixels;
        Log.v(TAG, "screenWidth = " + screenWidth);
    }

    public interface OnListItemClickListener {
        void onListItemSelected(long articleId);

        OnListItemClickListener listner = new OnListItemClickListener() {
            @Override public void onListItemSelected(long itemId) {
            }
        };
    }
    private OnListItemClickListener mListener = OnListItemClickListener.listner;
    private final Activity mActivity;
    private final Cursor mCursor;

    public ListItemsAdapter setListener(@NonNull OnListItemClickListener listener) {
        mListener = listener;
        return this;
    }
    @Override
    public long getItemId(int position) {
        Log.v(TAG, "getItemId = " + position );
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case

            public ImageView thumbnailView;
            public TextView titleView;
            public TextView subtitleView;

            public ViewHolder(View v) {
                super(v);
                thumbnailView = (ImageView) v.findViewById(R.id.thumbnail);
                titleView = (TextView) v.findViewById(R.id.article_title);
                subtitleView = (TextView) v.findViewById(R.id.article_subtitle);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.onListItemSelected(getItemId());
                    }
                });
            }
        }

    @Override
    public ListItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Log.v(TAG, "ListItemsAdapter::onCreateViewHolder");

        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

        final ViewHolder vh = new ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(ListItemsAdapter.ViewHolder holder, int position) {

        mCursor.moveToPosition(position);
        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        holder.subtitleView.setText(
                DateUtils.getRelativeTimeSpanString(
                        mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL).toString()
                        + " by "
                        + mCursor.getString(ArticleLoader.Query.AUTHOR));
        float aspectRatio = mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO);
        Log.v(TAG, "aspectRatio = " + aspectRatio);
        holder.thumbnailView.setLayoutParams(new LinearLayout.LayoutParams((int) (screenWidth), (int) (screenWidth/aspectRatio)));
        Picasso pic = Picasso.with(mActivity);
        pic.load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                .into(holder.thumbnailView);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
}