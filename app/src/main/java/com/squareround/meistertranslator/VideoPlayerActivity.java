package com.squareround.meistertranslator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;

public class VideoPlayerActivity extends AppCompatActivity {

    private TextView videoSubtitle;
    private StrechableVideoView videoPlayer;
    private MediaController videoController;

    private String subtitle;
    private int controllerHeight;
    private boolean end = false;
    private SparseArray< String > videoSyncMap;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_video_player );

        Intent intent = getIntent();
        String videoPath = intent.getStringExtra( "URI" );
        videoSubtitle = findViewById( R.id.video_subtitle );
        videoSubtitle.setText( "안녕하세요. 자막테스트 입니다." );
        videoPlayer = findViewById( R.id.video_player );
        videoPlayer.setVideoPath( videoPath );
        videoController = new MediaController( this );
        videoController.setAnchorView( videoPlayer );
        videoPlayer.setMediaController( videoController );
        videoPlayer.start();
        videoSyncMap = new SparseArray<>();
        ArrayList< Integer > keys = intent.getIntegerArrayListExtra( "SyncKeys" );
        ArrayList< String > values = intent.getStringArrayListExtra( "SyncValues" );

        for( int i = 0; ( i < keys.size() ) || ( i < values.size() ); i++ ) {
            videoSyncMap.put( keys.get( i ), values.get( i ) );
        }

        Thread refresher = new Thread( new Runnable() {

            @Override
            public void run() {
                while( !end ) {
                    controllerHeight = videoController.getMeasuredHeight();
                    int videoSeek = videoPlayer.getCurrentPosition();
                    int nearSeek = -1;

                    for( int i = 0; i < videoSyncMap.size(); i++ ) {
                        int key = videoSyncMap.keyAt( i );
                        if( ( key < videoSeek ) && ( key > nearSeek ) ) {
                            nearSeek = key;
                        }
                    }
                    subtitle = nearSeek < 0 ? "" : videoSyncMap.get( nearSeek );

                    if( !videoSubtitle.getText().toString().equals( subtitle ) ) {
                        runOnUiThread( new Runnable() {

                            @Override
                            public void run() {
                                videoSubtitle.setText( subtitle );
                            }

                        } );
                    }
                    if( ( ( !videoController.isShowing() ) && ( videoSubtitle.getPaddingBottom() != 0 ) ) ||
                            ( ( videoController.isShowing() ) && ( videoSubtitle.getPaddingBottom() != controllerHeight ) ) ) {
                        runOnUiThread( new Runnable() {

                            @Override
                            public void run() {
                                int controllerHeight = videoController.getMeasuredHeight();
                                if( ( ( !videoController.isShowing() ) && ( videoSubtitle.getPaddingBottom() != 0 ) ) ) {
                                    videoSubtitle.setPadding( 0, 0, 0, 0 );
                                }
                                if( ( ( videoController.isShowing() ) && ( videoSubtitle.getPaddingBottom() != controllerHeight ) ) ) {
                                    videoSubtitle.setPadding( 0, 0, 0, controllerHeight );
                                    videoController.show( 0 );
                                }
                            }

                        } );
                    }
                }
            }

        } );
        refresher.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        end = true;
    }
}
