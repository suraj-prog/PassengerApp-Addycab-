package com.example.cabs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.tomer.fadingtextview.FadingTextView;

import java.util.concurrent.TimeUnit;

public class Splash extends AppCompatActivity {
private FadingTextView fadingTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        fadingTextView = findViewById(R.id.splash_text);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fadingTextView.setTimeout(300, TimeUnit.MILLISECONDS);
             startActivity(new Intent(Splash.this,MainActivity.class));
             finish();
            }
        },5000);
    }
}
