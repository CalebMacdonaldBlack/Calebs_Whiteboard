package com.example.calebmacdonaldblack.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    public static DrawingView drawView;
    public static boolean loaded;
    private ImageButton currPaint;
    public static RunClient rc;
    public static Thread thread;
    public static int clientID = 0;
    public static final int maxClients = 10;
    public static String hostName;
    public static boolean isHostName;
    public static Runnable progressDialogRunnable;
    public static ProgressDialog progressDialog;
    public static Activity context;
    public static boolean hasConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        drawView = (DrawingView) findViewById(R.id.drawing);

        MainActivity.context = this;
        openProgressDialog("Loading", "Connecting to Server", progressDialog);

        LinearLayout paintLayout = (LinearLayout) findViewById(R.id.paint_colors);
        currPaint = (ImageButton) paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        hostName = getPrefs.getString("hostName", "dinodan.is-into-anime.com");
        isHostName = getPrefs.getBoolean("isHostNameCheckbox", true);
        loaded = false;


        thread = new Thread(rc = new RunClient(this));
        thread.start();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.preferences) {
            Intent p = new Intent("android.intent.action.PREFS");
            startActivity(p);
            return true;
        }
        if (id == R.id.clearScreen) {
            clearScreen();
            rc.sendObjectToServer("clearTheScreen");
            return true;
        }

        return super.onOptionsItemSelected(item);


    }

    public void paintClicked(View view) {
        //use chosen color

        if (view != currPaint) {
            //update color

            ImageButton imgView = (ImageButton) view;
            String color = (String) view.getTag();
            drawView.setColor(Color.parseColor(color), clientID);
            drawView.paintColor = Color.parseColor(color);

            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint = (ImageButton) view;
        }
    }

    public static void clearScreen() {
        drawView.clearCanvas();
    }

    public static void openProgressDialog(final String title, final String message, final ProgressDialog d) {
        MainActivity.context.runOnUiThread(progressDialogRunnable = new Runnable() {
            public void run() {
                MainActivity.progressDialog = MainActivity.progressDialog.show(MainActivity.context, title, message, true);

                try {
                    timerDelayRemoveDialog(20000, d);
                } catch (NullPointerException ignore) {

                }
            }
        });
    }

    public static void timerDelayRemoveDialog(long time, final ProgressDialog d) throws NullPointerException {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (RunClient.connectionStatus) ;
                d.dismiss();
                MainActivity.rc.stopRunning();
            }
        }, time);
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.exit(0);
    }
}
