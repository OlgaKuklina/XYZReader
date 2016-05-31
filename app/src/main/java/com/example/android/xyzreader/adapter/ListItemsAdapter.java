package com.example.android.xyzreader.adapter;

import android.app.Activity;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.xyzreader.R;
import com.example.android.xyzreader.activity.ListItemsActivity;
import com.example.android.xyzreader.data.ArticleLoader;
import com.example.android.xyzreader.ui.DynamicHeightNetworkImageView;
import com.example.android.xyzreader.ui.ImageLoaderHelper;

/**
 * Created by olgakuklina on 2016-05-04.
 */
public class ListItemsAdapter extends RecyclerView.Adapter<ListItemsAdapter.ViewHolder> {

    public static final String TAG = ListItemsAdapter.class.getSimpleName();

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
        Log.v(TAG, "getItemId = " + position );
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public DynamicHeightNetworkImageView thumbnailView;
            public TextView titleView;
            public TextView subtitleView;

            public ViewHolder(View v) {
                super(v);
                thumbnailView = (DynamicHeightNetworkImageView) v.findViewById(R.id.thumbnail);
                titleView = (TextView) v.findViewById(R.id.article_title);
                subtitleView = (TextView) v.findViewById(R.id.article_subtitle);
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


        holder.thumbnailView.setImageUrl(
                mCursor.getString(ArticleLoader.Query.THUMB_URL),
                ImageLoaderHelper.getInstance(mActivity).getImageLoader());
        holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));

    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
}