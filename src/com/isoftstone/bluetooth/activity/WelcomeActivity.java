package com.isoftstone.bluetooth.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;

import com.isoftstone.bluetooth.R;

public  class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        new Handler().postDelayed(new Runnable() {
            //设置延迟2.5s
            @Override
            public void run() {
                Intent intent = new Intent(WelcomeActivity. this, SecondActivity.class);
                startActivity(intent);
                WelcomeActivity.this.finish();
            }

        }, 2500);




    }
}
