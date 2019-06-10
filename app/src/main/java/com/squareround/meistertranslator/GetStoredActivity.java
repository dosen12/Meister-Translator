package com.squareround.meistertranslator;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.widget.FrameLayout;

import com.github.jlmd.animatedcircleloadingview.AnimatedCircleLoadingView;
import com.squareround.meistertranslator.databinding.ActivityGetstoredvideoBinding;

import java.util.Vector;

public class GetStoredActivity extends AppCompatActivity {

    private static final int GSV = R.layout.activity_getstoredvideo;
    private ActivityGetstoredvideoBinding binding;
    private boolean started = false;

    private ServiceConnection serviceClients;
    private ServiceConnection serviceSttClient;
    private FFMPEGLinker ffClient;
    private TextToTextClient tttClient;
    private SpeechToTextClient sttClient;
    private ClientExecuter client;
    private FrameLayout layout;
    private ClientLoadingView clientLoadingView;
    private String pathFile;
    private boolean inBackground;
    private boolean comebackStart;

    @Override
    public void onBackPressed() {
        if( ClientExecuter.getExecuting() ) {
            unbindService( serviceClients );
            clientLoadingView.setProgress( 0, 300 );
            clientLoadingView.stopFailure();
            bindService( new Intent( this, ClientExecuter.class ), serviceClients, BIND_AUTO_CREATE );
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, GSV);

        binding.menuListView.setLayoutManager(new GridLayoutManager(this, 3));
        GSVAdapter adapter = new GSVAdapter(this);

        binding.menuListView.setAdapter(adapter);
        getVideo();
        adapter.setUp(getVideo());





        serviceClients = new ServiceConnection() {

            @Override
            public void onServiceConnected( ComponentName name, IBinder service ) {
                ClientExecuter.ClientBinder clientBinder = ( ClientExecuter.ClientBinder )service;
                client = clientBinder.getService();
            }

            @Override
            public void onServiceDisconnected( ComponentName name ) {
                client = null;
            }

        };
        serviceSttClient = new ServiceConnection() {

            @Override
            public void onServiceConnected( ComponentName name, IBinder service ) {
                sttClient = SpeechToTextClient.from( service );
            }

            @Override
            public void onServiceDisconnected( ComponentName name ) {
                sttClient = null;
            }

        };
        bindService( new Intent( this, ClientExecuter.class ), serviceClients, BIND_AUTO_CREATE );
        bindService( new Intent( this, SpeechToTextClient.class ), serviceSttClient, BIND_AUTO_CREATE );
        layout = findViewById( R.id.layout );
        clientLoadingView = findViewById( R.id.circle_loading_view );
        clientLoadingView.startDeterminate();
        clientLoadingView.setAnimationListener( new AnimatedCircleLoadingView.AnimationListener() {

            @Override
            public void onAnimationEnd( final boolean success ) {
                runOnUiThread( new Runnable() {

                    @Override
                    public void run() {
                        layout.setBackgroundColor( Color.argb( 0, 0, 100, 200 ) );
                        clientLoadingView.getViewAnimator().hideChildViews();
                        if( success ) {
                            if( ( client.getListKey() != null ) && ( client.getListValue() != null ) && !started && !inBackground ) {
                                started = true;
                                Intent intent = new Intent( getApplicationContext(), VideoPlayerActivity.class );
                                intent.putExtra( "URI", pathFile );
                                intent.putIntegerArrayListExtra( "SyncKeys", client.getListKey() );
                                intent.putStringArrayListExtra( "SyncValues", client.getListValue() );
                                startActivity( intent );
                                unbindService( serviceClients );
                                bindService( new Intent( getApplicationContext(), ClientExecuter.class ), serviceClients, BIND_AUTO_CREATE );
                            } else if( inBackground ) {
                                comebackStart = true;
                            }
                        }
                    }

                });

            }

        });
        ffClient = new FFMPEGLinker();
        tttClient = new TextToTextClient( "" );
        adapter.setItemListener( new GSVAdapter.ItemListener() {

            @Override
            public void item( Uri uri ) {

                if( !ClientExecuter.getExecuting() ) {
                    started = false;
                    pathFile = uri.toString();
                    layout.setBackgroundColor( Color.argb( 255, 0, 100, 200 ) );
                    clientLoadingView.reset();
                    clientLoadingView.startIndeterminate();
                    client.startClients( ffClient, tttClient, sttClient, clientLoadingView, pathFile );
                }

            }

        } );
    }

    private Vector<Menu> getVideo() {
        String[] proj = {MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA
        };
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        Vector<Menu> menus = new Vector<>();
        assert cursor != null;
        while (cursor.moveToNext()) {
            String title = cursor.getString(1);
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID));
            Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), id, MediaStore.Video.Thumbnails.MINI_KIND, null);

            // 썸네일 크기 변경할 때.
            Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, 500, 450);
            String data = cursor.getString(2);
            menus.add(new Menu(title, thumbnail, Uri.parse(data)));
        }

        cursor.close();
        return menus;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService( serviceClients );
        unbindService( serviceSttClient );
    }

    @Override
    protected void onPause() {
        super.onPause();

        comebackStart = false;
        inBackground = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        inBackground = false;
        if( comebackStart ) {
            comebackStart = false;
            Intent intent = new Intent( getApplicationContext(), VideoPlayerActivity.class );
            intent.putExtra( "URI", pathFile );
            intent.putIntegerArrayListExtra( "SyncKeys", client.getListKey() );
            intent.putStringArrayListExtra( "SyncValues", client.getListValue() );
            startActivity( intent );
            unbindService( serviceClients );
            bindService( new Intent( this, ClientExecuter.class ), serviceClients, BIND_AUTO_CREATE );
        }
    }

}
