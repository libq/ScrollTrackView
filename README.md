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
	        compile 'com.github.libq:ScrollTrackView:1.0.1'
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
	app:auto_run="true"
        />
```
  

