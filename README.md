# ScrollTrackView
类似抖音效视频音频截取进度条

![demo](https://github.com/libq/ScrollTrackView/blob/master/demo.png)


# 使用
## project 下的 build.gradle ：
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
## module 下的 build.gradle ：
    dependencies {
	        compile 'com.github.libq:ScrollTrackView:1.0.4'
	}
	
## xml：
 ```
 <com.libq.scrolltrackview.ScrollTrackView
        android:id="@+id/stv"
        android:layout_margin="10dp"
        android:paddingLeft="10dp"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        app:foreground_color="@color/colorAccent"
        app:background_color="@color/colorPrimary"
        app:space_size="5dp"
        app:track_item_width="1dp"
        app:track_fragment_count="10"
        app:loop_run="false"
        app:cut_duration="10000"
        app:auto_run="false"
        />
```
## java：
```
        float[] template = {0.9f,0.6f,0.7f,0.5f,0.8f,0.4f,0.5f,0.2f,0.6f,0.8f,0.8f};

        stv.setTrackTemplateData(template);
        stv.setDuration(20000); // 音频时间
        stv.setCutDuration(10000);//屏幕左边跑到右边持续的时间
        stv.setTrackFragmentCount(10);//1 中是一个片段，这个参数表示重复1中片段画10次
        stv.setLoopRun(false);//设置是否循环跑进度
	stv.setOnProgressRunListener(new ScrollTrackView.OnProgressRunListener() {
            @Override
            public void onTrackStart(int ms) {

            }

            @Override
            public void onTrackStartTimeChange(int ms) {
               
            }

            @Override
            public void onTrackEnd() {

            }
        });
```
  

