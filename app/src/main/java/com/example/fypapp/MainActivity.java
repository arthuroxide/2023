package com.example.fypapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.content.Intent;
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


    public void toAutoInpaintActivity(View button) {
        //start QR activity
        Intent toAutoInpaintPage= new Intent(this, AutoInpaint.class);
        startActivity(toAutoInpaintPage);}

    public void toManualInpaintActivity(View button) {
        //start QR activity
        Intent toManualInpaintPage= new Intent(this, ManualInpaint.class);
        startActivity(toManualInpaintPage);}
    public void toAlbumActivity(View button) {
        //start QR activity
        Intent toAlbumPage= new Intent(this, AlbumActivity.class);
        startActivity(toAlbumPage);}
    public void toCameraActivity(View button) {
        //start QR activity
        Intent toCameraPage= new Intent(this, CameraBaseActivity.class);
        startActivity(toCameraPage);}


    public void toInstructionActivity(View button) {
        //start QR activity
        Intent toInstruction= new Intent(this, Instruction.class);
        startActivity(toInstruction);}
    /**
    public void toGithubActivity(View button) {
        //start QR activity
        Intent toScannerPage= new Intent(this, AutoInpaint.class);
        startActivity(toScannerPage);}*/




    private void initiateDismissTimer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isReady= true;
            }
        }, 2000);
    }
}