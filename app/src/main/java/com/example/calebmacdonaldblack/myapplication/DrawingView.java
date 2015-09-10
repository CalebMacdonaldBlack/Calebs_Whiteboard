package com.example.calebmacdonaldblack.myapplication;

import android.graphics.Color;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

/**
 * Created by calebmacdonaldblack on 6/09/15.
 */


public class DrawingView extends View {

    //drawing path
    private Path[] drawPath = new Path[5];
    //drawing and canvas paint
    private Paint canvasPaint;
    private Paint[] drawPaint = new Paint[5];
    //initial color
    public int paintColor = 0xFF660000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;
    //maximum paths/users
    private static final int pathQty = 5;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        //get drawing area setup for interaction

        //initialize all line path and paint instances
        for (int i = 0; i < drawPath.length; i++) {
            drawPath[i] = new Path();
            drawPaint[i] = new Paint();
        }

        //apply properties to each instance of pain
        for (Paint p : drawPaint) {

            p.setColor(paintColor);
            p.setAntiAlias(true);
            p.setStrokeWidth(17);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeCap(Paint.Cap.ROUND);
            canvasPaint = new Paint(Paint.DITHER_FLAG);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //view given size
        super.onSizeChanged(w, h, oldw, oldh);

        //assign canvas bitmap to canvas
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //draw view
        for (int i = 0; i < drawPath.length; i++) {
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
            canvas.drawPath(drawPath[i], drawPaint[i]);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //detect user touch and assign information to Int array for socket output
        checkConnection();
        if (MainActivity.clientID != 0 && MainActivity.loaded == true) {
            int action = 0;

            if (event.getAction() == MotionEvent.ACTION_DOWN)
                action = 1;
            else if (event.getAction() == MotionEvent.ACTION_MOVE)
                action = 2;
            else if (event.getAction() == MotionEvent.ACTION_UP)
                action = 3;

            int[] path = {(int) event.getX(), (int) event.getY(), action, paintColor, MainActivity.clientID, 99};

            //execute event locally (makes paint smoother)

            executeTouchEvent(path);
            //send path array to be sent through socket
            MainActivity.rc.sendObjectToServer(path);
        }

        return true;
    }

    private void checkConnection() {
        //MainActivity.rc.sendObjectToServer("test");
    }

    public void executeTouchEvent(int[] i) {

        //get int array(event info) from socket input
        int touchX = i[0];
        int touchY = i[1];
        int action = i[2];
        //System.out.println(i[0] + " " + i[1] + " " + i[2] + " " + i[3] + " " + i[4]);
        switch (action) {
            case 1:
                drawPath[i[4]].moveTo(touchX, touchY);
                break;
            case 2:
                drawPath[i[4]].lineTo(touchX, touchY);
                drawCanvas.drawPath(drawPath[i[4]], drawPaint[i[4]]);
                break;
            case 3:
                drawPath[i[4]].reset();
                break;
        }
        setColor(i[3], i[4]);
        postInvalidate();
    }

    public void setColor(int newColor, int i) {
        //set color for socket input

        postInvalidate();
        drawPaint[i].setColor(newColor);
    }

    public void clearCanvas() {
        canvasBitmap.eraseColor(Color.TRANSPARENT);
        drawCanvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        for (int i = 0; i < drawPath.length; i++) {
            drawPath[i].reset(); //error could be here last thing i changed without testing
        }
        postInvalidate();
    }
}
