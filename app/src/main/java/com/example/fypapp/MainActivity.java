package com.example.fypapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;

public class MainActivity extends AppCompatActivity {
    boolean isReady= false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        View content= findViewById(android.R.id.content);

        content.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){
            @Override
            public boolean onPreDraw() {
                if(isReady){
                    content.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                initiateDismissTimer();
                return false;
            }
        });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void initiateDismissTimer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isReady= true;
            }
        }, 2000);
    }
}