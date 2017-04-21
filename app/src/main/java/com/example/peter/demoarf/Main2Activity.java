package com.example.peter.demoarf;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

public class Main2Activity extends AppCompatActivity {
    private MediaPlayer mp;     //声明MediaPlayer对象
    private SurfaceView sv; //声明SurfaceView对象
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mp=new MediaPlayer();       //实例化MediaPlayer对象
        sv=(SurfaceView)findViewById(R.id.surfaceView1);    //获取布局管理器中添加的SurfaceView组件
        Button play=(Button)findViewById(R.id.play);    //获取“播放”按钮
        final Button pause=(Button)findViewById(R.id.pause);    //获取“暂停/继续”按钮
        Button stop=(Button)findViewById(R.id.stop);        //获取“停止”按钮
        //为“播放”按钮添加单击事件监听器
        play.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mp.reset();     //重置MediaPlayer对象
                try {
                    mp.setDataSource("http://demoappdownload.oss-cn-hangzhou.aliyuncs.com/upload/video/20170410/1491813702321097394.mp4");    //设置要播放的视频
//                    mp.setDataSource("http://demoappdownload.oss-cn-hangzhou.aliyuncs.com/upload/demo.arf");
                    mp.setDisplay(sv.getHolder());  //设置将视频画面输出到SurfaceView
                    mp.prepare();   //预加载视频
                    mp.start(); //开始播放
//                    sv.setBackgroundResource(R.mipmap.ic_launcher_round);    //改变SurfaceView的背景图片
                    pause.setText("暂停");
                    pause.setEnabled(true); //设置“暂停”按钮可用
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        });
        //为“停止”按钮添加单击事件监听器
        stop.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if(mp.isPlaying()){
                    mp.stop();      //停止播放
                    sv.setBackgroundResource(R.mipmap.ic_launcher); //改变SurfaceView的背景图片
                    pause.setEnabled(false);    //设置“暂停”按钮不可用
                }
            }


        });
        //为“暂停”按钮添加单击事件监听器
        pause.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(mp.isPlaying()){
                    mp.pause();     //暂停视频的播放
                    ((Button)v).setText("继续");
                }else{
                    mp.start();     //继续视频的播放
                    ((Button)v).setText("暂停");
                }
            }


        });
        //为MediaPlayer对象添加完成事件监听器
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                sv.setBackgroundResource(R.mipmap.ic_launcher); //改变SurfaceView的背景图片
                Toast.makeText(Main2Activity.this, "视频播放完毕！", Toast.LENGTH_SHORT).show();
            }


        });

    }
    @Override
    protected void onDestroy() {
        if(mp.isPlaying()){
            mp.stop();  //停止播放视频
        }
        mp.release();   //释放资源
        super.onDestroy();
    }
}
