/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.xyzreader.ui;

import android.R;
import android.R.attr;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * A simple {@link LinearLayout} subclass that has a maxWidth
 */
public class MaxWidthLinearLayout extends LinearLayout {
    private static final int[] ATTRS = {
            attr.maxWidth
    };

    private int mMaxWidth = Integer.MAX_VALUE;

    public MaxWidthLinearLayout(Context context) {
        super(context);
        this.init(context, null, 0, 0);
    }

    public MaxWidthLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0, 0);
    }

    public MaxWidthLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context, attrs, defStyle, 0);
    }

    public MaxWidthLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, MaxWidthLinearLayout.ATTRS);
        this.mMaxWidth = a.getLayoutDimension(0, Integer.MAX_VALUE);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int newSpecWidth = Math.min(View.MeasureSpec.getSize(widthMeasureSpec), this.mMaxWidth);
        widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(newSpecWidth, View.MeasureSpec.getMode(widthMeasureSpec));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}