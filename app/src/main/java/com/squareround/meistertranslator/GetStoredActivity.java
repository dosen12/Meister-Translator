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
    ActivityGetstoredvideoBinding binding;
    Context context = this;

    FFMPEGLinker ffClient;
    TextToTextClient tttClient;
    SpeechToTextClient sttClient;
    ClientExecuter client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, GSV);

        binding.menuListView.setLayoutManager(new GridLayoutManager(this, 3));
        GSVAdapter adapter = new GSVAdapter(this);

        binding.menuListView.setAdapter(adapter);
        getVideo();
        adapter.setUp(getVideo());





        ffClient = new FFMPEGLinker();
        tttClient = new TextToTextClient( "" );
        client = new ClientExecuter( context, ffClient, tttClient, sttClient );
        adapter.setItemListener( new GSVAdapter.ItemListener() {

            @Override
            public void item( Uri uri ) {
                if( !client.getExecuting() ) {
                    client = new ClientExecuter( context, ffClient, tttClient, sttClient );
                    client.execute( uri.toString() );
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

        bindService( new Intent( this, SpeechToTextClient.class ), new ServiceConnection() {

            @Override
            public void onServiceConnected( ComponentName name, IBinder service ) {
                sttClient = SpeechToTextClient.from( service );
            }

            @Override
            public void onServiceDisconnected( ComponentName name ) {
                sttClient.unbindService( this );
                sttClient = null;
            }

        }, BIND_AUTO_CREATE );
    }

}