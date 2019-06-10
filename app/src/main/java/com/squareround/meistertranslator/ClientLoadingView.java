package com.squareround.meistertranslator;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;

public class ClientLoadingView extends AnimatedCircleLoadingView {

    Context context;

    public ClientLoadingView( Context context ) {
        super( context );

        this.context = context;
    }

    public ClientLoadingView( Context context, AttributeSet attrs ) {
        super( context, attrs );

        this.context = context;
    }

    public ClientLoadingView( Context context, AttributeSet attrs, int defStyleAttr ) {
        super( context, attrs, defStyleAttr );

        this.context = context;
    }

    public void setProgress( final int progress, final int progressMax ) {
        ( new Thread( new Runnable() {

            @Override
            public void run() {
                changePercent( Math.min( Math.max( Math.round( ( float )( progress * 100 ) / ( float )progressMax ), 0 ), 100 ) );
            }

        }) ).start();
    }

    public void changePercent( final int percent ) {
        ( ( Activity )context ).runOnUiThread( new Runnable() {

            @Override
            public void run() {
                setPercent( percent );
            }

        });
    }

    public void reset() {
        ( ( Activity )context ).runOnUiThread( new Runnable() {

            @Override
            public void run() {
                resetLoading();
            }

        });
    }

}
