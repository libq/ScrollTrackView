package com.libq.scrolltrackviewdemo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.libq.scrolltrackview.ScrollTrackView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ScrollTrackView stv = (ScrollTrackView) findViewById(R.id.stv);

        final TextView tv = (TextView) findViewById(R.id.tv);
        //1.每个Track小块的数据,不设置也可以，有默认
        float[] template = {0.9f,0.6f,0.7f,0.5f,0.8f,0.4f,0.5f,0.2f,0.6f,0.8f,0.8f};

        stv.setTrackTemplateData(template);
        stv.setDuration(20000); // 音频时间
        stv.setTrackFragmentCount(10);//1 中是一个片段，这个参数标识重复1中片段个数
        stv.setOnProgressRunListener(new ScrollTrackView.OnProgressRunListener() {
            @Override
            public void onTrackRun() {
                //进度走到结尾，会重复走到这个方法，从头开始
            }

            @Override
            public void onTrackStartTimeChange(int ms) {
                tv.setText("从 "+ms*1f/1000f+" 开始");
            }
        });
    }

}
