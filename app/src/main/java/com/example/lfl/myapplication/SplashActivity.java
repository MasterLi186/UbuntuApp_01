package com.example.lfl.myapplication;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.iv1)
    ImageView iv1;
    private static final String TAG = "SplashActivity";
    private final int splashFlag = 0;
    private final long splashTime = 3000;
    private Intent mainActivityIntent;
    private Handler mUiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case splashFlag:
                    startActivity(mainActivityIntent);
                    SplashActivity.this.finish();
                    break;
            }
        }
    };
    private class DelayAdsThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                SystemClock.sleep(splashTime);
                mUiHandler.sendEmptyMessage(splashFlag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        iv1.setOnClickListener(this);
        iv1.setImageResource(R.mipmap.ic_back_image);
        mainActivityIntent = new Intent(this,MainActivity.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
//        animForCode();
//        new DelayAdsThread().start();
        mUiHandler.sendEmptyMessageDelayed(splashFlag,splashTime);
    }

    private void animForCode() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(iv1,"scaleX",0f,1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(iv1,"scaleY",0f,1f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(2000);
        animatorSet.play(scaleX).with(scaleY);
        animatorSet.setTarget(iv1);
        animatorSet.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv1:
                Log.d(TAG, "onClick: ");
                break;
        }
    }
}
