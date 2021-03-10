package com.finetech.fineevapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.finetech.fineevapp.R;
import com.finetech.fineevapp.ElecDataBase.ElecDataBase;
import com.finetech.fineevapp.ExportCSV.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DialogShare extends AppCompatActivity {


    //DB 에있는 데이터를 csv 파일로 만들어서 전송

    Button btnClose,btnKaKao,btnMail;

    String sendTarget;
    String DATE;

    ArrayList<String> startTime = new ArrayList<>();    //시작 시간
    ArrayList<String> endTime = new ArrayList<>();      //종료 시간
    ArrayList<String> chargingTime = new ArrayList<>(); //충전 시간
    ArrayList<String> chargerID = new ArrayList<>();        //충전기 ID
    ArrayList<String> userID = new ArrayList<>();           //사용자 ID
    ArrayList<String> carID = new ArrayList<>();              //자동차 ID
    ArrayList<String> chargingAmount = new ArrayList<>();  //충전량
    ArrayList<String> Voltage = new ArrayList<>();  //전압
    ArrayList<String> electricCurrent = new ArrayList<>();  //전류
    ArrayList<String> power = new ArrayList<>();  //전력
    ArrayList<String> temp  = new ArrayList<>();  //전압
    ArrayList<String> state = new ArrayList<>();  //전압
    ArrayList<String> error = new ArrayList<>();  //전압

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new ExportDatabaseCSVTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                new ExportDatabaseCSVTask().execute();
            }
        });

        btnKaKao.setOnClickListener(n->{
            sendTarget = "kakao";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new ExportDatabaseCSVTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                new ExportDatabaseCSVTask().execute();
            }
        });

        btnClose.setOnClickListener(n->{
            finish();
        });
    }



    private void ShareFile() {

//        File exportDir = new File(Environment.getExternalStorageDirectory(), "/codesss/");
        File exportDir = new File(getApplication().getExternalFilesDir("cvs"),"/codesss/");


        SharedPreferences pref = getApplication().getSharedPreferences("info",MODE_PRIVATE);
        String carName = pref.getString("CarName","");

        String fileName = DATE+"_"+carName+"_"+"충전내역.csv";

        File sharingGifFile = new File(exportDir, fileName);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        if(sendTarget.equals("mail")) {             // sendTarget 이 메일이면 Gmail 을 사용함
        shareIntent.setPackage("com.google.android.gm");
        }else {
            shareIntent.setPackage("com.kakao.talk");           //sendTarget 이 카카오톡이면 바로 카카오톡으로 이동
        }
        shareIntent.setType("application/csv");


//        Uri uri = Uri.fromFile(sharingGifFile);
        Uri uri = FileProvider.getUriForFile(this,"com.finetech.fineevapp.fileprovider",sharingGifFile);

        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(shareIntent, "Share CSV"));
        finish();
    }


public class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean> {       // 데이터 베이스를 csv 로 변환  (AsyncTask 백그라운드 작업)

    private final ProgressDialog dialog = new ProgressDialog(DialogShare.this);
    ElecDataBase dbhelper;
    @Override
    protected void onPreExecute() {
        this.dialog.setMessage("데이터를 전송중입니다...");
        this.dialog.show();
        dbhelper = new ElecDataBase(DialogShare.this);
    }

