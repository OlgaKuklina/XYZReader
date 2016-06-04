package com.example.android.xyzreader.adapter;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.example.android.xyzreader.R;
import com.example.android.xyzreader.R.color;
import com.example.android.xyzreader.R.id;
import com.example.android.xyzreader.R.layout;
import com.example.android.xyzreader.adapter.ListItemsAdapter.ViewHolder;
import com.example.android.xyzreader.data.ArticleLoader;
import com.example.android.xyzreader.data.ArticleLoader.Query;
import com.example.android.xyzreader.data.ItemsContract;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import static android.support.v4.app.ActivityCompat.startActivity;

/**
 * Created by olgakuklina on 2016-05-04.
 */
public class ListItemsAdapter extends Adapter<ViewHolder> {

    public static final String TAG = ListItemsAdapter.class.getSimpleName();
    private final int screenWidth;

    public ListItemsAdapter(Activity activity, Cursor cursor) {

        this.mActivity = activity;
        this.mCursor = cursor;
        this.screenWidth = this.mActivity.getBaseContext().getResources().getDisplayMetrics().widthPixels;
        Log.v(ListItemsAdapter.TAG, "screenWidth = " + this.screenWidth);
    }

    public interface OnListItemClickListener {
        void onListItemSelected(long articleId);

        ListItemsAdapter.OnListItemClickListener listner = new ListItemsAdapter.OnListItemClickListener() {
            @Override public void onListItemSelected(long itemId) {
            }
        };
    }
    private ListItemsAdapter.OnListItemClickListener mListener = ListItemsAdapter.OnListItemClickListener.listner;
    private final Activity mActivity;
    private final Cursor mCursor;

    public ListItemsAdapter setListener(@NonNull ListItemsAdapter.OnListItemClickListener listener) {
        this.mListener = listener;
        return this;
    }
    @Override
    public long getItemId(int position) {
        Log.v(ListItemsAdapter.TAG, "getItemId = " + position );
        this.mCursor.moveToPosition(position);
        return this.mCursor.getLong(Query._ID);
    }

        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case

            public ImageView thumbnailView;
            public TextView titleView;
            public TextView subtitleView;

            public ViewHolder(View v) {
                super(v);
                this.thumbnailView = (ImageView) v.findViewById(id.thumbnail);
                this.titleView = (TextView) v.findViewById(id.article_title);
                this.subtitleView = (TextView) v.findViewById(id.article_subtitle);
                v.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ListItemsAdapter.this.mListener.onListItemSelected(ViewHolder.this.getItemId());
                    }
                });
            }
        }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Log.v(ListItemsAdapter.TAG, "ListItemsAdapter::onCreateViewHolder");

        View view =  LayoutInflater.from(parent.getContext()).inflate(layout.list_item, parent, false);

        ListItemsAdapter.ViewHolder vh = new ListItemsAdapter.ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(ListItemsAdapter.ViewHolder holder, int position) {

        this.mCursor.moveToPosition(position);
        holder.titleView.setBackgroundColor(ContextCompat.getColor(this.mActivity, color.colorPrimary));
        holder.titleView.setText(this.mCursor.getString(Query.TITLE));
        holder.subtitleView.setBackgroundColor(ContextCompat.getColor(this.mActivity, color.colorPrimary));
        holder.subtitleView.setText(
                DateUtils.getRelativeTimeSpanString(
                        this.mCursor.getLong(Query.PUBLISHED_DATE),
                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL)
                        + " by "
                        + this.mCursor.getString(Query.AUTHOR));
        float aspectRatio = this.mCursor.getFloat(Query.ASPECT_RATIO);
        Log.v(ListItemsAdapter.TAG, "aspectRatio = " + aspectRatio);
        holder.thumbnailView.setLayoutParams(new LayoutParams(this.screenWidth, (int) (this.screenWidth /aspectRatio)));
        Picasso pic = Picasso.with(this.mActivity);
        pic.load(this.mCursor.getString(Query.THUMB_URL))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .placeholder(color.colorPrimary)
                .into(holder.thumbnailView);
    }

    @Override
    public int getItemCount() {
        return this.mCursor.getCount();
    }
}