package com.example.fypapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import static com.example.fypapp.ManualInpaint.paint;
import static com.example.fypapp.ManualInpaint.path;

import androidx.annotation.Nullable;

public class PaintMask extends View {

    public static ArrayList<Path> pathList= new ArrayList<>();
    public static ArrayList<Integer> colorList= new ArrayList<>();
    public static int brushColor= Color.WHITE;
    public ViewGroup.LayoutParams params;

    private Bitmap mask;
    private Canvas canvas;


    public PaintMask(Context context, @Nullable AttributeSet attrs){
        super(context, attrs);
        init(context);
    }
    public PaintMask(Context context){
        super(context,null);
        init(context);
    }
    public PaintMask(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        init(context);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mask = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(mask);
    }

    private void init(Context context){
     paint.setAntiAlias(true);
     paint.setColor(brushColor);
     paint.setStyle(Paint.Style.STROKE);
     paint.setStrokeJoin(Paint.Join.ROUND);
     paint.setStrokeWidth(20f);
     params= new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){

        float x= e.getX();
        float y= e.getY();
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x,y);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x,y);
                pathList.add(path);
                colorList.add(brushColor);
                invalidate();
                return true;
            default:
                return false;

        }
    }
    @Override
    protected void onDraw(Canvas canvas){
        for (int i = 0; i < pathList.size(); i++) {
            paint.setColor(brushColor);
            canvas.drawPath(pathList.get(i), paint);
            invalidate();
        }
    }
    public Bitmap getMask(){
        //this.measure(100, 100);
        //this.layout(0, 0, 100, 100);
        //this.setBackground(new);
        this.setBackgroundColor(Color.BLACK);
        this.draw(canvas);
        Log.i("draw","function");

//then create a copy of bitmap bmp1 into bmp2

        return mask.copy(mask.getConfig(), true);
    }


    public Bitmap getNoBlackMask() {
        this.draw(canvas);
        Log.i("origin","reward");
        return mask.copy(mask.getConfig(), true);
    }
}
