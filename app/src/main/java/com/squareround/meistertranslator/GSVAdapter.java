package com.squareround.meistertranslator;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.github.jlmd.animatedcircleloadingview.AnimatedCircleLoadingView;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;
import com.squareround.meistertranslator.databinding.ItemMenuBinding;

import java.util.Vector;

public class GSVAdapter extends RecyclerView.Adapter<GSVAdapter.GSVHolder> {

    private Vector<Menu> menus = new Vector<>();
    private Context context;
    private Activity activity;
    private AnimatedCircleLoadingView animatedCircleLoadingView;

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
                if( !ClientExecuter.getExecuting() ) {
                    new FancyAlertDialog.Builder(activity)
                    .setTitle("이 동영상을 번역하시겠습니까?")
                    .setBackgroundColor(Color.parseColor("#F39C12"))  //Don't pass R.color.colorvalue
                    .setNegativeBtnText("취소")
                    .setPositiveBtnBackground(Color.parseColor("#F39C12"))  //Don't pass R.color.colorvalue
                    .setPositiveBtnText("실행")
                    .setNegativeBtnBackground(Color.parseColor("#FFA9A7A8"))  //Don't pass R.color.colorvalue
                    .setAnimation(Animation.POP)
                    .isCancellable(true)
                    .setIcon(R.drawable.ic_error_outline_black_24dp, Icon.Visible)
                    .OnPositiveClicked(new FancyAlertDialogListener() {
                        @Override
                        public void OnClick() {
                            animatedCircleLoadingView = activity.findViewById(R.id.circle_loading_view);
                            startLoading();
                            startPercentMockThread();
                            FrameLayout layout = activity.findViewById( R.id.layout );
                            animatedCircleLoadingView.setMinimumHeight( layout.getMeasuredHeight() );
                            layout.setBackgroundColor( Color.argb( 255, 0, 100, 200 ) );
                            itemListener.item( uri );
                        }
                    })
                    .OnNegativeClicked(new FancyAlertDialogListener() {
                        @Override
                        public void OnClick() {
                        }
                    })
                    .build();
                }
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

    private void startLoading() {
    animatedCircleLoadingView.startDeterminate();
  }

  private void startPercentMockThread() {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(1500);
          for (int i = 0; i <= 100; i++) {
              if( i >= 80 ) {
                  break;
              }
            Thread.sleep(65);
            changePercent(i);
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    };
    new Thread(runnable).start();
  }

  private void changePercent(final int percent) {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        animatedCircleLoadingView.setPercent(percent);
      }
    });
  }

  public void resetLoading() {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        animatedCircleLoadingView.resetLoading();
      }
    });
  }

    public interface ItemListener {

        void item( Uri uri );

    }

    public void setItemListener( ItemListener itemListener ) {
        this.itemListener = itemListener;
    }
}