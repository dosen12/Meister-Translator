package com.squareround.meistertranslator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

public class MainActivity2 extends AppCompatActivity {

    ShimmerTextView tv,tv1;
    Shimmer shimmer;
    Button btn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        tv = (ShimmerTextView) findViewById(R.id.textView);
        tv1 = (ShimmerTextView) findViewById(R.id.textView2);
        btn = findViewById( R.id.FButton );

        if (shimmer != null && shimmer.isAnimating()) {
            shimmer.cancel();
        } else {
            shimmer = new Shimmer();
            shimmer.start(tv);
            shimmer.start(tv1);
            shimmer.setDuration(2500);
        }
        btn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View v ) {
                Intent intent = new Intent( getApplicationContext(), GetStoredActivity.class );
                startActivity( intent );
            }

        } );
    }
}
