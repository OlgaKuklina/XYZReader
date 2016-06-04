package com.example.android.xyzreader.data;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;

import com.example.android.xyzreader.data.ItemsContract.Items;
import com.example.android.xyzreader.remote.RemoteEndpointUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UpdaterService extends IntentService {
    private static final String TAG = "UpdaterService";

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.example.xyzreader.intent.action.STATE_CHANGE";
    public static final String EXTRA_REFRESHING
            = "com.example.xyzreader.intent.extra.REFRESHING";

    public UpdaterService() {
        super(UpdaterService.TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Time time = new Time();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            Log.w(UpdaterService.TAG, "Not online, not refreshing.");
            return;
        }

        this.sendStickyBroadcast(new Intent(UpdaterService.BROADCAST_ACTION_STATE_CHANGE).putExtra(UpdaterService.EXTRA_REFRESHING, true));

        // Don't even inspect the intent, we only do one thing, and that's fetch content.
        ArrayList<ContentProviderOperation> cpo = new ArrayList<ContentProviderOperation>();

        Uri dirUri = Items.buildDirUri();

        // Delete all items
        cpo.add(ContentProviderOperation.newDelete(dirUri).build());

        try {
            JSONArray array = RemoteEndpointUtil.fetchJsonArray();
            if (array == null) {
                throw new JSONException("Invalid parsed item array");
            }

            for (int i = 0; i < array.length(); i++) {
                ContentValues values = new ContentValues();
                JSONObject object = array.getJSONObject(i);
                values.put(Items.SERVER_ID, object.getString("id"));
                values.put(Items.AUTHOR, object.getString("author"));
                values.put(Items.TITLE, object.getString("title"));
                values.put(Items.BODY, object.getString("body"));
                values.put(Items.THUMB_URL, object.getString("thumb"));
                values.put(Items.PHOTO_URL, object.getString("photo"));
                values.put(Items.ASPECT_RATIO, object.getString("aspect_ratio"));
                time.parse3339(object.getString("published_date"));
                values.put(Items.PUBLISHED_DATE, time.toMillis(false));
                cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
            }

            this.getContentResolver().applyBatch(ItemsContract.CONTENT_AUTHORITY, cpo);

        } catch (JSONException | RemoteException | OperationApplicationException e) {
            Log.e(UpdaterService.TAG, "Error updating content.", e);
        }

        this.sendStickyBroadcast(
                new Intent(UpdaterService.BROADCAST_ACTION_STATE_CHANGE).putExtra(UpdaterService.EXTRA_REFRESHING, false));
    }
}
