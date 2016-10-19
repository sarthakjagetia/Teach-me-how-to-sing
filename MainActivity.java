package com.example.firstproject_beta;

        import android.media.MediaPlayer;
        import android.os.Bundle;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer = new MediaPlayer();//MediaPlayer Object
    private TextView hint;//claim message text
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //get functions for each button
        final Button button1=(Button)findViewById(R.id.button1);//play1
        final Button button2=(Button)findViewById(R.id.button2);//play2
        final Button button3=(Button)findViewById(R.id.button3);//play3
        final Button button4=(Button)findViewById(R.id.button4);//stop
        hint=(TextView)findViewById(R.id.textView);


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play1();
                button1.setEnabled(false);
                button2.setEnabled(false);
                button3.setEnabled(false);
                button4.setEnabled(true);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play2();
                button1.setEnabled(false);
                button2.setEnabled(false);
                button3.setEnabled(false);
                button4.setEnabled(true);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play3();
                button1.setEnabled(false);
                button2.setEnabled(false);
                button3.setEnabled(false);
                button4.setEnabled(true);
            }
        });
        //对停止按钮添加事件监听器
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                hint.setText("Stop...");
                button1.setEnabled(true);
                button2.setEnabled(true);
                button3.setEnabled(true);
                button4.setEnabled(false);
            }
        });
    }
    protected void onDestroy() {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        mediaPlayer.release();
        super.onDestroy();
    }

    //play music
    private void play1(){
        try{
            mediaPlayer.reset();//reset music
            mediaPlayer=MediaPlayer.create(this,R.raw.song1);
            mediaPlayer.start();//play music
            hint.setText("Music is starting");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("err",e.getMessage());
        }
        return ;
    }
    private void play2(){
        try{
            mediaPlayer.reset();//reset music
            mediaPlayer=MediaPlayer.create(this,R.raw.song2);
            mediaPlayer.start();//play music
            hint.setText("Music is starting");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("err",e.getMessage());
        }
        return ;
    }
    private void play3(){
        try{
            mediaPlayer.reset();//reset music
            mediaPlayer=MediaPlayer.create(this,R.raw.song3);
            mediaPlayer.start();//play music
            hint.setText("Music is starting");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("err",e.getMessage());
        }
        return ;
    }

}