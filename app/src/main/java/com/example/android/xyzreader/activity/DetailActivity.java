package com.example.android.xyzreader.activity;

import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowInsets;

import com.example.android.xyzreader.R.id;
import com.example.android.xyzreader.R.layout;
import com.example.android.xyzreader.data.ArticleLoader;
import com.example.android.xyzreader.data.ArticleLoader.Query;
import com.example.android.xyzreader.data.ItemsContract.Items;
import com.example.android.xyzreader.fragments.DetailActivityFragment;


/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class DetailActivity extends MainActivity
        implements LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private long mStartId;

    private long mSelectedItemId;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private int mTopInset;

    private ViewPager mPager;
    private DetailActivity.MyPagerAdapter mPagerAdapter;
    private View mUpButtonContainer;
    private View mUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            this.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        this.setContentView(layout.activity_detail);

        this.getSupportLoaderManager().initLoader(0, null, this);

        this.mPagerAdapter = new DetailActivity.MyPagerAdapter(this.getSupportFragmentManager());
        this.mPager = (ViewPager) this.findViewById(id.pager);
        this.mPager.setAdapter(this.mPagerAdapter);
        this.mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, this.getResources().getDisplayMetrics()));
        this.mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        this.mPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                DetailActivity.this.mUpButton.animate()
                        .alpha(state == ViewPager.SCROLL_STATE_IDLE ? 1f : 0f)
                        .setDuration(300);
            }

            @Override
            public void onPageSelected(int position) {
                if (DetailActivity.this.mCursor != null) {
                    DetailActivity.this.mCursor.moveToPosition(position);
                }
                DetailActivity.this.mSelectedItemId = DetailActivity.this.mCursor.getLong(Query._ID);
                DetailActivity.this.updateUpButtonPosition();
            }
        });

        this.mUpButtonContainer = this.findViewById(id.up_container);

        this.mUpButton = this.findViewById(id.action_up);
        this.mUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                DetailActivity.this.onSupportNavigateUp();
            }
        });

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            this.mUpButtonContainer.setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    view.onApplyWindowInsets(windowInsets);
                    DetailActivity.this.mTopInset = windowInsets.getSystemWindowInsetTop();
                    DetailActivity.this.mUpButtonContainer.setTranslationY(DetailActivity.this.mTopInset);
                    DetailActivity.this.updateUpButtonPosition();
                    return windowInsets;
                }
            });
        }

        if (savedInstanceState == null) {
            if (this.getIntent() != null && this.getIntent().getData() != null) {
                this.mStartId = Items.getItemId(this.getIntent().getData());
                this.mSelectedItemId = this.mStartId;
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        this.mCursor = cursor;
        this.mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (this.mStartId > 0) {
            this.mCursor.moveToFirst();
            // TODO: optimize
            while (!this.mCursor.isAfterLast()) {
                if (this.mCursor.getLong(Query._ID) == this.mStartId) {
                    int position = this.mCursor.getPosition();
                    this.mPager.setCurrentItem(position, false);
                    break;
                }
                this.mCursor.moveToNext();
            }
            this.mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        this.mCursor = null;
        this.mPagerAdapter.notifyDataSetChanged();
    }

    public void onUpButtonFloorChanged(long itemId, DetailActivityFragment fragment) {
        if (itemId == this.mSelectedItemId) {
            this.mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
            this.updateUpButtonPosition();
        }
    }

    private void updateUpButtonPosition() {
        int upButtonNormalBottom = this.mTopInset + this.mUpButton.getHeight();
        this.mUpButton.setTranslationY(Math.min(this.mSelectedItemUpButtonFloor - upButtonNormalBottom, 0));
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            DetailActivityFragment fragment = (DetailActivityFragment) object;
            if (fragment != null) {
                DetailActivity.this.mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
                DetailActivity.this.updateUpButtonPosition();
            }
        }

        @Override
        public Fragment getItem(int position) {
            DetailActivity.this.mCursor.moveToPosition(position);
            return DetailActivityFragment.newInstance(DetailActivity.this.mCursor.getLong(Query._ID));
        }

        @Override
        public int getCount() {
            return DetailActivity.this.mCursor != null ? DetailActivity.this.mCursor.getCount() : 0;
        }
    }
}

