package com.smartpocket.timeline2.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Displays an image in a square View
 */
public class SquareImageView extends ImageView {

    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        // make the view square
        //noinspection SuspiciousNameCombination
        setMeasuredDimension(width, width);
    }
}