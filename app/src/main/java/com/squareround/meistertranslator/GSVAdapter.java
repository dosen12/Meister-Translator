package com.squareround.meistertranslator;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.squareround.meistertranslator.databinding.ItemMenuBinding;

import java.util.Vector;

public class GSVAdapter extends RecyclerView.Adapter<GSVAdapter.GSVHolder> {

    private Vector<Menu> menus = new Vector<>();
    private Context context;
    private Activity activity;

    private ItemListener itemListener;

    public GSVAdapter(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    @NonNull
    @Override
    public GSVHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        ItemMenuBinding binding = ItemMenuBinding.inflate(LayoutInflater.from(context), viewGroup, false);
        return new GSVHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GSVHolder gsvHolder, int position) {
        ItemMenuBinding binding = gsvHolder.binding;
        Menu menu = menus.get(position);
        String title = menu.getTitle();
        Bitmap img = menu.getImg();
        final Uri uri = menu.getUri();
        binding.menuTitleImgView.setImageBitmap(img);
        //다이얼로그로 동영상의 Uri를 보내며 다이얼로그를 띄우는코드.
        binding.menuTitleImgView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ShowVideoDialog dialog = new ShowVideoDialog(activity, uri, activity);
                itemListener.item( uri );
            }
        } );
    }

    @Override
    public int getItemCount() {
        return menus.size();
    }

    public void setUp(Vector<Menu> menus) {
        this.menus = menus;
        notifyDataSetChanged();
    }

    class GSVHolder extends RecyclerView.ViewHolder {
        ItemMenuBinding binding;

        GSVHolder(ItemMenuBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public interface ItemListener {

        void item( Uri uri );

    }

    public void setItemListener( ItemListener itemListener ) {
        this.itemListener = itemListener;
    }
}