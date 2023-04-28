package com.example.fypapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.ArrayList;

public class AutoInpaint extends AppCompatActivity {


    Spinner spinner;
    ArrayList<String> options;



    // use default spinner item to show options in spinner


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_inpaint);
        options =new ArrayList<String>();
        spinner = findViewById(R.id.dropDown);
        options.add("person 1");
        options.add("person 2");
        options.add("backpack");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,options);
        spinner.setAdapter(adapter);
    }
}