package com.squareround.meistertranslator;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;

import com.squareround.meistertranslator.databinding.ActivityGetstoredvideoBinding;

import java.util.Vector;

public class GetStoredActivity extends AppCompatActivity {

    private static final int GSV = R.layout.activity_getstoredvideo;
    private ActivityGetstoredvideoBinding binding;
    private Context context = this;
    private Intent intent;

    private ServiceConnection serviceClients;
    private ServiceConnection serviceSttClient;
    private FFMPEGLinker ffClient;
    private TextToTextClient tttClient;
    private SpeechToTextClient sttClient;
    private ClientExecuter client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, GSV);

        binding.menuListView.setLayoutManager(new GridLayoutManager(this, 3));
        GSVAdapter adapter = new GSVAdapter(this);

        binding.menuListView.setAdapter(adapter);
        getVideo();
        adapter.setUp(getVideo());





        intent = new Intent( GetStoredActivity.this, ClientExecuter.class );
        ffClient = new FFMPEGLinker();
        tttClient = new TextToTextClient( "" );
//        client = new ClientExecuter( context, ffClient, tttClient, sttClient );
        serviceClients = new ServiceConnection() {

            @Override
            public void onServiceConnected( ComponentName name, IBinder service ) {
                ClientExecuter.ClientBinder clientBinder = ( ClientExecuter.ClientBinder )service;
                client = clientBinder.getService();
                System.out.println( " >>> 실행중..." );
            }

            @Override
            public void onServiceDisconnected( ComponentName name ) {
            }

        };
        adapter.setItemListener( new GSVAdapter.ItemListener() {

            @Override
            public void item( Uri uri ) {
                if( !ClientExecuter.getExecuting() ) {
                    System.out.println( " >>> 실행하지 않고있다." );
//                    client = new ClientExecuter( context, ffClient, tttClient, sttClient );
                    bindService( intent, serviceClients, BIND_AUTO_CREATE );
//                    client.execute( uri.toString() );
                } else {
                    System.out.println( " >>> 이미 실행되고 있다." );
//                    client.getget();
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
    protected void onStart() {
        super.onStart();

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
        bindService( new Intent( this, SpeechToTextClient.class ), serviceSttClient, BIND_AUTO_CREATE );
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindService( serviceSttClient );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService( serviceClients );
    }

}