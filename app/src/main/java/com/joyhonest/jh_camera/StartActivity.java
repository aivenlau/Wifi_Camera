package com.joyhonest.jh_camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.joyhonest.wifination.wifination;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;


//import com.joyhonest.wifination.wifination;

//import org.simple.eventbus.EventBus;


public class                StartActivity extends AppCompatActivity {
    private  String TAG = "Wifi_Camera";


    ImageView  DispImageView;

    boolean  bPlaying = false;

    Button   button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        button = findViewById(R.id.button1);
        DispImageView = findViewById(R.id.DispImageView);
        button.setText("Play");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bPlaying)
                {
                    wifination.naSetRevBmp(true);
                    wifination.naInit("");
                    wifination.naSetAdjGsensorData(true);
                    wifination.naSetEnableRotate(true);
                    wifination.naSetCircul(true);
                    wifination.naSetSensor(true);
                    wifination.naSetsquare(true);
                }
                else
                {
                    wifination.naStopRecord_All();
                    wifination.naStop();
                }
                bPlaying = !bPlaying;
                if(bPlaying)
                {
                    button.setText("Stop");
                }
                else
                {
                    button.setText("Play");
                }
            //    startActivity(new Intent(StartActivity.this, PlayActivity.class));
//                byte[]data = new byte[10];
//                data[0]='J';
//                data[1]='H';
//                data[2]='C';
//                data[3]='M';
//                data[4]='D';
//                data[5]=0x20;
//                data[6]=0x01;
//                data[7]=0x00;
//
//                wifination.naSentUdpData("192.168.29.1",20000,data,8);
            }
        });

        //wifination.naSetDebug(true);



        EventBus.getDefault().register(this);
     //   EventBus.getDefault().register(this);
    }


    private Handler openHandler = new Handler();
    private Handler openHandler1 = new Handler();

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("abc","onResume");
        openHandler.removeCallbacksAndMessages(null);
        openHandler.post(new Runnable() {
            @Override
            public void run() {
                wifination.naStop();
                openHandler1.removeCallbacksAndMessages(null);
                openHandler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("abc","OpenCamera");
                        wifination.naSetRevBmp(true);
                        wifination.naInit("");
                        wifination.naSetAdjGsensorData(true);
                        wifination.naSetEnableRotate(true);
                        wifination.naSetCircul(true);
                        wifination.naSetSensor(true);
                        wifination.naSetsquare(true);
                    }
                },150);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        wifination.naStop();
        Log.e("abc","onPause");

    }

    @Subscriber(tag = "GetDataFromWifi")
    private  void GetDataFromWifi(byte[] cmd)
    {
        if (cmd != null && cmd.length >= 3) {
                if(cmd[0]==0x20 && cmd[1]==0x01)
                {
                    Log.v("ABC","Get Resolution = "+cmd[2]);
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wifination.naStop();
        EventBus.getDefault().unregister(this);
    }


    @Subscriber(tag = "ReviceBMP")
    private void ReviceBMP(Bitmap bmp) {

        DispImageView.setImageBitmap(bmp);
    }

    //    @Subscriber(tag="Key_Pressed")
//    private  void key_Press(Integer nKeyA)
//    {
//        int nKye = nKeyA.intValue();
//        Log.e(TAG,"Key = "+nKye);
//    }
//
//    @Subscriber(tag = "SDStatus_Changed")
//    private void  _OnStatusChanged(int nStatus)
//    {
//        Log.e(TAG,"Status = "+nStatus);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        EventBus.getDefault().unregister(this);
//    }
}
