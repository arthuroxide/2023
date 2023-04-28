package com.example.fypapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;

import java.util.List;

public class CameraBaseActivity extends AppCompatActivity {

       ImageView imageView;
       TextureView textureView;
       CameraManager cameraManager;
       Handler handler;
       CameraDevice cameraDevice;
       Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        get_camera_permission();
        HandlerThread handlerThread =new HandlerThread("vT");
        handler= new Handler(handlerThread.getLooper());

        handlerThread.start();
        imageView= findViewById(R.id.imageView);
        textureView= findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                activate_camera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
             bitmap=textureView.getBitmap();
            }

    });
        cameraManager= (CameraManager) getSystemService(Context.CAMERA_SERVICE);


    }
@SuppressLint("MissingPermission")
    private void activate_camera() {
        try{
        cameraManager.openCamera(cameraManager.getCameraIdList()[0],new CameraDevice.StateCallback(){

            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                try{
                cameraDevice =camera;
                SurfaceTexture surfaceTexture =textureView.getSurfaceTexture();
                Surface surface= new Surface(surfaceTexture);
                CaptureRequest.Builder captureRequest=  cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                captureRequest.addTarget(surface);
                cameraDevice.createCaptureSession(List.of(surface), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        try{
                        session.setRepeatingRequest(captureRequest.build(), null, null);
                        }catch(Exception e){
                            Log.e("REPEATINGREQUEST FAIL", "try working the shaft");
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                    }
                },handler);
                }catch (Exception e){
                    Log.e("error on open camera", "onOpened");
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {

            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {

            }
        }, handler);


        } catch (CameraAccessException e) {
            Log.e("cannot open camera", "failure to activate camera function");
        }
    }

    private void get_camera_permission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] { Manifest.permission.CAMERA }, 101);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]!= PackageManager.PERMISSION_GRANTED){
            get_camera_permission();
        }
    }
}