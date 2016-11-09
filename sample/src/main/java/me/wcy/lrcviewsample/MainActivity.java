package me.wcy.lrcviewsample;

import android.content.Intent;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;

import me.wcy.lrcview.LrcView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button2 = (Button) findViewById(R.id.button2);
        ButtonListenerButton2 buttonListenerButton2 = new ButtonListenerButton2();
        button2.setOnClickListener(buttonListenerButton2);
    }
    class ButtonListenerButton2 implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, Main2Activity.class);
            startActivity(intent);
        }
    }
}

//i made a change