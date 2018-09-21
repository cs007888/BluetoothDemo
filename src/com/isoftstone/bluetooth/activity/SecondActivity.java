package com.isoftstone.bluetooth.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;

import com.isoftstone.bluetooth.R;
import com.isoftstone.bluetooth.p2p.WifiDirectActivity;

import java.util.ArrayList;

public class SecondActivity extends Activity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

        ArrayList<View> views = new ArrayList<View>();
        ImageView v_start, v_ly, v_wf;
        boolean isclose = true;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
         v_start = (ImageView) findViewById(R.id.btn_start);
         v_wf = (ImageView) findViewById(R.id.wf);
         v_ly = (ImageView) findViewById(R.id.ly);
         v_wf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SecondActivity.this, WifiDirectActivity.class);
                startActivity(intent);
            }
        });
        v_ly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SecondActivity.this, BluetoothActivity.class);
                startActivity(intent);
            }
        });



        views.add(v_start);
        views.add(v_wf);
        views.add(v_ly);


        v_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isclose) {
                    star();
                } else {
                    close();
                }
            }
        });

    }


    private void star() {
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(
                views.get(0), "alpha", 1F, 0.5F);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(
                views.get(1), "translationX", -200F);
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(
                views.get(2), "translationX", 200F);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(500);

        set.setInterpolator(new BounceInterpolator());
        set.playTogether(animator1, animator2, animator3);
        set.start();
        isclose = false;

    }

    private void close() {
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(
                views.get(0), "alpha", 0.5F, 1F);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(
                views.get(1), "translationX", 0F);
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(
                views.get(2), "translationX", 0F);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(500);
        set.setInterpolator(new BounceInterpolator());
        set.playTogether(animator1, animator2, animator3);
        set.start();
        isclose = true;


    }
}

