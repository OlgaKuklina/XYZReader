/*
 * Copyright 2014 Google Inc.
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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import com.example.android.xyzreader.R;
import com.example.android.xyzreader.R.styleable;


public class DrawInsetsFrameLayout extends FrameLayout {
    private Drawable mInsetBackground;
    private Drawable mTopInsetBackground;
    private Drawable mBottomInsetBackground;
    private Drawable mSideInsetBackground;

    private Rect mInsets;
    private final Rect mTempRect = new Rect();
    private DrawInsetsFrameLayout.OnInsetsCallback mOnInsetsCallback;

    public DrawInsetsFrameLayout(Context context) {
        super(context);
        this.init(context, null, 0);
    }

    public DrawInsetsFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }

    public DrawInsetsFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                styleable.DrawInsetsFrameLayout, defStyle, 0);
        assert a != null;

        this.mInsetBackground = a.getDrawable(styleable.DrawInsetsFrameLayout_insetBackground);

        a.recycle();
    }

    public void setInsetBackground(Drawable insetBackground) {
        if (this.mInsetBackground != null) {
            this.mInsetBackground.setCallback(null);
        }

        if (insetBackground != null) {
            insetBackground.setCallback(this);
        }

        this.mInsetBackground = insetBackground;
        this.postInvalidateOnAnimation();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            this.requestApplyInsets();
        }
        if (this.mInsetBackground != null) {
            this.mInsetBackground.setCallback(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mInsetBackground != null) {
            this.mInsetBackground.setCallback(null);
        }
    }

    public void setOnInsetsCallback(DrawInsetsFrameLayout.OnInsetsCallback onInsetsCallback) {
        this.mOnInsetsCallback = onInsetsCallback;
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        insets = super.onApplyWindowInsets(insets);
        this.mInsets = new Rect(
                insets.getSystemWindowInsetLeft(),
                insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(),
                insets.getSystemWindowInsetBottom());
        this.setWillNotDraw(false);
        this.postInvalidateOnAnimation();
        if (this.mOnInsetsCallback != null) {
            this.mOnInsetsCallback.onInsetsChanged(this.mInsets);
        }
        return insets;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        int width = this.getWidth();
        int height = this.getHeight();

        if (this.mInsets != null) {
            // Top
            this.mTempRect.set(0, 0, width, this.mInsets.top);
            if (this.mInsetBackground != null) {
                this.mInsetBackground.setBounds(this.mTempRect);
                this.mInsetBackground.draw(canvas);
            }

            // Bottom
            this.mTempRect.set(0, height - this.mInsets.bottom, width, height);
            if (this.mInsetBackground != null) {
                this.mInsetBackground.setBounds(this.mTempRect);
                this.mInsetBackground.draw(canvas);
            }

            // Left
            this.mTempRect.set(0, this.mInsets.top, this.mInsets.left, height - this.mInsets.bottom);
            if (this.mInsetBackground != null) {
                this.mInsetBackground.setBounds(this.mTempRect);
                this.mInsetBackground.draw(canvas);
            }

            // Right
            this.mTempRect.set(width - this.mInsets.right, this.mInsets.top, width, height - this.mInsets.bottom);
            if (this.mInsetBackground != null) {
                this.mInsetBackground.setBounds(this.mTempRect);
                this.mInsetBackground.draw(canvas);
            }
        }
    }

    public interface OnInsetsCallback {
        void onInsetsChanged(Rect insets);
    }
}
