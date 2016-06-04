package com.example.android.xyzreader.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.xyzreader.data.ItemsContract.ItemsColumns;

import static com.example.android.xyzreader.data.ItemsProvider.Tables;

public class ItemsDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "xyzreader.db";
    private static final int DATABASE_VERSION = 1;

    public ItemsDatabase(Context context) {
        super(context, ItemsDatabase.DATABASE_NAME, null, ItemsDatabase.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ItemsProvider.Tables.ITEMS + " ("
                + ItemsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ItemsColumns.SERVER_ID + " TEXT,"
                + ItemsColumns.TITLE + " TEXT NOT NULL,"
                + ItemsColumns.AUTHOR + " TEXT NOT NULL,"
                + ItemsColumns.BODY + " TEXT NOT NULL,"
                + ItemsColumns.THUMB_URL + " TEXT NOT NULL,"
                + ItemsColumns.PHOTO_URL + " TEXT NOT NULL,"
                + ItemsColumns.ASPECT_RATIO + " REAL NOT NULL DEFAULT 1.5,"
                + ItemsColumns.PUBLISHED_DATE + " INTEGER NOT NULL DEFAULT 0"
                + ")" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ItemsProvider.Tables.ITEMS);
        this.onCreate(db);
    }
}
