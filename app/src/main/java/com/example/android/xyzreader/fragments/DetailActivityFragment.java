package com.example.android.xyzreader.fragments;

import android.annotation.SuppressLint;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.content.Intent;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.ShareCompat.IntentBuilder;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.PaletteAsyncListener;
import android.support.v7.graphics.Palette.Swatch;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.android.xyzreader.R;
import com.example.android.xyzreader.R.bool;
import com.example.android.xyzreader.R.color;
import com.example.android.xyzreader.R.id;
import com.example.android.xyzreader.R.layout;
import com.example.android.xyzreader.R.string;
import com.example.android.xyzreader.activity.DetailActivity;
import com.example.android.xyzreader.activity.ListItemsActivity;
import com.example.android.xyzreader.data.ArticleLoader;
import com.example.android.xyzreader.data.ArticleLoader.Query;
import com.example.android.xyzreader.ui.DrawInsetsFrameLayout;
import com.example.android.xyzreader.ui.DrawInsetsFrameLayout.OnInsetsCallback;
import com.example.android.xyzreader.ui.ObservableScrollView;
import com.example.android.xyzreader.ui.ObservableScrollView.Callbacks;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;


/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ListItemsActivity} in two-pane mode (on
 * tablets) or a {@link DetailActivity} on handsets.
 */
