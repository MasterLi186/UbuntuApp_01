package com.example.lfl.myapplication;

import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private Button tv;
    private String s = "123";
    private MessagesEvent messagesEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.tv1);
        tv.setOnClickListener(this);
        messagesEvent = new MessagesEvent(s);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv1 :
                Toast.makeText(this,"Click",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onClick: ");
                //onMessageEvent(messagesEvent);
//                EventBus.getDefault().post(new MessagesEvent("Hello_world!"));
//                new Runnable(){
//                    @Override
//                    public void run() {
//                        try {
//                            SystemClock.sleep(5000);
//                            EventBus.getDefault().post(new MessagesEvent("Hello_World  !!"));
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }.run();
//                new RunTest().start();
                startActivity(new Intent(this,SecondActivity.class));
                break;
        }
    }

    class RunTest extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                Log.d(TAG, "run: start");
                SystemClock.sleep(5000);
                EventBus.getDefault().post(new MessagesEvent("RunTest run"));
                Log.d(TAG, "run: end");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEvent(MessagesEvent messagesEvent){
        Log.d(TAG, "onMessageEvent: " + messagesEvent.message);
    }

    @Subscribe
    public void hanlderMessageElse(){

    }
}
