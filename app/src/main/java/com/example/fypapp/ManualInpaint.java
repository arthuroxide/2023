package com.example.fypapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TimingLogger;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.fypapp.PaintMask;
import static com.example.fypapp.PaintMask.brushColor;
import static com.example.fypapp.PaintMask.pathList;
import static com.example.fypapp.PaintMask.colorList;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManualInpaint extends AppCompatActivity {
    PaintMask imageview;
    Uri imageUri;
    ImageView resultView;
    AlertDialog.Builder builder;
    Bitmap original;
    Bitmap mask;
    Bitmap resultBitmap;
    public static Paint paint = new Paint();
    public static Path path = new Path();
    private static final int PICK_IMAGE = 100;
    Drawable originstream;
    Mat origMat, maskMat, destMat;
    private ProgressBar progressBar;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    origMat =new Mat();
                    maskMat =new Mat();
                    destMat = new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_inpaint);

        imageview = findViewById(R.id.imagecanvas);
        builder = new AlertDialog.Builder(this);
        Intent getImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryActivityResultLauncher.launch(getImage);

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
        cV.put(MediaStore.Images.Media.DISPLAY_NAME, uniqueString + ".jpg");
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
                                original = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor());
                                parcelFileDescriptor.close();
                                originstream= Drawable.createFromStream(inputStream, imageUri.toString());
                                imageview.setBackground(originstream);

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

    public void backToMainActivity(View button) {
        finish();
    }

    public void eraser(View view) {
        pathList.clear();
        colorList.clear();
        path.reset();
    }

    public void pencil(View view) {
        paint.setColor(Color.WHITE);
        brushColor = paint.getColor();
        path = new Path();
    }
    /**
    private void calculateAndLaunch(){
        if((mask.getHeight() !=  original.getHeight())|| (mask.getWidth() != original.getWidth())){
            Log.w("Warning:", "mask wxh: "+mask.getWidth()+"x"+mask.getHeight()+"   original wxh:"+original.getWidth()+"x"+original.getHeight());
        }else{
            Log.w("Warning:", "mask wxh: "+mask.getWidth()+"x"+mask.getHeight()+"   original wxh:"+original.getWidth()+"x"+original.getHeight());
            Log.i("continuing calculation:","wew");
        }
        Utils.bitmapToMat(original, origMat);
        Utils.bitmapToMat(mask, maskMat);

        Imgproc.cvtColor(origMat, origMat, Imgproc.COLOR_RGBA2RGB);//cha
        Imgproc.cvtColor(origMat, origMat, Imgproc.COLOR_RGB2XYZ);//cha
        Imgproc.cvtColor(maskMat, maskMat, Imgproc.COLOR_RGBA2RGB);//cha
        Imgproc.cvtColor(maskMat, maskMat, Imgproc.COLOR_RGB2GRAY);//cha

        Photo.inpaint(origMat,maskMat,destMat, 30, Photo.INPAINT_TELEA);
        if(destMat.empty()){
            Log.e("Error:", "problem with inpaint");
        }
        resultBitmap= Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        Imgproc.cvtColor(destMat,destMat,Imgproc.COLOR_XYZ2RGB);
        Utils.matToBitmap(destMat, resultBitmap);

        createResultLayout();

    }

    private void createResultLayout(){
        setContentView(R.layout.activity_result);
        resultView= findViewById(R.id.result);
        resultView.setImageBitmap(resultBitmap);
    }*/

    private void calculateAndLaunch2(){

        setContentView(R.layout.activity_result);
        progressBar= findViewById(R.id.progress_bar);
        resultView= findViewById(R.id.result);
        ExecutorService calculateInpaint = Executors.newSingleThreadExecutor();
        calculateInpaint.execute(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });




                Utils.bitmapToMat(original, origMat);
                Utils.bitmapToMat(mask, maskMat);

                Imgproc.cvtColor(origMat, origMat, Imgproc.COLOR_RGBA2RGB);//cha
                Imgproc.cvtColor(origMat, origMat, Imgproc.COLOR_RGB2XYZ);//cha
                Imgproc.cvtColor(maskMat, maskMat, Imgproc.COLOR_RGBA2RGB);//cha
                Imgproc.cvtColor(maskMat, maskMat, Imgproc.COLOR_RGB2GRAY);//cha

                Photo.inpaint(origMat,maskMat,destMat, 30, Photo.INPAINT_TELEA);

                resultBitmap= Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
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


    public Bitmap getScaledOrig() {
        pathList.clear();
        colorList.clear();
        path.reset();
        imageview.setBackground(originstream);
        return imageview.getNoBlackMask();
    }
    public void saveAs(View view) {


        mask= imageview.getMask();
        original= getScaledOrig();

        builder.setTitle("Alert!")
                .setMessage("Confirm all the inputted mask is correct??")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Bitmap emptyBitmap = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), mask.getConfig());
                        if (mask.sameAs(emptyBitmap) || mask == null) {
                            Log.e("Bitmap empty", "problem with view");
                        } else {
                            Log.i("ERERasdsa", "saveAs() called with: view = [" + view + "]");
                        }

                        if (original.sameAs(emptyBitmap) || original == null) {
                            Log.e("Original empty", "problem with view");
                        } else {
                            Log.i("bitmapok", "saveAs() called with: view = [" + view + "]");
                        }


                        try{
                            calculateAndLaunch2();
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();



    }
}