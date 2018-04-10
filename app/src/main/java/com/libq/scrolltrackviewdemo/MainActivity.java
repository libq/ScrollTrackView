package com.libq.scrolltrackviewdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.libq.scrolltrackview.ScrollTrackView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ScrollTrackView stv = (ScrollTrackView) findViewById(R.id.stv);
        Button btn = (Button)findViewById(R.id.btnRestart);
        Button btnPause = (Button)findViewById(R.id.btnPause);
        Button btnStop = (Button)findViewById(R.id.btnStop);
        Button btnStart =  (Button)findViewById(R.id.btnStart);

        final TextView tv = (TextView) findViewById(R.id.tv);
        //1.每个Track小块的数据,不设置也可以，有默认
        float[] template = {0.9f,0.6f,0.7f,0.5f,0.8f,0.4f,0.5f,0.2f,0.6f,0.8f,0.8f};

        stv.setTrackTemplateData(template);
        stv.setDuration(20000); // 音频时间
        stv.setCutDuration(10000);//屏幕左边跑到右边持续的时间
        stv.setTrackFragmentCount(10);//1 中是一个片段，这个参数表示重复1中片段画10次
        stv.setLoopRun(true);//设置是否循环跑进度
        stv.setOnProgressRunListener(new ScrollTrackView.OnProgressRunListener() {
            @Override
            public void onTrackStart(int ms) {

            }

            @Override
            public void onTrackStartTimeChange(int ms) {
                tv.setText("从 "+ms*1f/1000f+" 秒开始");
            }

            @Override
            public void onTrackEnd() {

            }
        });


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stv.restartMove();
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stv.stopMove();
            }
        });
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               stv.pauseMove();
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stv.startMove();
            }
        });
    }

}
