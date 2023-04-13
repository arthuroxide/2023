package com.example.fypapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;

public class Result extends AppCompatActivity {

    ImageView resultView;
    Bitmap result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        resultView= findViewById(R.id.result);
        Bundle extras = getIntent().getExtras();
        Uri resultImageUri = Uri.parse(extras.getString("imageUri"));
        resultView.setImageURI(resultImageUri);


    }
    public void backToMainActivity(View button) {
        Intent i=new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
    public void saveInpaint(View button){
        Bitmap emptyBitmap = Bitmap.createBitmap(result.getWidth(), result.getHeight(), result.getConfig());
        if (result.sameAs(emptyBitmap) || result == null) {
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
        Uri imageUri = resolver.insert(imgC, cV);
        try {
            OutputStream outputStream = resolver.openOutputStream(Objects.requireNonNull(imageUri));
            result.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Objects.requireNonNull(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}