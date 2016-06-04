/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications:
 * -Imported from AOSP frameworks/base/core/java/com/android/internal/content
 * -Changed package name
 */

package com.example.android.xyzreader.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Helper for building selection clauses for {@link SQLiteDatabase}. Each
 * appended clause is combined using {@code AND}. This class is <em>not</em>
 * thread safe.
 */
public class SelectionBuilder {
    private String mTable;
    private HashMap<String, String> mProjectionMap;
    private StringBuilder mSelection;
    private ArrayList<String> mSelectionArgs;

    /**
     * Reset any internal state, allowing this builder to be recycled.
     */
    public SelectionBuilder reset() {
        this.mTable = null;
		if (this.mProjectionMap != null) {
            this.mProjectionMap.clear();
		}
		if (this.mSelection != null) {
            this.mSelection.setLength(0);
		}
		if (this.mSelectionArgs != null) {
            this.mSelectionArgs.clear();
		}
        return this;
    }

    /**
     * Append the given selection clause to the internal state. Each clause is
     * surrounded with parenthesis and combined using {@code AND}.
     */
    public SelectionBuilder where(String selection, String... selectionArgs) {
        if (TextUtils.isEmpty(selection)) {
            if (selectionArgs != null && selectionArgs.length > 0) {
                throw new IllegalArgumentException(
                        "Valid selection required when including arguments=");
            }

            // Shortcut when clause is empty
            return this;
        }

        this.ensureSelection(selection.length());
        if (this.mSelection.length() > 0) {
            this.mSelection.append(" AND ");
        }

        this.mSelection.append("(").append(selection).append(")");
        if (selectionArgs != null) {
            this.ensureSelectionArgs();
            for (String arg : selectionArgs) {
                this.mSelectionArgs.add(arg);
            }
        }

        return this;
    }

    public SelectionBuilder table(String table) {
        this.mTable = table;
        return this;
    }

    private void assertTable() {
        if (this.mTable == null) {
            throw new IllegalStateException("Table not specified");
        }
    }

    private void ensureProjectionMap() {
		if (this.mProjectionMap == null) {
            this.mProjectionMap = new HashMap<String, String>();
		}
    }

    private void ensureSelection(int lengthHint) {
    	if (this.mSelection == null) {
            this.mSelection = new StringBuilder(lengthHint + 8);
    	}
    }

    private void ensureSelectionArgs() {
    	if (this.mSelectionArgs == null) {
            this.mSelectionArgs = new ArrayList<String>();
    	}
    }

    public SelectionBuilder mapToTable(String column, String table) {
        this.ensureProjectionMap();
        this.mProjectionMap.put(column, table + "." + column);
        return this;
    }

    public SelectionBuilder map(String fromColumn, String toClause) {
        this.ensureProjectionMap();
        this.mProjectionMap.put(fromColumn, toClause + " AS " + fromColumn);
        return this;
    }

    /**
     * Return selection string for current internal state.
     *
     * @see #getSelectionArgs()
     */
    public String getSelection() {
    	if (this.mSelection != null) {
            return this.mSelection.toString();
    	} else {
    		return null;
    	}
    }

    /**
     * Return selection arguments for current internal state.
     *
     * @see #getSelection()
     */
    public String[] getSelectionArgs() {
    	if (this.mSelectionArgs != null) {
            return this.mSelectionArgs.toArray(new String[this.mSelectionArgs.size()]);
    	} else {
    		return null;
    	}
    }

    private void mapColumns(String[] columns) {
    	if (this.mProjectionMap == null) return;
        for (int i = 0; i < columns.length; i++) {
            String target = this.mProjectionMap.get(columns[i]);
            if (target != null) {
                columns[i] = target;
            }
        }
    }

    @Override
    public String toString() {
        return "SelectionBuilder[table=" + this.mTable + ", selection=" + this.getSelection()
                + ", selectionArgs=" + Arrays.toString(this.getSelectionArgs()) + "]";
    }

    /**
     * Execute query using the current internal state as {@code WHERE} clause.
     */
    public Cursor query(SQLiteDatabase db, String[] columns, String orderBy) {
        return this.query(db, columns, null, null, orderBy, null);
    }

    /**
     * Execute query using the current internal state as {@code WHERE} clause.
     */
    public Cursor query(SQLiteDatabase db, String[] columns, String groupBy,
                        String having, String orderBy, String limit) {
        this.assertTable();
        if (columns != null) this.mapColumns(columns);
        return db.query(this.mTable, columns, this.getSelection(), this.getSelectionArgs(), groupBy, having,
                orderBy, limit);
    }

    /**
     * Execute update using the current internal state as {@code WHERE} clause.
     */
    public int update(SQLiteDatabase db, ContentValues values) {
        this.assertTable();
        return db.update(this.mTable, values, this.getSelection(), this.getSelectionArgs());
    }

    /**
     * Execute delete using the current internal state as {@code WHERE} clause.
     */
    public int delete(SQLiteDatabase db) {
        this.assertTable();
        return db.delete(this.mTable, this.getSelection(), this.getSelectionArgs());
    }
}
