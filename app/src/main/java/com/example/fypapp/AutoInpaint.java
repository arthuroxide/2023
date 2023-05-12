package com.example.fypapp;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.widget.Toast;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AutoInpaint extends AppCompatActivity {


    Spinner spinner;
    ArrayList<String> options;
    //
    private objectDetectorClass objectDetectorClass;

    private ArrayList<Pair<Point, Point>> boundingBoxPoints;
    private ArrayList<String> boundingBoxClasses;

    boolean[] checkedItems;


    public static float MINIMUM_CONFIDENCE_TF_OD_API= 0.5f;

    private BaseLoaderCallback mLoaderCallback =new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface
                        .SUCCESS:{
                    Log.i(TAG,"OpenCv Is loaded");

                }
                default:
                {
                    super.onManagerConnected(status);

                }
                break;
            }
        }
    };
//


    Uri imageUri;

    Bitmap inputPic;
    Drawable inputPicStream;

    ImageView imageView;

    boolean detectedImage= false;

    private ProgressBar progressBar;
    ImageView resultView;

    Mat destMat;

    Bitmap resultBitmap;



    // use default spinner item to show options in spinner


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_inpaint);
        imageView= findViewById(R.id.inputPic);

        Intent getImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryActivityResultLauncher.launch(getImage);

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
    public String getRealPathFromURI(Uri contentUri) {
        // can post image
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri,
                filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        return picturePath;
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result != null) {
                        // There are no request codes
                        Intent data = result.getData();
                        imageUri = data.getData();
                        //BitmapFactory.Options options=new BitmapFactory.Options();
                        File file = new File(getRealPathFromURI(imageUri));
                        if (file.exists()) {
                            //BitmapFactory.decodeFile(getRealPathFromURI(imageUri), options);
                            try {
                                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                                ParcelFileDescriptor parcelFileDescriptor= getContentResolver().openFileDescriptor(imageUri,"r");
                                inputPic = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor());
                                parcelFileDescriptor.close();
                                inputPicStream= Drawable.createFromStream(inputStream, imageUri.toString());
                                imageView.setImageBitmap(inputPic);


                            } catch (FileNotFoundException e) {
                                Log.e("File not Found", "ewewewe");
                            }catch (IOException e) {
                                e.printStackTrace();
                            }
                            //imageview.setBackground(Drawable.createFromPath(file.getAbsolutePath()));
                            Log.i("File Path:", "ok");
                        } else {
                            Log.e("File not Found", "ad");
                        }

                    }
                }
            });

    public void backToMainActivity(View button){
        finish();
    }
    public void proceedImage(View button){
        //TODO process the image with the object selected
        if(!detectedImage){
            Toast.makeText(getApplicationContext(),"Cannot detect any object in the image.",Toast.LENGTH_SHORT ).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_event_note_24);
        checkedItems= new boolean[boundingBoxClasses.size()];
        String[] boundingBoxClassArray= boundingBoxClasses.toArray(new String[boundingBoxClasses.size()]);
        builder.setMultiChoiceItems(boundingBoxClassArray, checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
            String currentItem = boundingBoxClasses.get(which);
        });
        builder.setTitle("Choose Bounding Boxes to be removed");

        builder.setCancelable(false);
        builder.setPositiveButton("Done", (dialog, which) -> {
            Toast.makeText(getApplicationContext(),"Object Chosen and now computing",Toast.LENGTH_SHORT ).show();
            calculateAndLaunch();
        });

        // handle the negative button of the alert dialog
        builder.setNegativeButton("CANCEL", (dialog, which) -> {});

        // handle the neutral button of the dialog to clear the selected items boolean checkedItem
        builder.setNeutralButton("CLEAR ALL", (dialog, which) -> {
            Arrays.fill(checkedItems, false);
        });

        builder.create();


        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }
    public void calculateAndLaunch(){

        setContentView(R.layout.activity_result_auto);
        progressBar= findViewById(R.id.progress_bar_auto);
        resultView= findViewById(R.id.result_auto);

        ExecutorService calculateMaskInpaint = Executors.newSingleThreadExecutor();
        calculateMaskInpaint.execute(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });

                Mat imageMat= new Mat(inputPic.getHeight(), inputPic.getWidth(), CvType.CV_8UC4);
                Utils.bitmapToMat(inputPic,imageMat);
                Mat imageMask= new Mat(inputPic.getHeight(), inputPic.getWidth(), CvType.CV_8UC4, new Scalar(0,0,0));
                for (int i = 0; i < boundingBoxPoints.size(); i++) {
                    if(checkedItems[i]){
                        Imgproc.rectangle(imageMask, boundingBoxPoints.get(i).first,boundingBoxPoints.get(i).second, new Scalar(255,255,255), -1);
                    }
                }

                Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_RGBA2RGB);//cha
                Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_RGB2XYZ);//cha
                Imgproc.cvtColor(imageMask, imageMask, Imgproc.COLOR_RGBA2RGB);//cha
                Imgproc.cvtColor(imageMask, imageMask, Imgproc.COLOR_RGB2GRAY);//cha
                destMat= new Mat();

                Photo.inpaint(imageMat,imageMask,destMat, 30, Photo.INPAINT_TELEA);

                resultBitmap= Bitmap.createBitmap(inputPic.getWidth(), inputPic.getHeight(), Bitmap.Config.ARGB_8888);
                Imgproc.cvtColor(destMat,destMat,Imgproc.COLOR_XYZ2RGB);
                Utils.matToBitmap(destMat, resultBitmap);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        if(resultBitmap != null){
                            resultView.setImageBitmap(resultBitmap);
                        }
                    }
                });

            }
        });
    }
    public void saveInpaint(View button){
        Bitmap emptyBitmap = Bitmap.createBitmap(resultBitmap.getWidth(), resultBitmap.getHeight(), resultBitmap.getConfig());
        if (resultBitmap.sameAs(emptyBitmap) || resultBitmap == null) {
            Log.e("Result empty", "problem with resukt");
        } else {
            Log.i("finallydopne", "wewe");
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
        cV.put(MediaStore.Images.Media.DISPLAY_NAME, uniqueString+"_auto_inpaint" + ".jpg");
        cV.put(MediaStore.Images.Media.MIME_TYPE, "images/jpeg");
        Uri rImageUri = resolver.insert(imgC, cV);
        try {
            OutputStream outputStream = resolver.openOutputStream(Objects.requireNonNull(rImageUri));
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Objects.requireNonNull(outputStream);
            Toast.makeText(getApplicationContext(),"Successful storage of Image:"+ uniqueString+".jpg",Toast.LENGTH_SHORT ).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void detectImage(View button){
        // TODO detect image using YOLOv4
       // Handler handler= new Handler();
        Mat imageMat= new Mat(inputPic.getHeight(), inputPic.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(inputPic,imageMat);

        imageMat= objectDetectorClass.recognizePhoto(imageMat);
        boundingBoxPoints= objectDetectorClass.getPoints();
        boundingBoxClasses= objectDetectorClass.getPointString();


        Bitmap bitmapFinished= null;
        bitmapFinished= Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(imageMat, bitmapFinished);
        imageView.setImageBitmap(bitmapFinished);

        if (boundingBoxClasses.isEmpty() || boundingBoxPoints.isEmpty()){
            Toast.makeText(getApplicationContext(),"No Object Detected",Toast.LENGTH_SHORT ).show();
        }else{
            Toast.makeText(getApplicationContext(),"Object detected: Bounding Box Array Length: "+ boundingBoxPoints.size()+ "Bounding Box Class Array Length: "+ boundingBoxClasses.size(),Toast.LENGTH_SHORT ).show();
            detectedImage= true;
        }




    }










}