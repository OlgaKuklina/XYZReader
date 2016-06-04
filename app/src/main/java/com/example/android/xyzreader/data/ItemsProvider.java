
package com.example.android.xyzreader.data;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.example.android.xyzreader.data.ItemsContract.Items;

import java.util.ArrayList;
import java.util.List;

public class ItemsProvider extends ContentProvider {
	private SQLiteOpenHelper mOpenHelper;

	interface Tables {
		String ITEMS = "items";
	}

	private static final int ITEMS = 0;
	private static final int ITEMS__ID = 1;

	private static final UriMatcher sUriMatcher = ItemsProvider.buildUriMatcher();

	private static UriMatcher buildUriMatcher() {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		String authority = ItemsContract.CONTENT_AUTHORITY;
		matcher.addURI(authority, "items", ItemsProvider.ITEMS);
		matcher.addURI(authority, "items/#", ItemsProvider.ITEMS__ID);
		return matcher;
	}

	@Override
	public boolean onCreate() {
		this.mOpenHelper = new ItemsDatabase(this.getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		int match = ItemsProvider.sUriMatcher.match(uri);
		switch (match) {
			case ItemsProvider.ITEMS:
				return Items.CONTENT_TYPE;
			case ItemsProvider.ITEMS__ID:
				return Items.CONTENT_ITEM_TYPE;
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = this.mOpenHelper.getReadableDatabase();
		SelectionBuilder builder = this.buildSelection(uri);
		Cursor cursor = builder.where(selection, selectionArgs).query(db, projection, sortOrder);
        if (cursor != null) {
            cursor.setNotificationUri(this.getContext().getContentResolver(), uri);
        }
        return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
		int match = ItemsProvider.sUriMatcher.match(uri);
		switch (match) {
			case ItemsProvider.ITEMS:
				final long _id = db.insertOrThrow(Tables.ITEMS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return ItemsContract.Items.buildItemUri(_id);
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
		SelectionBuilder builder = this.buildSelection(uri);
		this.getContext().getContentResolver().notifyChange(uri, null);
		return builder.where(selection, selectionArgs).update(db, values);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
		SelectionBuilder builder = this.buildSelection(uri);
		this.getContext().getContentResolver().notifyChange(uri, null);
		return builder.where(selection, selectionArgs).delete(db);
	}

	private SelectionBuilder buildSelection(Uri uri) {
		SelectionBuilder builder = new SelectionBuilder();
		int match = ItemsProvider.sUriMatcher.match(uri);
		return this.buildSelection(uri, match, builder);
	}

	private SelectionBuilder buildSelection(Uri uri, int match, SelectionBuilder builder) {
		List<String> paths = uri.getPathSegments();
		switch (match) {
			case ItemsProvider.ITEMS:
				return builder.table(Tables.ITEMS);
			case ItemsProvider.ITEMS__ID:
				final String _id = paths.get(1);
				return builder.table(Tables.ITEMS).where(ItemsContract.Items._ID + "=?", _id);
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

    /**
     * Apply the given set of {@link ContentProviderOperation}, executing inside
     * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
     * any single one fails.
     */
    @Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int numOperations = operations.size();
            ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }
}
