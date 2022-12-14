package com.example.medi;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gauravk.audiovisualizer.visualizer.WaveVisualizer;

import java.util.ArrayList;


public class playerActivity extends AppCompatActivity {
    Button playbtn,nextbtn,prevbtn,fastforwardbtn,rewindbtn;
    TextView txtsname,sstart,sstop;
    SeekBar seekmusic;
    WaveVisualizer visualizer;
    String sname;
    ImageView imageView;
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<Integer> mySongs;
    Thread updateseekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(visualizer!=null){
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        playbtn = findViewById(R.id.playbtn);
        prevbtn = findViewById(R.id.previousbtn);
        nextbtn = findViewById(R.id.nextbtn);
        fastforwardbtn = findViewById(R.id.fastforwardbtn);
        rewindbtn = findViewById(R.id.rewindbtn);
        txtsname = findViewById(R.id.txtsn);
        sstart = findViewById(R.id.txtsstart);
        sstop = findViewById(R.id.txtsstop);
        seekmusic = findViewById(R.id.seekbar);
        visualizer = findViewById(R.id.blast);
        imageView = findViewById(R.id.imageview);
        if(mediaPlayer!=null){
            mediaPlayer.stop(); //start()
            mediaPlayer.release();
        }
        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList)bundle.getIntegerArrayList("songs");
        position = bundle.getInt("pos",0);
        sname=getResources().getResourceName(mySongs.get(position)).replace(".mp3","").replace("com.example.medi:raw/","");
        txtsname.setText(sname);

        int idd = mySongs.get(position);
          mediaPlayer = MediaPlayer.create(getApplicationContext(),idd);
          mediaPlayer.start();

        updateseekbar=new Thread(){
            @Override
            public void run() {
                int totalDuration=mediaPlayer.getDuration();
                int currentPosition=0;
                while(currentPosition<totalDuration){
                    try{
                        sleep(500);
                        currentPosition=mediaPlayer.getCurrentPosition();
                        seekmusic.setProgress(currentPosition);
                    }
                    catch (InterruptedException|IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        seekmusic.setMax(mediaPlayer.getDuration());
        updateseekbar.start();
        seekmusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.purple_500), PorterDuff.Mode.MULTIPLY);
        seekmusic.getThumb().setColorFilter(getResources().getColor(R.color.purple_200), PorterDuff.Mode.SRC_IN);
        seekmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        sstop.setText(endTime);

        final Handler handler = new Handler();
        final int delay=1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                sstart.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        },delay);

        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    playbtn.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                }
                else{
                    playbtn.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }
        });

        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position=((position+1)%mySongs.size());
                int iddd = mySongs.get(position);
                mediaPlayer = MediaPlayer.create(getApplicationContext(),iddd);
                sname=getResources().getResourceName(mySongs.get(position)).replace(".mp3","").replace("com.example.medi:raw/","");
                txtsname.setText(sname);
                mediaPlayer.start();
                playbtn.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(imageView);
                int audiosessionId = mediaPlayer.getAudioSessionId();
                if(audiosessionId!=-1){
                    visualizer.setAudioSessionId(audiosessionId);
                }
            }
        });
        //next listner
         mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
             @Override
             public void onCompletion(MediaPlayer mediaPlayer) {
                 nextbtn.performClick();
             }
         });

         int audiosessionId = mediaPlayer.getAudioSessionId();
         if(audiosessionId!=-1){
             visualizer.setAudioSessionId(audiosessionId);
         }

        prevbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position=((position-1)<0?(mySongs.size()-1):(position-1));
                int iddd = mySongs.get(position);
                mediaPlayer = MediaPlayer.create(getApplicationContext(),iddd);
                sname=getResources().getResourceName(mySongs.get(position)).replace(".mp3","").replace("com.example.medi:raw/","");
                txtsname.setText(sname);
                mediaPlayer.start();
                playbtn.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(imageView);
                int audiosessionId = mediaPlayer.getAudioSessionId();
                if(audiosessionId!=-1){
                    visualizer.setAudioSessionId(audiosessionId);
                }
            }
        });

        fastforwardbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });

        rewindbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });
    }

    public void startAnimation(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView,"rotation",0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet= new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();

    }

    public String createTime(int duration){
        String time="";
        int min=duration/1000/60;
        int sec=duration/1000%60;
        time+=min+":";
        if(sec<10){
            time+="0";
        }
        time+=sec;
        return time;
    }
}