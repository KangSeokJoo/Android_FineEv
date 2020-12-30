package com.jinasoft.fineevapp.ElecDataBase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ElecDataBase extends SQLiteOpenHelper implements DBObservable,DBObserver {

    //SQLite DATABASE 사용   -> DB 생성, 데이터 추가, 삭제

    private static final String DATABASE_NAME="FineEV.db";
    private static final int DATABASE_VERSION =1;

    public static final String TABLE_FINEEV ="fine_ev";
    public static final String TABLE_MYDEVICE ="devices";


    private static ElecDataBase dbHelper;
    public static synchronized ElecDataBase getDbHelper(){return dbHelper;}

    public static SQLiteDatabase DB;

    public ElecDataBase(Context context){
        super(context,context.getFilesDir().getAbsoluteFile()+"/FineEV/" + DATABASE_NAME,null,DATABASE_VERSION);
        Log.d("@!@!","DB : "+context.getFilesDir().getAbsolutePath()+"FineEV/"+DATABASE_NAME);
        dbHelper = this;
        DB = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {           // 데이터 베이스 생성 ( 데이터 베이스는 앱이 깔릴 때 생성되므로, 구조가 바뀌면 앱을 지웠다가 다시 설치해야함)
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FINEEV +
                    "(charging_date TEXT, " +                           //충전 날짜
                    "charging_total_amount TEXT, " +                //충전 배터리량
                    "charging_amount TEXT, " +                        //충전량
                    "setting_mode TEXT, " +                             //충전모드
                    "charging_start_time TEXT, "+                      //충전 시작 시간
                    "charging_time TEXT, " +                            //충전 시간
                    "charging_end_time TEXT, " +                      //충전 종료 시간
                    "distance TEXT, " +                                    // 주행거리
                    "contact_pressure TEXT, " +                         //전압
                    "electric_current TEXT, " +                         //전류
                    "efficiency TEXT, " +                         //전비
                    "power TEXT, " +
                    "user_id TEXT, " +
                    "charger_id TEXT, " +
                    "car_id TEXT, " +
                    "state TEXT, " +
                    "error TEXT, " +
                    "car_temp TEXT);");                                   //온도

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MYDEVICE +
                "(device_name TEXT, " +
                "device_address TEXT);");



    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("drop table if exists " + TABLE_FINEEV);

    }


    public void setDataBase(String charging_date,String charging_total_amount,String charging_amount,
                            String setting_mode,String charging_start_time,String charging_time,String charging_end_time,
                            String distance,String contact_pressure,String electric_current,String car_temp,String efficiency,String power
                            ,String user_id,String charger_id,String car_id,String state,String error){             //데이터 베이스에 데이터 삽입

        DB = dbHelper.getWritableDatabase();
//        DB.execSQL("INSERT INTO " + TABLE_FINEEV + " VALUES ('"+charging_date +"', '"+charging_total_amount+"' , '"+charging_amount+"' ," +
//                "  '"+setting_mode+"' , '"+charging_start_time+"' , '"+charging_time+"' , '"+charging_end_tme+"' ," +
//                "  '"+distance+"' , '"+contact_pressure+"' , '"+electric_current+"' , '" +car_temp+"');");

        DB.execSQL("INSERT INTO " +TABLE_FINEEV+ " VALUES ('"+charging_date +"' , '"+charging_total_amount+"', '"+charging_amount+"', '"+setting_mode+"'," +
                " '"+charging_start_time+"', '"+charging_time+"', '"+charging_end_time+"','"+distance+"', '"+contact_pressure+"', '"+electric_current+"', '"+efficiency+"', '"+power+"'," +
                " '"+user_id+"', '"+charger_id+"', '"+car_id+"','"+state+"', '"+error+"','"+car_temp+"');");



        Log.d("@@TABLE_FINEEV","입력됨");


    }
    public Cursor getDataBase(){                // 데이터 가져오기
        DB = dbHelper.getReadableDatabase();
        String sql = "select * from " + TABLE_FINEEV ;
        return DB.rawQuery(sql,null);
    }

    public void DeleteDataBase(String charging_date,String charging_time){ // 데이터 삭제하기
        DB = dbHelper.getWritableDatabase();
        DB.execSQL("DELETE FROM " + TABLE_FINEEV + " where charging_date=" + " '"+charging_date+"' and charging_time= "+" '"+charging_time+"' ");

    }



    public void SetDevice(String DeviceName,String DeviceAddress){      //기기 정보 입력
        DB = dbHelper.getWritableDatabase();
        DB.execSQL("INSERT INTO " + TABLE_MYDEVICE + " VALUES ('"+DeviceName +"', '"+DeviceAddress+"');");
    }

    public Cursor getDevice(){                  //기기정보 가져오기
        DB = dbHelper.getReadableDatabase();
        String sql = "select * from " + TABLE_MYDEVICE ;
        return DB.rawQuery(sql,null);
    }
    public void DeleteDevice(String DeviceName,String DeviceAddress){           //기기 정보 삭제
        DB = dbHelper.getWritableDatabase();
        DB.execSQL("DELETE FROM " + TABLE_MYDEVICE + " where device_name=" + " '"+DeviceName+"' and device_address="+" '"+DeviceAddress+"'");

    }

    @Override
    public void registerDbObserver(DBObserver databaseObserver) {

    }

    @Override
    public void removeDbObserver(DBObserver databaseObserver) {

    }

    @Override
    public void onDatabaseChanged() {

    }


    public Cursor raw() {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_FINEEV , new String[]{});

        return res;
    }

}
