package com.example.fypapp;


import androidx.camera.core.ImageCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG="MainActivity";

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    ImageView imageview;

    private ImageCapture imageCapture;
    private Button bCapture;
    private File mImageFile;
    int Previewbool=0;
    private Mat mRgba;
    private Mat mGray;

    Bitmap resultBitmap;
    private CameraBridgeViewBase mOpenCvCameraView;
    private objectDetectorClass objectDetectorClass;
    private BaseLoaderCallback mLoaderCallback =new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface
                        .SUCCESS:{
                    Log.i(TAG,"OpenCv Is loaded");
                    mOpenCvCameraView.enableView();
                }
                default:
                {
                    super.onManagerConnected(status);

                }
                break;
            }
        }
    };

    public CameraActivity(){
        Log.i(TAG,"Instantiated new "+this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CameraActivity","Entered Camera Activity bp-1");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Log.d("CameraActivity","Entered Camera Activity bpi");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.d("CameraActivity","Entered Camera Activity bp0");
        int MY_PERMISSIONS_REQUEST_CAMERA=100;

        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            Log.d("CameraActivity","Permission first denied" );
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
        Log.d("CameraActivity","Entered Camera Activity bp1");
        setContentView(R.layout.activity_camera);

        mOpenCvCameraView=(CameraBridgeViewBase) findViewById(R.id.frame_Surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCameraPermissionGranted();
        mOpenCvCameraView.setCvCameraViewListener(this);
        // previewView = findViewById(R.id.previewView);
        Log.d("CameraActivity","Entered Camera Activity bp2");
        try{

            objectDetectorClass=new objectDetectorClass(getAssets(),"ssd_mobilenet.tflite", "labelmap2.txt",300);
            Log.d("MainActivity","Model is successfully loaded");
        }
        catch (IOException e){
            Log.d("MainActivity","Getting some error");
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            //if load success
            Log.d(TAG,"Opencv initialization is done");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            //if not loaded
            Log.d(TAG,"Opencv is not loaded. try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,mLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }

    }

    public void onCameraViewStarted(int width ,int height){
        mRgba =new Mat(width,height, CvType.CV_8UC4);
        mGray =new Mat(width,height,CvType.CV_8UC1);
        Log.i("MyActivity", "the mRGBA width is " + mRgba.width());
        Log.i("MyActivity", "the MRGBA height is " + mRgba.height());

        Log.i("MyActivity", "the width is " + width);
        Log.i("MyActivity", "the height is " + height);
        Log.i("MyActivity", "the mOpenCvCameraView width is " + mOpenCvCameraView.getWidth());
        Log.i("MyActivity", "the mOpenCvCameraView height is " + mOpenCvCameraView.getHeight());
    }
    public void onCameraViewStopped(){
        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        mRgba=inputFrame.rgba();
        mGray=inputFrame.gray();

        if (Previewbool==1)
        {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Bitmap abitmap = Bitmap.createBitmap(1280, 960, Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(mRgba, abitmap);
                    MediaStore.Images.Media.insertImage(getContentResolver(), abitmap, "ShitImage", "This image is a real shit");

                    Log.e("MyActivity", "inputsize " + mOpenCvCameraView.getHeight());

                }
            });
        }
        Mat out=new Mat();
        out=objectDetectorClass.recognizeImage(mRgba);
        Previewbool=0;
        return out;
    }

    public void takePicture(View view) {
        Previewbool=1;
    }

    public void takePicture2(View view) {
        ExecutorService savePicture = Executors.newSingleThreadExecutor();
        savePicture.execute(new Runnable() {
            @Override
            public void run() {
                resultBitmap = Bitmap.createBitmap(mRgba.width(), mRgba.height(), Bitmap.Config.ARGB_8888);
                Log.i("MyAcitivitu", "the result Bitmap size is "+ resultBitmap.getWidth()+"x"+resultBitmap.getHeight());

                Utils.matToBitmap(mRgba, resultBitmap);
                Bitmap emptyBitmap = Bitmap.createBitmap(resultBitmap.getWidth(), resultBitmap.getHeight(), resultBitmap.getConfig());
                if (resultBitmap.sameAs(emptyBitmap) || resultBitmap == null) {

                            Log.e("Result empty", "problem with resukt");


                } else {

                            Log.e("Result empty", "problem with resukt");

                }
                String uniqueString = UUID.randomUUID().toString();
                Uri imgC;
                ContentResolver resolver = getContentResolver();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    imgC = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                } else {
                    imgC = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
                ContentValues cV = new ContentValues();
                cV.put(MediaStore.Images.Media.DISPLAY_NAME, uniqueString+"enhanced_camera" + ".jpg");
                cV.put(MediaStore.Images.Media.MIME_TYPE, "images/jpeg");
                Uri rImageUri = resolver.insert(imgC, cV);
                try {
                    OutputStream outputStream = resolver.openOutputStream(Objects.requireNonNull(rImageUri));
                    resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    Objects.requireNonNull(outputStream);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Successful storage of Image:"+ uniqueString+"enhanced_camera"+".jpg",Toast.LENGTH_SHORT ).show();
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
    }


    public void saveImage(Bitmap bitmap) {
        OutputStream fos = null;
        try {
            mImageFile = new File(getExternalFilesDir(null), "image.jpg");
            fos = new FileOutputStream(mImageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            MediaStore.Images.Media.insertImage(getContentResolver(), mImageFile.getAbsolutePath(), mImageFile.getName(), mImageFile.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}