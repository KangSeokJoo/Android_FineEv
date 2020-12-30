package com.jinasoft.fineevapp;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jinasoft.fineevapp.Bluetooth.ConnectSettingActivity;
import com.jinasoft.fineevapp.Main.MainActivity;


public class LoadingActivity extends AppCompatActivity {



    Handler handler = new Handler();
    Runnable r = new Runnable() {
        @Override
        public void run() {
//                Intent intent = new Intent(LoadingActivity.this, ConnectSettingActivity.class);
            Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

    }

    @Override
    protected  void onResume() {
        super.onResume();
        handler.postDelayed(r,500);
    }
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(r);
    }



}
