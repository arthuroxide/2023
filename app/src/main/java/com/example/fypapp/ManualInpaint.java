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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.fypapp.PaintMask;
import static com.example.fypapp.PaintMask.brushColor;
import static com.example.fypapp.PaintMask.pathList;
import static com.example.fypapp.PaintMask.colorList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class ManualInpaint extends AppCompatActivity {
    PaintMask imageview;
    Button button;
    Uri imageUri;
    AlertDialog.Builder builder;

    public static Paint paint= new Paint();
    public static Path path= new Path();
    private static final int PICK_IMAGE= 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_inpaint);
        imageview= findViewById(R.id.imagecanvas);
        builder= new AlertDialog.Builder(this);
        Intent getImage= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryActivityResultLauncher.launch(getImage);
    }
    public String getRealPathFromURI(Uri contentUri) {
        // can post image
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri,
                filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        return  picturePath ;
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher=registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result!=null) {
                            // There are no request codes
                            Intent data = result.getData();
                            imageUri = data.getData();
                            //BitmapFactory.Options options=new BitmapFactory.Options();
                            File file= new File(getRealPathFromURI(imageUri));
                            if(file.exists()){
                                //BitmapFactory.decodeFile(getRealPathFromURI(imageUri), options);
                                try {
                                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                                    imageview.setBackground(Drawable.createFromStream(inputStream, imageUri.toString()));
                                } catch (FileNotFoundException e) {
                                    Log.e("File not Found", "ewewewe");
                                }
                                //imageview.setBackground(Drawable.createFromPath(file.getAbsolutePath()));
                                Log.i("File Path:","ok");
                            }else{
                                Log.e("File not Found", "ad");
                            }

                        }
                    }
                });
    public void backToMainActivity(View button){
        finish();
    }
    public void eraser(View view){
        pathList.clear();
        colorList.clear();
        path.reset();
    }
    public void pencil(View view){
        paint.setColor(Color.WHITE);
        brushColor= paint.getColor();
        path= new Path();
    }
    public boolean saveAs(View view){


        Bitmap drawings= imageview.getMask();
        builder.setTitle("Alert!")
                .setMessage("Confirm all the inputted mask is correct??")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();


        Bitmap emptyBitmap = Bitmap.createBitmap(drawings.getWidth(), drawings.getHeight(), drawings.getConfig());
       if (drawings.sameAs(emptyBitmap) || drawings== null) {
         Log.e("Bitmap empty", "problem with view");
        }else{
           Log.i("ERERasdsa", "saveAs() called with: view = [" + view + "]");
       }
        String uniqueString = UUID.randomUUID().toString();
        Uri imgC =null;
        ContentResolver resolver= getContentResolver();
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q){
            imgC= MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }else{
            imgC= MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        ContentValues cV= new ContentValues();
        cV.put(MediaStore.Images.Media.DISPLAY_NAME, uniqueString+".jpg");
        cV.put(MediaStore.Images.Media.MIME_TYPE,"images/jpeg");
        Uri imageUri= resolver.insert(imgC, cV);
        try{
            OutputStream outputStream= resolver.openOutputStream(Objects.requireNonNull(imageUri));
            drawings.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Objects.requireNonNull(outputStream);
            return true;

        }catch (Exception e){
            Toast.makeText(this,"Image not saved:\n"+e,Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return false;
    }



}