    protected Boolean doInBackground(final String... args) {
        File exportDir = new File(getApplication().getExternalFilesDir("cvs"),"/codesss/");

        if (!exportDir.exists()) { exportDir.mkdirs(); }

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNOW = new SimpleDateFormat("yyyy.MM.dd");
        DATE = sdfNOW.format(date);

        SharedPreferences pref = getApplication().getSharedPreferences("info",MODE_PRIVATE);
        String carName = pref.getString("CarName","");

        File file = new File(exportDir, DATE+"_"+carName+"_"+"충전내역.csv");
        try {
            file.createNewFile();
//            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            CSVWriter csvWrite = new CSVWriter(new OutputStreamWriter(new FileOutputStream(file), "euc-kr"),',', '"');
            Cursor curCSV = dbhelper.raw();
            String[] colum= new String[13];
            colum[0] = "시작시간";
            colum[1] = "종료시간";
            colum[2] = "충전시간";
            colum[3] = "충전기";
            colum[4] = "사용자";
            colum[5] = "ID";
            colum[6] = "충전량(kWh)";
            colum[7] = "전압(v)";
            colum[8] = " 전류(A)";
            colum[9] = " 전력(kW)";
            colum[10] = " 온도(C)";
            colum[11] = " 상태";
            colum[12] = " 오류";
            csvWrite.writeNext(colum);



//            while(curCSV.moveToNext()) {
//                String arrStr[]=null;
//                String[] mySecondStringArray = new String[curCSV.getColumnNames().length];
//                for(int i=0;i<curCSV.getColumnNames().length;i++)
//                {
//                    mySecondStringArray[i] =curCSV.getString(i);
////                    mySecondStringArray[i] =curCSV.get
//                }
//                csvWrite.writeNext(mySecondStringArray);
//            }
            Cursor cursor = ElecDataBase.getDbHelper().getDataBase();
            if (cursor.moveToFirst()) {
                do {
                    startTime.add(cursor.getString(cursor.getColumnIndex("charging_start_time")));
                    endTime.add(cursor.getString(cursor.getColumnIndex("charging_end_time")));
                    chargingTime.add(cursor.getString(cursor.getColumnIndex("charging_time")));
                    chargerID.add(cursor.getString(cursor.getColumnIndex("charger_id")));
                    userID.add(cursor.getString(cursor.getColumnIndex("car_id")));
                    carID.add(cursor.getString(cursor.getColumnIndex("user_id")));
                    chargingAmount.add(cursor.getString(cursor.getColumnIndex("charging_amount")));
                    Voltage.add(cursor.getString(cursor.getColumnIndex("electric_current")));  //전압
                    electricCurrent.add(cursor.getString(cursor.getColumnIndex("contact_pressure"))); //전류
                    power.add(cursor.getString(cursor.getColumnIndex("power")));
                    temp.add(cursor.getString(cursor.getColumnIndex("car_temp")));
                    state.add(cursor.getString(cursor.getColumnIndex("state")));
                    error.add(cursor.getString(cursor.getColumnIndex("error")));
                }while (cursor.moveToNext());
            }
            for(int i = 0; i<startTime.size(); i++) {
                String[] value = new String[13];
                value[0] = startTime.get(i);
                value[1] = endTime.get(i);
                value[2] = chargingTime.get(i);
                value[3] = chargerID.get(i);
                value[4] = userID.get(i);
                value[5] = carID.get(i);
                value[6] = chargingAmount.get(i);
                value[7] = Voltage.get(i);
                value[8] = electricCurrent.get(i);
                value[9] = power.get(i);
                value[10] = temp.get(i);
                value[11] = state.get(i);
                value[12] = error.get(i);
                csvWrite.writeNext(value);
            }




            csvWrite.close();
            curCSV.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected void onPostExecute(final Boolean success) {
        if (this.dialog.isShowing()) { this.dialog.dismiss(); }
        if (success) {
//            Toast.makeText(DialogShare.this, "전송 완료", Toast.LENGTH_SHORT).show();
            ShareFile();
        } else {
            Toast.makeText(DialogShare.this, "저장된 기록이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();

        }
    }
}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult){
        super.onRequestPermissionsResult(requestCode, permissions, grantResult);
        //requestPermission 메서드에서 리퀘스트 코드로 지정한, 마지막 매개변수에 0을 넣어 줬으므로
        if(requestCode == 2){
            // requestPermission의 두번째 매개변수는 배열이므로 아이템이 여러개 있을 수 있기 때문에 결과를 배열로 받는다.
            // 해당 예시는 요청 퍼미션이 한개 이므로 i=0 만 호출한다.
            if(grantResult[0] == 0){
                //해당 권한이 승낙된 경우.
            }else{
                //해당 권한이 거절된 경우.
                AlertDialog.Builder builder = new AlertDialog.Builder(DialogShare.this);
                builder.setTitle("권한 설정");
                builder.setMessage("권한을 허용하지 않으시면 공유하기 기능을 사용하실수 없습니다.");
                builder.setPositiveButton("설정",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog , int id){
                        Intent appDetail = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName()));
                        appDetail.addCategory(Intent.CATEGORY_DEFAULT);
                        appDetail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(appDetail);
                    }
                });

                builder.setNegativeButton("공유하기 취소",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog,int id){
                        finish();

                    }
                });
                builder.create().show();
            }
        }
    }
}
