package com.example.peter.demoarf;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = (TextView) findViewById(R.id.tv_11);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,Main2Activity.class);
                startActivity(i);
//                MediaPlayer mediaPlayer=new MediaPlayer();
//                try {
//                    MediaPlayer mediaPlayer=MediaPlayer.create(MainActivity.this, R.raw.takeabow);
//                    mediaPlayer.setDataSource(R.raw.demo);
//                    mediaPlayer.prepare();
//                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
//                        @Override
//                        public void onPrepared(MediaPlayer mediaPlayer) {
//                            mediaPlayer.start();
//                        }
//                    });
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        });
    }
}
