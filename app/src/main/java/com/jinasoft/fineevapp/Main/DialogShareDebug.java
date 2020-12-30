package com.jinasoft.fineevapp.Main;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.jinasoft.fineevapp.ElecDataBase.ElecDataBase;
import com.jinasoft.fineevapp.ExportCSV.CSVWriter;
import com.jinasoft.fineevapp.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DialogShareDebug extends AppCompatActivity {
        // 디버그 페이지 공유하기 ( cvs 가 아닌 text 형식으로 공유함)

    Button btnClose,btnKaKao,btnMail;

    String sendTarget;

    String DATE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_share);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        }

        btnClose = findViewById(R.id.btnClose);
        btnKaKao = findViewById(R.id.btnKaKao);
        btnMail = findViewById(R.id.btnMail);
        btnMail.setOnClickListener(n->{
            sendTarget = "mail";
            ShareFile();
        });

        btnKaKao.setOnClickListener(n->{
            sendTarget = "kakao";
            ShareFile();
        });

        btnClose.setOnClickListener(n->{
            finish();
        });
    }



    private void ShareFile() {  //텍스트 형식 공유 ( 이메일, 카카오톡)

//        File exportDir = new File(Environment.getExternalStorageDirectory(), "/codesss/");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if(sendTarget.equals("mail")) {
        shareIntent.setPackage("com.google.android.gm");
        }else {
            shareIntent.setPackage("com.kakao.talk");
        }
        shareIntent.setType("text/plain");
        Intent getIntent =getIntent();
        String text =getIntent.getStringExtra("DebugText");

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNOW = new SimpleDateFormat("yyyy.MM.dd");
        DATE = sdfNOW.format(date);

        SharedPreferences pref = getApplication().getSharedPreferences("info",MODE_PRIVATE);
        String carName = pref.getString("CarName","");

//        Uri uri = Uri.fromFile(sharingGifFile);
        shareIntent.putExtra(Intent.EXTRA_TEXT, DATE+"_"+carName+"_DEBUG"+text);
        startActivity(Intent.createChooser(shareIntent, "DEBUG"));
        finish();
    }



}
