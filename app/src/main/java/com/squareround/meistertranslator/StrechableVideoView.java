package com.squareround.meistertranslator;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.VideoView;

public class StrechableVideoView extends VideoView {

    private Context context;

    public StrechableVideoView( Context context ) {
        super( context );
        this.context = context;
    }

    public StrechableVideoView( Context context, AttributeSet attrs ) {
        super( context, attrs );
        this.context = context;
    }

    public StrechableVideoView( Context context, AttributeSet attrs, int defStyleAttr ) {
        super( context, attrs, defStyleAttr );
        this.context = context;
    }

    @Override
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
        DisplayMetrics display = context.getResources().getDisplayMetrics();
        if( display == null ) {
            super.onMeasure( widthMeasureSpec, heightMeasureSpec );
        } else {
            setMeasuredDimension( display.widthPixels, display.heightPixels );
        }
    }

}