public class DetailActivityFragment extends Fragment implements
        LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private ObservableScrollView mScrollView;
    private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;

    private int mTopInset;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private int mScrollY;
    private boolean mIsCard;
    private int mStatusBarFullOpacityBottom;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DetailActivityFragment() {
    }

    public static DetailActivityFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(DetailActivityFragment.ARG_ITEM_ID, itemId);
        DetailActivityFragment fragment = new DetailActivityFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(DetailActivityFragment.TAG, "onCreate");

        if (this.getArguments().containsKey(DetailActivityFragment.ARG_ITEM_ID)) {
            this.mItemId = this.getArguments().getLong(DetailActivityFragment.ARG_ITEM_ID);
        }

        this.mIsCard = this.getResources().getBoolean(bool.detail_is_card);
      //  mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(R.dimen.detail_card_top_margin);
        this.setHasOptionsMenu(true);
    }

    public DetailActivity getActivityCast() {
        return (DetailActivity) this.getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        this.getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(layout.fragment_detail, container, false);
        this.mDrawInsetsFrameLayout = (DrawInsetsFrameLayout)
                this.mRootView.findViewById(id.draw_insets_frame_layout);
        this.mDrawInsetsFrameLayout.setOnInsetsCallback(new OnInsetsCallback() {
            @Override
            public void onInsetsChanged(Rect insets) {
                DetailActivityFragment.this.mTopInset = insets.top;
            }
        });

        Window window = this.getActivity().getWindow();
        window.clearFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getActivity().getResources().getColor(color.statusBarColor));
        Log.v(DetailActivityFragment.TAG, "onCreateView");
        this.mScrollView = (ObservableScrollView) this.mRootView.findViewById(id.scrollview);
        this.mScrollView.setCallbacks(new Callbacks() {
            @Override
            public void onScrollChanged() {
                DetailActivityFragment.this.mScrollY = DetailActivityFragment.this.mScrollView.getScrollY();
                DetailActivityFragment.this.getActivityCast().onUpButtonFloorChanged(DetailActivityFragment.this.mItemId, DetailActivityFragment.this);
                DetailActivityFragment.this.mPhotoContainerView.setTranslationY((int) (DetailActivityFragment.this.mScrollY - DetailActivityFragment.this.mScrollY / DetailActivityFragment.PARALLAX_FACTOR));
                DetailActivityFragment.this.updateStatusBar();
            }
        });

        this.mPhotoView = (ImageView) this.mRootView.findViewById(id.photo);
        this.mPhotoContainerView = this.mRootView.findViewById(id.photo_container);

        this.mStatusBarColorDrawable = new ColorDrawable(0);

        this.mRootView.findViewById(id.share_fab).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                DetailActivityFragment.this.startActivity(Intent.createChooser(IntentBuilder.from(DetailActivityFragment.this.getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), DetailActivityFragment.this.getString(string.action_share)));
            }
        });

        this.bindViews();
        this.updateStatusBar();
        return this.mRootView;
    }

    private void updateStatusBar() {
        int color = 0;
        if (this.mPhotoView != null && this.mTopInset != 0 && this.mScrollY > 0) {
            float f = DetailActivityFragment.progress(this.mScrollY,
                    this.mStatusBarFullOpacityBottom - this.mTopInset * 3,
                    this.mStatusBarFullOpacityBottom - this.mTopInset);
            color = Color.argb((int) (255 * f),
                    (int) (Color.red(this.mMutedColor) * 0.9),
                    (int) (Color.green(this.mMutedColor) * 0.9),
                    (int) (Color.blue(this.mMutedColor) * 0.9));
        }
        this.mStatusBarColorDrawable.setColor(color);
        this.mDrawInsetsFrameLayout.setInsetBackground(this.mStatusBarColorDrawable);
    }

    static float progress(float v, float min, float max) {
        return DetailActivityFragment.constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private void bindViews() {
        if (this.mRootView == null) {
            return;
        }

        TextView titleView = (TextView) this.mRootView.findViewById(id.article_title);
        TextView bylineView = (TextView) this.mRootView.findViewById(id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) this.mRootView.findViewById(id.article_body);
//        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));
        PaletteAsyncListener paletteAsyncListener = new PaletteAsyncListener() {
            @SuppressLint("NewApi")
            @Override
            public void onGenerated(Palette palette) {
                if (DetailActivityFragment.this.getActivity() == null)
                    return;
                Log.v(DetailActivityFragment.TAG, "textSwatch.PaletteAsyncListener");

                Swatch textSwatch = palette.getMutedSwatch();
                Swatch bgSwatch = palette.getDarkVibrantSwatch();

                DetailActivityFragment.this.mRootView.findViewById(id.meta_bar).setBackgroundColor(bgSwatch.getBodyTextColor());
                DetailActivityFragment.this.updateStatusBar();
            }
        };
        if (this.mCursor != null) {
            this.mRootView.setAlpha(0);
            this.mRootView.setVisibility(View.VISIBLE);
            this.mRootView.animate().alpha(1);
            titleView.setText(this.mCursor.getString(Query.TITLE));
            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            this.mCursor.getLong(Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL)
                            + " by <font color='#ffffff'>"
                            + this.mCursor.getString(Query.AUTHOR)
                            + "</font>"));
            bodyView.setText(Html.fromHtml(this.mCursor.getString(Query.BODY)));

            Callback callback = new Callback() {

                @Override
                public void onSuccess() {
                    Bitmap bitmapBg = ((BitmapDrawable) DetailActivityFragment.this.mPhotoView.getDrawable()).getBitmap();
                    if (bitmapBg != null) {
                        Palette p = Palette.generate(bitmapBg, 12);
                        DetailActivityFragment.this.mMutedColor = p.getDarkMutedColor(0xFF333333);
                        DetailActivityFragment.this.mRootView.findViewById(id.meta_bar)
                                .setBackgroundColor(DetailActivityFragment.this.mMutedColor);
                        DetailActivityFragment.this.updateStatusBar();
                    }
                }

                @Override
                public void onError() {
                    Log.e(DetailActivityFragment.TAG, "Image failed to load");
                }
            };
            Picasso pic = Picasso.with(this.getActivity());
            float aspectRatio = this.mCursor.getFloat(Query.ASPECT_RATIO);
            Log.v(DetailActivityFragment.TAG, "aspectRatio = " + aspectRatio);
            int  screenWidth = this.getActivity().getBaseContext().getResources().getDisplayMetrics().widthPixels;
            pic.load(this.mCursor.getString(Query.THUMB_URL))
                    .placeholder(color.colorPrimary)
                    .resize(screenWidth, (int) (screenWidth/aspectRatio))
                    .into(this.mPhotoView, callback);

        } else {
            this.mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A" );
            bodyView.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(this.getActivity(), this.mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!this.isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        this.mCursor = cursor;
        if (this.mCursor != null && !this.mCursor.moveToFirst()) {
            Log.e(DetailActivityFragment.TAG, "Error reading item detail cursor");
            this.mCursor.close();
            this.mCursor = null;
        }

        this.bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        this.mCursor = null;
        this.bindViews();
    }

    public int getUpButtonFloor() {
        if (this.mPhotoContainerView == null || this.mPhotoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        // account for parallax
        return this.mIsCard
                ? (int) this.mPhotoContainerView.getTranslationY() + this.mPhotoView.getHeight() - this.mScrollY
                : this.mPhotoView.getHeight() - this.mScrollY;
    }
}

