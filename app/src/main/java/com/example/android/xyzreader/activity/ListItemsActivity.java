package com.example.android.xyzreader.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.xyzreader.R;
import com.example.android.xyzreader.R.array;
import com.example.android.xyzreader.R.dimen;
import com.example.android.xyzreader.R.id;
import com.example.android.xyzreader.R.layout;
import com.example.android.xyzreader.adapter.ListItemsAdapter;
import com.example.android.xyzreader.adapter.ListItemsAdapter.OnListItemClickListener;
import com.example.android.xyzreader.data.ArticleLoader;
import com.example.android.xyzreader.data.ItemsContract.Items;
import com.example.android.xyzreader.data.UpdaterService;

/**
 * Created by olgakuklina on 2016-05-04.
 */
public class ListItemsActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, OnRefreshListener, OnListItemClickListener {
    private static final String TAG = ListItemsActivity.class.getSimpleName();
    private LayoutManager mLayoutManager;
    private Adapter mAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Toolbar mToolbar;
    private boolean mIsRefreshing;

    private final BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                ListItemsActivity.this.mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                ListItemsActivity.this.updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        this.mSwipeRefreshLayout.setRefreshing(this.mIsRefreshing);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(layout.activity_items_list);
        this.mToolbar = (Toolbar) this.findViewById(id.toolbar);
        this.mRecyclerView = (RecyclerView) this.findViewById(id.items_list_recycler_view);
        this.mSwipeRefreshLayout = (SwipeRefreshLayout) this.findViewById(id.swipe_refresh_layout);
        ViewCompat.setElevation(this.mToolbar, this.getResources().getDimension(dimen.tb_elevation));

        this.setSupportActionBar(this.mToolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setTitle("");

        this.mSwipeRefreshLayout.setOnRefreshListener(this);
        this.mSwipeRefreshLayout.setColorSchemeColors(this.getResources().getIntArray(array.swipe_to_refresh_progress_colors));

        this.getSupportLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            this.refresh();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case id.menu_refresh:
                this.onRefresh();
                return true;
            default:
                return false;
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(ListItemsActivity.TAG, "onCreateLoader");
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data == null) {
            Log.v(ListItemsActivity.TAG, "onLoadFinished null");
        }
        else {
            Log.v(ListItemsActivity.TAG, "onLoadFinished " + data.getCount());
        }

        this.mAdapter = new ListItemsAdapter(this,data).setListener(this);
        this.mAdapter.setHasStableIds(true);
        this.mRecyclerView.setAdapter(this.mAdapter);

        this.mLayoutManager = new LinearLayoutManager(this);
        this.mRecyclerView.setLayoutManager(this.mLayoutManager);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onListItemSelected(long articleId) {

        this.startActivity(new Intent(Intent.ACTION_VIEW, Items.buildItemUri(articleId)));
    }
    @Override
    protected void onStart() {
        super.onStart();
        this.registerReceiver(this.mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unregisterReceiver(this.mRefreshingReceiver);
    }

    public void refresh() {
        this.startService(new Intent(this, UpdaterService.class));
    }

    @Override
    public void onRefresh() {
        Log.v(ListItemsActivity.TAG, "onRefresh");
        this.refresh();
    }
}
