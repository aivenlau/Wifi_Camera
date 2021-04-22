package com.joyhonest.jh_camera;

import android.media.Image;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.joyhonest.wifination.wifination;

public class PlayActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView DispImageView;
    private Button  btn_Play;
    private Button  btn_Seek;
    private Button  btn_Pause;
    private Button  btn_Stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        DispImageView = findViewById(R.id.DispImageView);
        btn_Play = findViewById(R.id.btn_Play);
        btn_Seek = findViewById(R.id.btn_Seek);
        btn_Pause = findViewById(R.id.btn_Pause);
        btn_Stop = findViewById(R.id.btn_Stop);
        btn_Play.setOnClickListener(this);
        btn_Seek.setOnClickListener(this);
        btn_Pause.setOnClickListener(this);
        btn_Stop.setOnClickListener(this);


//        wifination.naStop();
//        wifination.naSetRevBmp(false);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                wifination.naPlayFlie("/storage/emulated/0/1/MOVI0209_aaa.mp4");
//
//            }
//        },200);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btn_Play:
                break;
            case R.id.btn_Seek:
                break;
            case R.id.btn_Pause:
                break;
            case R.id.btn_Stop:
                break;
        }

    }
}