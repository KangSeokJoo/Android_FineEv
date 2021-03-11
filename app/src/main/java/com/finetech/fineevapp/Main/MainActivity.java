package com.finetech.fineevapp.Main;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.inputmethodservice.KeyboardView;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.finetech.fineevapp.Bluetooth.BLEService;
import com.finetech.fineevapp.Bluetooth.BleConnectDialog;
import com.finetech.fineevapp.Bluetooth.ConnectSettingActivity;
import com.finetech.fineevapp.Bluetooth.ConnectSettingActivity2;
import com.finetech.fineevapp.DebugModeActivity;
import com.finetech.fineevapp.ElecDataBase.ElecDataBase;
import com.finetech.fineevapp.R;
import com.finetech.fineevapp.Service.ForecdTerminationService;
import com.finetech.fineevapp.UserSettingActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import me.tankery.lib.circularseekbar.CircularSeekBar;

public class MainActivity extends AppCompatActivity implements MainAdapter.MyRecyclerViewClickListener , MainAdapter.MyRecyclerViewLongClickListener {

    LinearLayout IbtnFindWeb;
    LinearLayout IbtnSetting;
    TextView tvMainName;

    TextView tvTimer,tvStateOfCharge;
    TextView ImageMode;
    TextView tvMileage;
    TextView tvBattery;
    ImageView ImageChargeState;
    TextView tvLeftTimer,tvRightTimer;
    CircularSeekBar circleSeekbar;
    RecyclerView main_recyclerView;
    Button btnShare;
    TextView tvV,tvC,tvA;

    MainAdapter adapter;


    ArrayList<String> charging_dateArray = new ArrayList<>();
    ArrayList<String> charging_total_amount  = new ArrayList<>();
    ArrayList<String> charging_amount  = new ArrayList<>();
    ArrayList<String> setting_mode  = new ArrayList<>();
    ArrayList<String> charging_start_time  = new ArrayList<>();
    ArrayList<String> charging_time  = new ArrayList<>();
    ArrayList<String> charging_end_time  = new ArrayList<>();
    ArrayList<String> distance  = new ArrayList<>();
    ArrayList<String> contact_pressure  = new ArrayList<>();
    ArrayList<String> electric_current  = new ArrayList<>();
    ArrayList<String> tempArray  = new ArrayList<>();
    ArrayList<String> efficiency = new ArrayList<>();


    ConstraintLayout item_constLayout;


    LinearLayout white_Linear;
    TextView tvCarInfo;

    String Charging_Mode;

    static MainActivity mainActivity;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    int a =0;

    String strA,strC,strV;
    float sumMile;


    String CarInfo;

    Button btnLinkChk;
    Timer timer;

    boolean getDataState;
    String StateREQ;

    int DBinsert=0;
    String DBinsertState;

    int DISCONNECT_CHECK =0;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    LinearLayout Full_Linear;
    Context mContext;
    float pressedX;


    boolean ImageCheck = true;

    boolean ischarging = false;

    TextView tvError;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, ForecdTerminationService.class)); // 앱이 강제종료되는 시점에 onDestroy를 호출해주는 서비스



        Intent gattServiceIntent = new Intent(this, BLEService.class); // 블루투스 서비스 시작
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        mContext = getApplicationContext();

        tvError = findViewById(R.id.tvError);

        Full_Linear = findViewById(R.id.Full_Linear);

        Full_Linear.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {             // 리니어를 스크롤 했을때 다음화면으로 Intent 하기 위해서 드래그 시작과 끝 좌표를 저장해서 지정해둔 만큼 차이가 나면 Intent를 발생 시킨다.
                float distance = 0;

                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
// 손가락을 touch 했을 떄 x 좌표값 저장
                        pressedX = event.getX();
                        return true;
                    case MotionEvent.ACTION_UP:
// 손가락을 떼었을 때 저장해놓은 x좌표와의 거리 비교
                        distance = pressedX - event.getX();
                        break;
//                        return true;
                }
// 해당 거리가 100이 되지 않으면 이벤트 처리 하지 않는다.

                Log.d("distance",String.valueOf(distance));
                if (Math.abs(distance)< 50) {
                    return false;
                }
                if (distance > 0) {

// 손가락을 왼쪽으로 움직였으면 오른쪽 화면이 나타나야 한다.
                    Intent intent = new Intent(mContext, UserSettingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    if(ischarging == false) {
                        startActivity(intent);
                        overridePendingTransition(R.anim.left_in, R.anim.left_out);
                    }else if (ischarging == true){
                        Toast.makeText(mainActivity, "충전 중일 때는 설정화면으로 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
// 손가락을 오른쪽으로 움직였으면 왼쪽 화면이 나타나야 한다.
                    Intent intent = new Intent(mContext, UserSettingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    if(ischarging == false) {
                    startActivity(intent);
                    overridePendingTransition(R.anim.right_in, R.anim.right_out);
                    }else if (ischarging == true){
                        Toast.makeText(mainActivity, "충전 중일 때는 설정화면으로 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            }
        });




        pref = getSharedPreferences("info",MODE_PRIVATE);
        new ElecDataBase(this);


//        getExDeviceConnect();

        editor = pref.edit();
//        editor.putString("disconnect_check","0");
//        editor.commit();
        mainActivity = this;
        init();
        AddItemToAdapter();


        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));           // 리시버 등록



        mCharacteristicIndex = 0; //intent.getIntExtra(EXTRAS_CHARACTERISTIC_INDEX, 0);
        mServiceIndex =3; // + 2;

        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toggleNotify(true);
                getDataState=false;

//                getExDeviceConnect();
            }
        }, 1000);


        findViewById(R.id.tvMainName).setOnLongClickListener(n->{

            Intent intent = new Intent(MainActivity.this, DebugModeActivity.class);
            intent.putExtra("getDataState",getDataState);
            startActivity(intent);

            return false;
        });

        findViewById(R.id.tvMainName).setOnClickListener(n->{
        getExDeviceConnect();
        });

        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }

        btnLinkChk = findViewById(R.id.btnlinkChk);

//        editor.putString("disconnect_check","0");
//        editor.commit();


    // 앱이 시작 되자마자 블루투스를 연결 하기위해 블루투스 관련 서비스가 등록된 ConnectSettingActivity 를 잠깐 활성화 시킴
        Intent intent = new Intent(MainActivity.this, ConnectSettingActivity2.class);
        startActivity(intent);

        // 그리고 블루투스 검색후 DB에 있는 기기와 같은 기기를 찾으면 연결
        getExDeviceConnect();

    }

        public void HandleTest(){
            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_st00));
                    tvMainName.setText("연결이 해제되었습니다.");
                    ischarging = false;
                    //디바이스 연결 해제시
                }
//        }, 600000);
            }, 5000);
        }



    public static MainActivity getMain(){
        return mainActivity;
    }

    public void init(){  // 초기 설정

        tvV = findViewById(R.id.tvV);
        tvC = findViewById(R.id.tvC);
        tvA = findViewById(R.id.tvA);


        IbtnFindWeb = findViewById(R.id.IbtnFindWeb);
        IbtnFindWeb.setOnClickListener(n->{
//            ConnectSettingActivity.getCon().WriteBleData("#0084T400CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE + str16num + ";");
//
//            sendData("S003","T800");
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.fine-ev.com"));
//            startActivity(intent);
            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
            startActivity(intent);

        });
        IbtnSetting = findViewById(R.id.IbtnSetting);
        IbtnSetting.setOnClickListener(n->{
            Intent intent = new Intent(MainActivity.this, ConnectSettingActivity.class);
//            Intent intent = new Intent(MainActivity.this, UserSettingActivity.class);
            if (ischarging == false){
                startActivity(intent);
            }else {
                Toast.makeText(mainActivity, "충전 중일 때는 설정화면으로 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }

        });



        tvTimer = findViewById(R.id.tvTimer);
        tvStateOfCharge = findViewById(R.id.tvStateOfCharge);
        ImageMode = findViewById(R.id.ImageMode);
        tvMileage = findViewById(R.id.tvMileage);


        tvBattery = findViewById(R.id.tvBattery);
        tvBattery.setText(pref.getString("Battery","64")+"kWh");
        ImageChargeState = findViewById(R.id.ImageChargeState);

        ImageChargeState.setOnClickListener(n->{            // 블루투스로 받은 데이터에 따라 버튼 이미지 변경 , 블루투스 상태, 와이파이 상태
            Log.d("clickA",String.valueOf(a));

                if (BLEService.getBleService().getConnectBLE()) {

                    try {
                        double intEnergy = (Float.parseFloat(energy)) * 0.001;
                        if (intEnergy <= Integer.parseInt(pref.getString("Battery", "64"))) {
                            if (a == 1) {
//                    toggleNotify(false);
//                            ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_charge));
                                if (pref.getString("FullMode", "1").equals("1")) {
                                    Charging_Mode = "Full";
                                } else {
                                    if (pref.getString("SmartMode", "1").equals("1")) {
                                        Charging_Mode = "Smart";
                                    } else {
                                        Charging_Mode = "Normal";
                                    }
                                }
                                // db 에 데이터 저장
                                if (status.equals("S023") || status.equals("S123")) {
                                    String VoltMileage = pref.getString("VoltMileage", "6");
                                    try {

                                        StateREQ = "S002";
                                        sendData(StateREQ, "T100");

                                        if (DBinsert == 0) {


                                            long now = System.currentTimeMillis();
                                            Date date = new Date(now);
                                            SimpleDateFormat sdfNOW = new SimpleDateFormat("HH:mm:ss");
                                            String DATE = sdfNOW.format(date);

                                            float intPower = (float) (Integer.parseInt(power)*0.001);
                                            power = String.format("%.0f",intPower);

                                            String startTime = start.substring(8,10)+":"+start.substring(10,12)+":"+start.substring(12,14);

//                                            Log.d("energypppp",energy);
//                                            ElecDataBase.getDbHelper().setDataBase(start.substring(0, 8), pref.getString("Battery", "64"), energy, Charging_Mode,
//                                                    startTime, elapse, DATE, String.format("%.0f",sumMile), strA, strV, strC, VoltMileage,power,user_id,term_id,pref.getString("CarName",""),status,error);
//                                            AddItemToAdapter();

                                            DBinsert = 1;
                                        }

                                    } catch (Exception e) {
                                        tvMainName.setText("통신 데이터 오류 입니다.");
                                        Toast.makeText(this, "통신 데이터 오류 입니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }


                                getDataState = false;
                                a = 0;
                            } else {
                                getDataState = true;
                                a = 1;
                                StateREQ = "S001";
                                sendData(StateREQ, "T100");


                            }
                        } else {
                            Toast.makeText(MainActivity.this, "이미 충전이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        tvMainName.setText("통신 데이터 오류 입니다.");
                        Toast.makeText(this, "통신 데이터 오류 입니다.", Toast.LENGTH_SHORT).show();
                    }

                    //catch
                } else {
//                toggleNotify(false); //연결 안됨
                    String asss = pref.getString("disconnect_check","0");
                    if (pref.getString("disconnect_check", "0").equals("1")) {

                        mBleService.connect(pref.getString("device_address", ""), false);
                        Toast.makeText(MainActivity.this, "충전기 재접속", Toast.LENGTH_SHORT).show();
                        String deviceName = pref.getString("device_name", "");
                        String carName = pref.getString("CarName", "");
                        tvCarInfo= findViewById(R.id.tvCarInfo);
                        tvCarInfo.setText(carName + " / " + deviceName);

                        Handler mHandler = new Handler();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                editor.putString("disconnect_check","0");
//                                editor.commit();
                                if(!BLEService.getBleService().getConnectBLE()) {
                                    mBleService.disconnect();
                                    Toast.makeText(MainActivity.this, "기기를 찾을수 없습니다.", Toast.LENGTH_SHORT).show();
                                }
                                //디바이스 연결 해제시

                            }
                        }, 10000);


                    } else {
                        getDataState = false;
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("통신설절");
                        builder.setMessage("통신 설정이 필요합니다");
                        builder.setCancelable(true);
                        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(MainActivity.this, ConnectSettingActivity.class);
                                startActivity(intent);
                            }
                        });
                        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                            }
                        });
                        builder.create().show();

                    }
                }

        });
        tvLeftTimer = findViewById(R.id.tvLeftTimer);
        tvRightTimer = findViewById(R.id.tvRightTimer);

        circleSeekbar = findViewById(R.id.circleSeekbar);
        main_recyclerView = findViewById(R.id.main_recyclerView);
//        btnShare = findViewById(R.id.btnShare);
//        btnShare.setOnClickListener(n->{
//            Intent intent = new Intent(MainActivity.this, DialogShare.class);
//            startActivity(intent);
//        });



        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        main_recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new MainAdapter();
        adapter.setOnClickListener(this);
        adapter.setLongListener(this);
        main_recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(main_recyclerView);


        white_Linear= findViewById(R.id.white_Linear);
        white_Linear.setOnClickListener(n->{
//            Intent intent = new Intent(MainActivity.this, UserSettingActivity.class);
//            startActivity(intent);
        });



//        tvCarInfo.setText(pref.getString("CarName"," -- "));

        tvMainName = findViewById(R.id.tvMainName);
        tvMainName.setText("충전 대기 중입니다.");
    }


    public void AddItemToAdapter(){

            charging_dateArray.clear();
            charging_total_amount.clear();
            charging_amount.clear();
            setting_mode.clear();
            charging_start_time.clear();
            charging_time.clear();
            charging_end_time.clear();
            distance.clear();
            contact_pressure.clear();
            electric_current.clear();
            efficiency.clear();
            tempArray.clear();
            adapter.clearAlltems();


            Cursor cursor = ElecDataBase.getDbHelper().getDataBase();
            if (cursor.moveToFirst()) {
                do {
                    charging_dateArray.add(cursor.getString(cursor.getColumnIndex("charging_date")));
                    charging_total_amount.add(cursor.getString(cursor.getColumnIndex("charging_total_amount")));
                    charging_amount.add(cursor.getString(cursor.getColumnIndex("charging_amount")));
                    setting_mode.add(cursor.getString(cursor.getColumnIndex("setting_mode")));
                    charging_start_time  .add(cursor.getString(cursor.getColumnIndex("charging_start_time")));
                    charging_time  .add(cursor.getString(cursor.getColumnIndex("charging_time")));
                    charging_end_time  .add(cursor.getString(cursor.getColumnIndex("charging_end_time")));
                    distance .add(cursor.getString(cursor.getColumnIndex("distance")));
                    contact_pressure  .add(cursor.getString(cursor.getColumnIndex("contact_pressure")));
                    electric_current .add(cursor.getString(cursor.getColumnIndex("electric_current")));
                    efficiency.add(cursor.getString(cursor.getColumnIndex("efficiency")));

                    tempArray.add(cursor.getString(cursor.getColumnIndex("car_temp")));
                }while (cursor.moveToNext());
            }
        Collections.reverse(charging_dateArray);
        Collections.reverse(charging_total_amount);
        Collections.reverse(charging_amount);
        Collections.reverse(setting_mode);
        Collections.reverse(charging_start_time);
        Collections.reverse(charging_time);
        Collections.reverse(charging_end_time);
        Collections.reverse(distance);
        Collections.reverse(contact_pressure);
        Collections.reverse(electric_current);
        Collections.reverse(efficiency);



        for(int i =0; i<charging_dateArray.size(); i++) {
            MainAdapter.DataList data = new MainAdapter.DataList();
            String day = "";
            try {
                day = getDateDay(charging_dateArray.get(i),"yyyyMMdd");
                Log.d("day",day);
            } catch (Exception e) {
                e.printStackTrace();
            }

            data.setDate(day);

            String[] carTime = charging_time.get(i).replace(" ","").split(":");
            data.setUseTime(carTime[0]+"h " +carTime[1]+"m");

            data.setPower(charging_amount.get(i));
            data.setBatteryState("2");
            data.setCharge(charging_amount.get(i));
            if(setting_mode.get(i).equals("null")) {
                String battery = pref.getString("Battery","160");
                data.setMode(setting_mode.get(i) +"/"+battery);
            }else {
                String battery = pref.getString("Battery","160");
                data.setMode(setting_mode.get(i) +"/"+battery);
            }
            data.setStartTime(charging_start_time.get(i).substring(0,2)+" : "+charging_start_time.get(i).substring(2,4));

            data.setChargingTime(carTime[0]+"시간 " +carTime[1]+"분");

            data.setFinishTime(charging_end_time.get(i).substring(0,2)+" : "+charging_end_time.get(i).substring(2,4));
            data.setMileage(distance.get(i));
            data.setV(contact_pressure.get(i));
            data.setA(electric_current.get(i));
            data.setC(tempArray.get(i));




            adapter.addItem(data);
            adapter.notifyDataSetChanged();
        }
        if(adapter.getItemCount()==0){
            findViewById(R.id.empty_list).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.empty_list).setVisibility(View.GONE);
        }


    }
    public static String getDateDay(String date, String dateType) throws Exception {    // 날짜로 요일 가져오기

        String day = "";

        SimpleDateFormat dateFormat = new SimpleDateFormat(dateType);
        Date nDate = dateFormat.parse(date);
        SimpleDateFormat newDataFormat = new SimpleDateFormat("yy.MM.dd");
        String newDate = newDataFormat.format(nDate);

        Calendar cal = Calendar.getInstance();
        cal.setTime(nDate);

        int dayNum = cal.get(Calendar.DAY_OF_WEEK);

        switch (dayNum) {
            case 1:
                day = "일";
                break;
            case 2:
                day = "월";
                break;
            case 3:
                day = "화";
                break;
            case 4:
                day = "수";
                break;
            case 5:
                day = "목";
                break;
            case 6:
                day = "금";
                break;
            case 7:
                day = "토";
                break;

        }

        return newDate+"."+day;
    }



    @Override
    public void onProductClicked(int position) {

        Intent intent = new Intent(MainActivity.this,MainListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("CarInfo",CarInfo);
        intent.putExtra("getDataState",getDataState);
        startActivity(intent);


    }

    @Override
    public void onLongClick(int position) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//        builder.setTitle("삭제하기");
//        builder.setMessage("선택된 데이터를 삭제할까요?.");
//        builder.setCancelable(true);
//        builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int id) {
//                adapter.removeData(position);
//                adapter.notifyDataSetChanged();
//                ElecDataBase.getDbHelper().DeleteDataBase(charging_dateArray.get(position),charging_time.get(position));
//            }
//        });
//        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int id) {
//            }
//        });
//        builder.create().show();
    }


    long backKeyPressedTime;
    @Override
    public void onBackPressed() {
//        onBackPressed();
        //1번째 백버튼 클릭
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "뒤로가기 버튼을 두번 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
//            //2번째 백버튼 클릭 (종료)
        else {
            AppFinish();
        }
    }
    public void AppFinish() {
//        finish();
//        System.exit(0);
//        android.os.Process.killProcess(android.os.Process.myPid());
        finishAffinity();
//        System.runFinalization();
        System.exit(0);
    }


    /**for ble**/


    public static final String EXTRAS_CHARACTERISTIC_INDEX = "CHARACTERISTIC_INDEX";
    public static final String EXTRAS_SERVICE_INDEX = "SERVICE_INDEX";

    public static Integer mCharacteristicIndex;
    public static Integer mServiceIndex;
    public EditText writeText;

    private static final int RESULT_DISCONNECT = 5;
    private static final int REQUEST_DISCONNECT = 5;

    public static BLEService mBleService;
    public KeyboardView mKeyboardView;
    private boolean mNotifyStatus = false;

    String result;
    String rx_mode;
    String term_id;
    String user_id;
    String start;
    String end;
    String elapse;
    String count;
    String status;
    String volt;
    String currunt;
    String power;
    String energy;
    String energy2;
    String temp;
    String set_current;
    String set_timer;
    String set_mode;
    String error;
    String ver;
    String req;
    String v_code;

    int STCheck = 25;



    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBleService = ((BLEService.LocalBinder) service).getService();
            if (!mBleService.initialize()) {
                //Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
//
        }



        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBleService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d("ACTION",action);
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
//                btnLinkChk.setBackground(getResources().getDrawable(R.drawable.icon_connect));
                ImageCheck= true;
                SharedPreferences pref = getApplication().getSharedPreferences("info",MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("ClickDisconnect","1");
                editor.commit();

            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {

                Toast.makeText(MainActivity.this, "연결이 해제되었습니다.", Toast.LENGTH_SHORT).show();
                ischarging = false;
//                btnLinkChk.setBackground(getResources().getDrawable(R.drawable.icon_unconnect));

                Intent main = new Intent(MainActivity.this,MainActivity.class);
                startActivity(main);
                overridePendingTransition(0,0);

                if(ImageCheck){
                    if(pref.getString("ClickDisconnect","0").equals("1")) {
                        ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_re_connect));
                        ImageCheck = false;
                    }else{
                        ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_st00));
                        tvMainName.setText("연결이 해제되었습니다.");
                        ImageCheck = false;
                        ischarging = false;
                    }
                }
                editor.putString("disconnect_check","1");
                editor.commit();

                runAutoConnectTimer();
                mBleService.disconnect();

            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            } else if (BLEService.ACTION_GATT_WRITE.equals(action)) {

                try{
                    result = intent.getStringExtra(BLEService.EXTRA_DATA);
                }catch (Exception e){
                    tvMainName.setText("통신 데이터 오류입니다.");
                }
                rx_mode = result.substring(5, 8);
//                ((Button) findViewById(R.id.characteristic_write_button)).setTextColor(Color.argb(0xFF, 0x00, 0x70, 0x00));
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                result = intent.getStringExtra(BLEService.EXTRA_DATA);
//                result="#0145R01FTEV28160245827E88BE59083D14A59220201223145301000000000000000000160018S123216.25+00.25000000000004038600C00020T0000M3200ER000V001RQ013145374D;";
//                ((TextView) findViewById(R.id.characteristic_read)).setText(intent.getStringExtra(BLEService.EXTRA_DATA));
//                Log.d("result",result);


//                if(getDataState){
//                    ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_charge_end));
//                }else{
//                    ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_charge));
//                }
                try {

                    //블루투스에서 전달 받은 데이터 변환

                    rx_mode = result.substring(5, 8);
                    term_id = result.substring(8, 18);
                    editor.putString("term_id", term_id);
                    user_id = result.substring(18, 40);
                    editor.putString("user_id", user_id);
                    editor.commit();
                    start = result.substring(40, 54);
                    end = result.substring(54, 68);
                    elapse = result.substring(68, 74);
                    elapse = elapse.substring(0, 2) + " : " + elapse.substring(2, 4);
                    count = result.substring(74, 78);
                    status = result.substring(78, 82);
                    volt = result.substring(82, 88);
                    currunt = result.substring(88, 94);
                    power = result.substring(95, 100);
                    energy = result.substring(100, 106);
                    temp = result.substring(106, 112);
                    set_current = result.substring(112, 118);
                    set_timer = result.substring(118, 123);
                    set_mode = result.substring(123, 128);
                    error = result.substring(128, 133);
                    ver = result.substring(133, 137);
                    req = result.substring(137, 141);
                    v_code = result.substring(141, 150);


                    if(status.equals("S010") || status.equals("S110")){
//                        tvMainName.setText("충전기 코드 설정이 필요합니다.");
                        ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_st01));
                        DBinsert =1;
                    }else if(status.equals("S020") || status.equals("S120")){
                        tvMainName.setText("차량에 충전기를 연결하세요.");
                        ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_st01));
                        DBinsert =1;
                    }else if(status.equals("S020") || status.equals("S120")){
                        tvMainName.setText("충전 대기중입니다.");
                        ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_charge));
                        DBinsert =1;
                    }else if(status.equals("S023") || status.equals("S123")){
                        if(DBinsert!=0) {
                            if(!DBinsertState.equals("S023") && !DBinsertState.equals("S123")) {
                                DBinsert = 0;
                            }
                        }
                        a=1;
                        String name = pref.getString("CarName", "");
                        ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_charge_end));
                        tvMainName.setText(name + " 충전 중입니다.");
                        ischarging = true;
                    }else if (status.equals("S024") || status.equals("S124")) {
                        DBinsert =1;
                        tvMainName.setText("충전이 멈췄습니다.");
                        ischarging = false;
                        ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_charge));
                    }else if (status.equals("S025") ||status.equals("S125") ) {
                        if(DBinsert!=0) {
                            DBinsert = 0;
                        }
                        tvMainName.setText("충전이 종료되었습니다.");
                        ischarging = false;
                        ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_charge_done));
                    }

                    if(status.contains("S1")){
                        btnLinkChk.setBackground(getResources().getDrawable(R.drawable.icon_connect));
                    }else{
                        btnLinkChk.setBackground(getResources().getDrawable(R.drawable.icon_unconnect));
                    }


                    DBinsertState = status;

                            double intEnergy = (Float.parseFloat(energy)) * 0.001;
                            energy = String.format("%.1f", intEnergy);
                            energy2 = String.format("%.2f", intEnergy);

                            if (intEnergy > Integer.parseInt(pref.getString("Battery", "64"))) {
                                ImageChargeState.performClick();
                                Toast.makeText(MainActivity.this, "충전이 종료되었습니다.", Toast.LENGTH_SHORT).show();
                                ischarging = false;
                            }

                            Log.d("result",result);
                            Log.d("rx_mode", rx_mode);
                            Log.d("term_id", term_id);
                            Log.d("user_id", user_id);
                            editor.putString("term_id", term_id);
                            editor.putString("user_id", user_id);
                            editor.commit();
                            Log.d("start", start);
                            Log.d("end", end);
                            Log.d("elapse", elapse);
                            Log.d("count", count);
                            Log.d("status", status);
                            Log.d("volt", volt);
                            Log.d("currunt", currunt);
                            Log.d("power", power);
                            Log.d("energy", energy);
                            Log.d("temp", temp);
                            Log.d("set_current", set_current);
                            Log.d("set_timer", set_timer);
                            Log.d("set_mode", set_mode);
                            Log.d("error", error);
                            Log.d("ver", ver);
                            Log.d("req", req);
                            Log.d("v_code", v_code);


                            tvLeftTimer.setText(start.substring(8, 10) + ":" + start.substring(10, 12));
//                        tvRightTimer.setText(end.substring(8, 10) + ":" + end.substring(10, 12));
                            if (pref.getString("Mode", "Normal").equals("Normal")) {
                                int hour = (Integer.parseInt(start.substring(8, 10)) + 6);
                                if (hour > 24) {
                                    hour = hour - 12;
                                }
//                                tvRightTimer.setText(hour + ":" + start.substring(10, 12)); // 시간 -- : --로 표시

                            } else {
                                int hour = (Integer.parseInt(start.substring(8, 10)) + Integer.parseInt(pref.getString("ChargingTime", "0")));
                                if (hour > 24) {
                                    hour = hour - 12;
                                }
//                                tvRightTimer.setText(hour + ":" + start.substring(10, 12)); // 시간 -- : --로 표시
                            }
                            tvTimer.setText(elapse);
                            tvStateOfCharge.setText(energy2);

                            String[] currentA = currunt.split("\\.");
                            String[] voltA = volt.split("\\.");
//                            String[] tempA = temp.split("\\.");
                            temp = "038600";
                            String tempA = String.valueOf(Integer.parseInt(temp)/1000);
//                            if (tempA[0].substring(0, 1).equals("0")) {
//                                tempA[0] = tempA[0].substring(1);
//                            }

                            tvA.setText(currentA[0] + "A");
                            tvV.setText(voltA[0] + "V");
//                            if(Integer.parseInt(tempA[0])<10){
//                                tvC.setText("저온");
//                            }else {
//                                tvC.setText(tempA[0] + "˚C");
//                            }
//                            strA = currentA[0];
//                            strC = tempA[0];
//                            strV = voltA[0];
                    if(Integer.parseInt(tempA)<10){
                        tvC.setText("저온");
                    }else {
                        tvC.setText(tempA+ "˚C");
                    }
                    strA = currentA[0];
                    strC = tempA;
                    strV = voltA[0];

                            String VoltMileage = pref.getString("VoltMileage", "6");
                            String Mile2 = tvStateOfCharge.getText().toString();
                            if (!Mile2.equals("--.-")) {
                                String[] MileA = Mile2.split("\\.");
                                float MileF = Float.parseFloat(Mile2);
                                int Mileage = Integer.parseInt(VoltMileage);
//                    sumMile = Mileage * (Integer.parseInt(MileA[0]));
                                sumMile = Mileage * (MileF);
                                tvMileage.setText("+주행거리 " + String.format("%.0f", sumMile) + "km 추가");

                            }

                            String bat = pref.getString("Battery", "64");
                            int intBat = Integer.parseInt(bat);
                            circleSeekbar.setMax(intBat);
                            String[] ener = String.valueOf(intEnergy).split("\\.");
                            circleSeekbar.setProgress(Integer.parseInt(ener[0]));


//                if(status.equals("ST01")){
//                btnLinkChk.setBackground(getResources().getDrawable(R.drawable.icon_unconnect));
//                }else if(status.equals("ST02")){
//                    btnLinkChk.setBackground(getResources().getDrawable(R.drawable.icon_connect));
//                }
//                            if (!BLEService.getBleService().getConnectBLE()) {
//                                btnLinkChk.setBackground(getResources().getDrawable(R.drawable.icon_unconnect));
//                            } else {
//                                btnLinkChk.setBackground(getResources().getDrawable(R.drawable.icon_connect));
//                            }

                            String name = pref.getString("CarName", "");
                            if (BLEService.getBleService().getConnectBLE()) {
                                if(!status.equals("S009") && !status.equals("S019")  && !status.equals("S029")
                                    &&!status.equals("S109") && !status.equals("S119")  && !status.equals("S129")){
                                    tvError.setVisibility(View.GONE);
                                }

                                if(!status.equals("S025") && !status.equals("S125")){
                                    STCheck = 25;
                                }
                                if (status.equals("S023") || status.equals("S123")) {                   //충전기 상태 코드에 따라 text 변경
                                    tvMainName.setText(name + " 충전 중입니다.");
                                    ischarging = true;
                                } else if (status.equals("S010") || status.equals("S110")) {
//                                    tvMainName.setText("충전기 코드를 설정하세요.");
                                    ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_st01));
                                } else if (status.equals("S020") || status.equals("S120")) {
                                    tvMainName.setText("충전 대기 중입니다.");
                                    ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_charge));
                                    ischarging = false;
                                } else if (status.equals("S001") || status.equals("S101")) {
                                    tvMainName.setText("차량에 충전기를 연결하세요.");
                                    ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_st01));
                                    ischarging = false;
                                } else if (status.equals("S024") || status.equals("S124")) {
                                    tvMainName.setText("충전이 멈췄습니다");
                                    ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_charge_wait));
                                    ischarging = false;
                                } else if (status.equals("S025") || status.equals("S125")) {
                                    tvMainName.setText("충전이 종료되었습니다.");
                                    ImageChargeState.setBackground(getResources().getDrawable(R.drawable.btn05_ble_done));
                                    ischarging = false;

                                    InsertDataIntoDB();
                                    STCheck = 0 ;

                                } else{
                                    if(status.equals("S029") || status.equals("S129")){             //충전기 에러 코드
                                        InsertDataIntoDB();
                                    }
                                    tvError.setVisibility(View.VISIBLE);
                                    ImageChargeState.setBackground(getResources().getDrawable(R.drawable.btn04_ble_stop));
                                    if (error.equals("ER001")) {
                                        tvError.setText("(ER001)");
                                        tvMainName.setText("충전기 이상입니다.");
                                    } else if (error.equals("ER002")) {
                                        tvMainName.setText("충전기 점검이 필요합니다.");
                                        tvError.setText("(ER002)");
                                    } else if (error.equals("ER003")) {
                                        tvMainName.setText("충전기 점검이 필요합니다.");
                                        tvError.setText("(ER003)");
                                    } else if (error.equals("ER004")) {
                                        tvMainName.setText("저전압 감지, 충전을 중지합니다.");
                                        tvError.setText("(ER004)");
                                    } else if (error.equals("ER005")) {
                                        tvMainName.setText("과전압 감지, 충전을 중지합니다.");
                                        tvError.setText("(ER005)");
                                    } else if (error.equals("ER006")) {
                                        tvMainName.setText("과전류 감지, 충전을 중지합니다.");
                                        tvError.setText("(ER006)");
                                    } else if (error.equals("ER007")) {
                                        tvMainName.setText("충전기 온도가 높습니다.");
                                        tvError.setText("(ER007)");
                                    }else if (error.equals("ER008")) {
                                        tvMainName.setText("차량연결에 문제가 있습니다.");
                                        tvError.setText("(ER008)");
                                    }else if (error.equals("ER009")) {
                                        tvMainName.setText("정전 발생 기록이 있습니다.");
                                        tvError.setText("(ER009)");
                                    }else if (error.equals("ER100")) {
                                        tvMainName.setText("서버 접속을 하지 못했습니다.");
                                        tvError.setText("(ER100)");
                                    }else if (error.equals("ER101")) {
                                        tvMainName.setText("서버 응답이 없습니다.");
                                        tvError.setText("(ER101)");
                                    }else if (error.equals("ER102")) {
                                        tvMainName.setText("통신 신호가 약합니다.");
                                        tvError.setText("(ER102)");
                                    }else if (error.equals("ER110")) {
                                        tvMainName.setText("메모리가 가득 찼습니다.");
                                        tvError.setText("(ER200)");
                                    }else if (error.equals("ER200")) {
                                        tvMainName.setText("충전기를 재설정하세요.");
                                        tvError.setText("(ER200)");
                                    }
                                }
                            }
                            //통신 미설정, 충전기 미설정

//                        } else {
//                        a=0;
//                        ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_charge));
//                            if (tvTimer.getText().toString().equals("-- : --")) {
//                                tvMainName.setText("충전 대기 중입니다.");
//                            } else {
//                                tvMainName.setText("충전이 종료되었습니다.");
//                            }
//                            if(status.equals("ST"))
//                        }

                    }catch(Exception e){
                        e.printStackTrace();        //데이터 변환중 데이터가 형식에 맞지 않아서 오류가 나는 경우
                        tvMainName.setText("통신 데이터 오류입니다.");
                    }

            }
        }
    };

    private void SetResult() {
        setResult(RESULT_DISCONNECT, new Intent());
        finish();

    }

    public void InsertDataIntoDB() { // 충전 후 데이터를 저장한다.
        if (STCheck == 25) {
            String VoltMileage = pref.getString("VoltMileage", "6");
            try {

                if (pref.getString("FullMode", "1").equals("1")) {
                    Charging_Mode = "Full";
                } else {
                    if (pref.getString("SmartMode", "1").equals("1")) {
                        Charging_Mode = "Smart";
                    } else {
                        Charging_Mode = "Normal";
                    }
                }

                StateREQ = "S002";
                sendData(StateREQ, "T100");

                if (DBinsert == 0) {


                    long now = System.currentTimeMillis();
                    Date date = new Date(now);
                    SimpleDateFormat sdfNOW = new SimpleDateFormat("HH:mm:ss");
                    String DATE = sdfNOW.format(date);

                    float intPower = (float) (Integer.parseInt(power) * 0.001);
                    power = String.format("%.0f", intPower);

                    String startTime = start.substring(8, 10) + ":" + start.substring(10, 12) + ":" + start.substring(12, 14);

                    Log.d("energypppp", energy);
                    ElecDataBase.getDbHelper().setDataBase(start.substring(0, 8), pref.getString("Battery", "64"), energy, Charging_Mode,
                            startTime, elapse, DATE, String.format("%.0f", sumMile), strA, strV, strC, VoltMileage, power, user_id, term_id, pref.getString("CarName", ""), status, error);
                    AddItemToAdapter();

                    DBinsert = 1;
                }

            } catch (Exception e) {
                e.printStackTrace();
                tvMainName.setText("통신 데이터 오류 입니다.");
                Toast.makeText(this, "통신 데이터 오류 입니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    case BluetoothAdapter.STATE_OFF:
                        //case BluetoothAdapter.STATE_TURNING_OFF:
                        finish();
                        break;

                    case BluetoothAdapter.STATE_ON:
                        //case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };


    public void toggleNotify(boolean isChecked) {

        try {
            if (BLEService.getBleService().getConnectBLE()) {
                mNotifyStatus = isChecked;
                if (isChecked) {
                    if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                        mBleService.setCharacteristicIndication(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), true);
                    } else {
                        mBleService.setCharacteristicNotification(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), true);
                    }
//                if(getDataState){
//                ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_charge_end));
//                }
//                getDataState = true;
                } else {

                    if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                        mBleService.setCharacteristicIndication(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), false);
                    } else {
                        mBleService.setCharacteristicNotification(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), false);
                    }
//                if(getDataState){
//                    ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_charge));
//                }
//                tvMainName.setText("충전이 종료되었습니다.");
//                getDataState = false;
                    a = 0;


//                if (pref.getString("FullMode", "1").equals("1")) {
//                    Charging_Mode = "Full";
//                } else {
//                    if (pref.getString("SmartMode", "1").equals("1")) {
//                        Charging_Mode = "Smart";
//                    } else {
//                        Charging_Mode = "Normal";
//                    }
//                }
//                // db 에 데이터 저장
//                String VoltMileage = pref.getString("VoltMileage", "6");
//                ElecDataBase.getDbHelper().setDataBase(start.substring(0, 8), pref.getString("Battery", "64"), energy, Charging_Mode,
//                        start.substring(8), elapse, end.substring(8), String.valueOf(sumMile), strA, strV, strC, VoltMileage);
//                AddItemToAdapter();

                    BLEService.getBleService().disconnect();
                    editor.putString("disconnect_check", "0");
                    editor.commit();
                }
            } else {
                tvMainName.setText("충전기 접속이 필요합니다.");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        tvBattery = findViewById(R.id.tvBattery);
        tvBattery.setText(pref.getString("Battery","64")+"kWh");

        if(BLEService.getBleService()!=null) {
            if (BLEService.getBleService().getConnectBLE()) {       //블루투스 상태 확인후 블루투스가 연결되어 있으면 데이터 전송
//                sendData(StateREQ, "T100");
//                btnLinkChk.setBackground(getResources().getDrawable(R.drawable.icon_connect));
                ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_charge));

            } else {
                if(pref.getString("disconnect_check","0").equals("1")){
//                    btnLinkChk.setBackground(getResources().getDrawable(R.drawable.icon_unconnect));
                    if(pref.getString("ClickDisconnect","0").equals("1")) {
                        ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_re_connect));
                    }else{
                        ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_st00));
                        tvMainName.setText("연결이 해제되었습니다.");
                    }
                }else {
//                    btnLinkChk.setBackground(getResources().getDrawable(R.drawable.icon_unconnect));
                    ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_st00));
                    tvMainName.setText("연결이 해제되었습니다.");
                }
            }

            tvCarInfo = findViewById(R.id.tvCarInfo);

            if(BLEService.getBleService().getConnectBLE()) {
                String deviceName = pref.getString("device_name", "");
                String carName = pref.getString("CarName", "");
                tvCarInfo.setText(carName + " / " + deviceName);
                CarInfo = carName + " / " + deviceName;
            }else{
                String carName = pref.getString("CarName", "");
                tvCarInfo.setText(carName);
            }
        }else{
            if(pref.getString("disconnect_check","0").equals("1")){
//                btnLinkChk.setBackground(getResources().getDrawable(R.drawable.icon_unconnect));
                if(pref.getString("ClickDisconnect","0").equals("1")) {
                    ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_re_connect));
                }else{
                    ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_st00));
                    tvMainName.setText("연결이 해제되었습니다.");
                }
            }else {
//                btnLinkChk.setBackground(getResources().getDrawable(R.drawable.icon_unconnect));
                ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_st00));
                tvMainName.setText("연결이 해제되었습니다.");
            }
        }

        String Mode = pref.getString("Mode","Normal");

        if(Mode.equals("Normal")){
            ImageMode.setText("NORMAL MODE");
        }else if(Mode.equals("Full")){
            ImageMode.setText("FULL MODE");
        }else if(Mode.equals("Smart")){
            ImageMode.setText("SMART MODE");
        }

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


//        if(timer!=null){
//            timer.cancel();
//        }
//        timer = new Timer();
//        TimerTask tt= timertaskMaker();
//        timer.schedule(tt,0,1000);

        AddItemToAdapter();
    }

    public TimerTask timertaskMaker(){
        TimerTask addTask = new TimerTask() {
            @Override
            public void run() {
                String REQ = "S000";
                if(getDataState){
                    REQ = "S001";
                }else{
                    REQ = "S002";
                }

                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdfNOW = new SimpleDateFormat("yyyyMMddHHmmss");
                String DATE = sdfNOW.format(date);
                String CURRENT = "C032";
                int Current = Integer.parseInt(pref.getString("ChargingVolt","4"));
                switch (Current){
                    case 0 :
                        CURRENT = "C007";
                        break;
                    case 1 :
                        CURRENT = "C009";
                        break;
                    case 2 :
                        CURRENT = "C012";
                        break;
                    case 3 :
                        CURRENT = "C016";
                        break;
                    case 4 :
                        CURRENT = "C020";
                        break;
                    case 5 :
                        CURRENT = "C025";
                        break;
                    case 6 :
                        CURRENT = "C032";
                        break;
                }

                int Timer = Integer.parseInt(pref.getString("ChargingTime","6"));
                String TIME = "T0600";
                switch (Timer){
                    case 0:
                        TIME = "T0000";
                        break;
                    case 1:
                        TIME = "T0100";
                        break;
                    case 2:
                        TIME = "T0200";
                        break;
                    case 3:
                        TIME = "T0300";
                        break;
                    case 4:
                        TIME = "T0400";
                        break;
                    case 5:
                        TIME = "T0500";
                        break;
                    case 6:
                        TIME = "T0600";
                        break;
                }

                String UserMode = pref.getString("UserMode","0");
                String FullMode = pref.getString("FullMode","0");
                String SmartMode = pref.getString("SmartMode","0");

                String MODE = "M0"+UserMode+FullMode+SmartMode;

//                    if(term_id== null){
//                        term_id="0000000000";
//                    }
//                    if(user_id ==null){
//                        user_id = "0000000000000000000000";
//                    }

                term_id = pref.getString("term_id", "0000000000");
                user_id = pref.getString("ChargerCode", "0000000000000000000000");
//                    String full = "0084T100CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE+"C6F43772;";
                String a = "0084T100CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE;
//                    String a ="0084T100CM01FTEV00000145827E88BE59083D14A59V012S00120200920081708C032T0152M0100";
                byte[] val = a.getBytes();

//
                CalculationCRC32 crc2 = new CalculationCRC32();
                long c =0;
                c = crc2.update(val);
                String str16num = Integer.toHexString((int) c);
                Log.d("C", String.valueOf(str16num));


                ConnectSettingActivity.getCon().WriteBleData("#0084T100CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE+str16num+";");
                Log.d("SendData","#0084T100CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE+str16num+";");


            }
        };
        return addTask;
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
//        timer.cancel();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        editor.putString("disconnect_check","0");
        editor.commit();


        try {
            if (mNotifyStatus &&
                    (((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) ||
                            ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0))) {

                if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                    mBleService.setCharacteristicIndication(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), false);
                } else {
                    mBleService.setCharacteristicNotification(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), false);
                }

//                ((TextView) findViewById(R.id.characteristic_notify)).setText("Notify/Indicate: " + "No");
            }
        } catch (Exception e) {}

        unregisterReceiver(bluetoothReceiver);

        unbindService(mServiceConnection);
        mBleService = null;


    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_GATT_WRITE);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    public static void WriteBleData(String var) {
        //ASCII 를 HEX로 변환.
        //값은 16진수 Hex로 전송
//        byte[] value = {Byte.parseByte("00"), Byte.parseByte("00"), Byte.parseByte("00")};

        String start ="#";
//        var = "#T100CM01FTEV0000100000000000000000000000V01S00220200917031223C32T0115M0100C6F43772;";
        String end = ";";
        int length = var.length();
        String strLength = String.valueOf(length);
        if(strLength.length()<4){
            strLength = "0"+strLength;
        }
        String LastValue = start+strLength+var;
        byte[] value = var.getBytes();


//        Log.d("value",String.valueOf(value));
        mBleService.writeCharacteristic(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), value);

    }


    public class CalculationCRC32{


        private final long[] CRC_TABLE = {
                0x00000000, 0x77073096, 0xEE0E612C, 0x990951BA, 0x076DC419, 0x706AF48F, 0xE963A535, 0x9E6495A3, 0x0EDB8832, 0x79DCB8A4,
                0xE0D5E91E, 0x97D2D988, 0x09B64C2B, 0x7EB17CBD, 0xE7B82D07, 0x90BF1D91, 0x1DB71064, 0x6AB020F2, 0xF3B97148, 0x84BE41DE,
                0x1ADAD47D, 0x6DDDE4EB, 0xF4D4B551, 0x83D385C7, 0x136C9856, 0x646BA8C0, 0xFD62F97A, 0x8A65C9EC, 0x14015C4F, 0x63066CD9,
                0xFA0F3D63, 0x8D080DF5, 0x3B6E20C8, 0x4C69105E, 0xD56041E4, 0xA2677172, 0x3C03E4D1, 0x4B04D447, 0xD20D85FD, 0xA50AB56B,
                0x35B5A8FA, 0x42B2986C, 0xDBBBC9D6, 0xACBCF940, 0x32D86CE3, 0x45DF5C75, 0xDCD60DCF, 0xABD13D59, 0x26D930AC, 0x51DE003A,
                0xC8D75180, 0xBFD06116, 0x21B4F4B5, 0x56B3C423, 0xCFBA9599, 0xB8BDA50F, 0x2802B89E, 0x5F058808, 0xC60CD9B2, 0xB10BE924,
                0x2F6F7C87, 0x58684C11, 0xC1611DAB, 0xB6662D3D, 0x76DC4190, 0x01DB7106, 0x98D220BC, 0xEFD5102A, 0x71B18589, 0x06B6B51F,
                0x9FBFE4A5, 0xE8B8D433, 0x7807C9A2, 0x0F00F934, 0x9609A88E, 0xE10E9818, 0x7F6A0DBB, 0x086D3D2D, 0x91646C97, 0xE6635C01,
                0x6B6B51F4, 0x1C6C6162, 0x856530D8, 0xF262004E, 0x6C0695ED, 0x1B01A57B, 0x8208F4C1, 0xF50FC457, 0x65B0D9C6, 0x12B7E950,
                0x8BBEB8EA, 0xFCB9887C, 0x62DD1DDF, 0x15DA2D49, 0x8CD37CF3, 0xFBD44C65, 0x4DB26158, 0x3AB551CE, 0xA3BC0074, 0xD4BB30E2,
                0x4ADFA541, 0x3DD895D7, 0xA4D1C46D, 0xD3D6F4FB, 0x4369E96A, 0x346ED9FC, 0xAD678846, 0xDA60B8D0, 0x44042D73, 0x33031DE5,
                0xAA0A4C5F, 0xDD0D7CC9, 0x5005713C, 0x270241AA, 0xBE0B1010, 0xC90C2086, 0x5768B525, 0x206F85B3, 0xB966D409, 0xCE61E49F,
                0x5EDEF90E, 0x29D9C998, 0xB0D09822, 0xC7D7A8B4, 0x59B33D17, 0x2EB40D81, 0xB7BD5C3B, 0xC0BA6CAD, 0xEDB88320, 0x9ABFB3B6,
                0x03B6E20C, 0x74B1D29A, 0xEAD54739, 0x9DD277AF, 0x04DB2615, 0x73DC1683, 0xE3630B12, 0x94643B84, 0x0D6D6A3E, 0x7A6A5AA8,
                0xE40ECF0B, 0x9309FF9D, 0x0A00AE27, 0x7D079EB1, 0xF00F9344, 0x8708A3D2, 0x1E01F268, 0x6906C2FE, 0xF762575D, 0x806567CB,
                0x196C3671, 0x6E6B06E7, 0xFED41B76, 0x89D32BE0, 0x10DA7A5A, 0x67DD4ACC, 0xF9B9DF6F, 0x8EBEEFF9, 0x17B7BE43, 0x60B08ED5,
                0xD6D6A3E8, 0xA1D1937E, 0x38D8C2C4, 0x4FDFF252, 0xD1BB67F1, 0xA6BC5767, 0x3FB506DD, 0x48B2364B, 0xD80D2BDA, 0xAF0A1B4C,
                0x36034AF6, 0x41047A60, 0xDF60EFC3, 0xA867DF55, 0x316E8EEF, 0x4669BE79, 0xCB61B38C, 0xBC66831A, 0x256FD2A0, 0x5268E236,
                0xCC0C7795, 0xBB0B4703, 0x220216B9, 0x5505262F, 0xC5BA3BBE, 0xB2BD0B28, 0x2BB45A92, 0x5CB36A04, 0xC2D7FFA7, 0xB5D0CF31,
                0x2CD99E8B, 0x5BDEAE1D, 0x9B64C2B0, 0xEC63F226, 0x756AA39C, 0x026D930A, 0x9C0906A9, 0xEB0E363F, 0x72076785, 0x05005713,
                0x95BF4A82, 0xE2B87A14, 0x7BB12BAE, 0x0CB61B38, 0x92D28E9B, 0xE5D5BE0D, 0x7CDCEFB7, 0x0BDBDF21, 0x86D3D2D4, 0xF1D4E242,
                0x68DDB3F8, 0x1FDA836E, 0x81BE16CD, 0xF6B9265B, 0x6FB077E1, 0x18B74777, 0x88085AE6, 0xFF0F6A70, 0x66063BCA, 0x11010B5C,
                0x8F659EFF, 0xF862AE69, 0x616BFFD3, 0x166CCF45, 0xA00AE278, 0xD70DD2EE, 0x4E048354, 0x3903B3C2, 0xA7672661, 0xD06016F7,
                0x4969474D, 0x3E6E77DB, 0xAED16A4A, 0xD9D65ADC, 0x40DF0B66, 0x37D83BF0, 0xA9BCAE53, 0xDEBB9EC5, 0x47B2CF7F, 0x30B5FFE9,
                0xBDBDF21C, 0xCABAC28A, 0x53B39330, 0x24B4A3A6, 0xBAD03605, 0xCDD70693, 0x54DE5729, 0x23D967BF, 0xB3667A2E, 0xC4614AB8,
                0x5D681B02, 0x2A6F2B94, 0xB40BBE37, 0xC30C8EA1, 0x5A05DF1B, 0x2D02EF8D
        };

        private long crc = 0;

        public long update(byte[] buffer) {
//            Log.d("bufferSize", String.valueOf(buffer.length));
            for(int i = 0; i < buffer.length; i++){
                crc = (crc >> 8) ^ CRC_TABLE[(int) ((crc & 0xFF) ^ buffer[i])];
            }
            String str16num = Integer.toHexString((int) crc);

            return crc;
        }

//        public long update(final byte[] buffer) {
//            long test =0;
//            for (int i = 0; i < buffer.length; i++) {
//                test = update(buffer[i]);
//            }
//            return test;
//        }
    }
public void sendData(String REQ,String Tdata){          // 데이터 전송( 페이지 이동때 마다 그 액티비티에 대한 정보를 전송해야함
//    REQ = "S002";

    long now = System.currentTimeMillis();
    Date date = new Date(now);
    SimpleDateFormat sdfNOW = new SimpleDateFormat("yyyyMMddHHmmss");
    String DATE = sdfNOW.format(date);
    String CURRENT = "C032";
    int Current = Integer.parseInt(pref.getString("ChargingVolt","4"));
    switch (Current){
        case 0 :
            CURRENT = "C007";
            break;
        case 1 :
            CURRENT = "C009";
            break;
        case 2 :
            CURRENT = "C012";
            break;
        case 3 :
            CURRENT = "C016";
            break;
        case 4 :
            CURRENT = "C020";
            break;
        case 5 :
            CURRENT = "C025";
            break;
        case 6 :
            CURRENT = "C032";
            break;
    }

    String time = pref.getString("ChargingTime", "6");
    if(time.equals("")){
        time = "6";
    }
    int Timer = Integer.parseInt(time);

    String TIME = "T0600";
    switch (Timer){
        case 0:
            TIME = "T0000";
            break;
        case 1:
            TIME = "T0100";
            break;
        case 2:
            TIME = "T0200";
            break;
        case 3:
            TIME = "T0300";
            break;
        case 4:
            TIME = "T0400";
            break;
        case 5:
            TIME = "T0500";
            break;
        case 6:
            TIME = "T0600";
            break;
    }

    String UserMode = pref.getString("UserMode","0");
    String FullMode = pref.getString("FullMode","0");
    String SmartMode = pref.getString("SmartMode","0");

    String MODE = "M0"+UserMode+FullMode+SmartMode;

//    if(term_id== null){
//        term_id="0000000000";
//    }
//    if(user_id ==null){
//        user_id = "0000000000000000000000";
//    }

    term_id = pref.getString("term_id", "0000000000");
    user_id = pref.getString("ChargerCode", "0000000000000000000000");

//                    String full = "0084T100CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE+"C6F43772;";
    String a = "0084T100CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE;
//                    String a ="0084T100CM01FTEV00000145827E88BE59083D14A59V012S00120200920081708C032T0152M0100";
    byte[] val = a.getBytes();

//
    CalculationCRC32 crc2 = new CalculationCRC32();
    long c =0;
    c = crc2.update(val);
    String str16num = Integer.toHexString((int) c);
    Log.d("C", String.valueOf(str16num));


    ConnectSettingActivity.getCon().WriteBleData("#0084"+Tdata+"CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE+str16num+";");
    Log.d("SendData","#0084T100CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE+str16num+";");

}
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();


            adapter.removeData(position);
            adapter.notifyDataSetChanged();
            ElecDataBase.getDbHelper().DeleteDataBase(charging_dateArray.get(position),charging_time.get(position));

//            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//            builder.setTitle("삭제하기");
//            builder.setMessage("선택된 데이터를 삭제할까요?.");
//            builder.setCancelable(true);
//            builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int id) {
//                    adapter.removeData(position);
//                    adapter.notifyDataSetChanged();
//                    ElecDataBase.getDbHelper().DeleteDataBase(charging_dateArray.get(position),charging_time.get(position));
//                }
//            });
//            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int id) {
//                }
//            });
//            builder.create().show();
//            adapter.notifyItemRemoved(position);
        }
    };


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                finish();
            }
        });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult){
        super.onRequestPermissionsResult(requestCode, permissions, grantResult);
        //requestPermission 메서드에서 리퀘스트 코드로 지정한, 마지막 매개변수에 0을 넣어 줬으므로
        if(requestCode == 100){
            // requestPermission의 두번째 매개변수는 배열이므로 아이템이 여러개 있을 수 있기 때문에 결과를 배열로 받는다.
            // 해당 예시는 요청 퍼미션이 한개 이므로 i=0 만 호출한다.
            if(grantResult[0] == 0){
                //해당 권한이 승낙된 경우.
            }else{
                //해당 권한이 거절된 경우.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("권한 설정");
                builder.setMessage("권한을 허용하지 않으시면 앱을 사용하실수 없습니다.");
                builder.setPositiveButton("설정",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog , int id){
                      Intent appDetail = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName()));
                      appDetail.addCategory(Intent.CATEGORY_DEFAULT);
                      appDetail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                      startActivity(appDetail);
                    }
                });

                builder.setNegativeButton("종료",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog,int id){
                        moveTaskToBack(true);
                        finish();
                        android.os.Process.killProcess(android.os.Process.myPid());

                    }
                });
                builder.create().show();
            }
        }
    }

    public void runAutoConnectTimer(){    // 기기와 연결이 끊겼을때 한시간 동안은 버튼을 재연결로 두고 다시 클릭 시 바로 원래 연결되어있던 기기와 연결을 시도함
        if(pref.getString("ClickDisconnect","0").equals("1")) {
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    Log.d("ImageCheck", "setGray");
                    editor.putString("disconnect_check", "0");
                    editor.commit();

                    MainActivity.getMain().setButtonBack();
//                Intent main = new Intent(MainActivity.this,MainActivity.class);
//                startActivity(main);
//                overridePendingTransition(0,0);

                    //디바이스 연결 해제시
                }
            }, 600000);             // -> 재연결 시간 변경시 이 부분 수정
//            }, 5000);
        }
    }

    public void setButtonBack(){
        ImageChargeState.setBackground(getResources().getDrawable(R.drawable.ic_st00));
    }


    ArrayList<String> aDeviceAddress  = new ArrayList<>();
    ArrayList<String> aDeviceName  = new ArrayList<>();
    public void getExDeviceConnect(){   // 데이터베이스에서 기기 목록을 가져오고 , 블루투스 기기를 스캔해서 DB 에 있는 기기가 스캔되면 연결함

        Cursor cursor = ElecDataBase.getDbHelper().getDevice();
        if (cursor.moveToFirst()) {
            do {
                aDeviceAddress.add(cursor.getString(cursor.getColumnIndex("device_address")));
                aDeviceName.add(cursor.getString(cursor.getColumnIndex("device_name")));
            } while (cursor.moveToNext());
        }

            // 블루투스 스캔
        BLEScan();


    }


    private final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    ArrayList<BluetoothDevice> bleDevice = new ArrayList<>();
    ArrayList<String> bleDeviceName = new ArrayList<>();

    public void BLEScan(){

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();


        bleCheck(bluetoothAdapter);

        bluetoothAdapter.startLeScan(leScanCallback);

        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                        bluetoothAdapter.stopLeScan(leScanCallback);

//                        for(BluetoothDevice device : bleDevice ){
//                            bleDeviceName.clear();
//                            Log.d("DeviceName",device.getName());
//                        }
            }
        }, 5000);
    }
    private void bleCheck(BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter == null) {
            //블루투스를 지원하지 않으면 장치를 끈다
            Toast.makeText(this, "블루투스를 지원하지 않는 장치입니다.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            //연결 안되었을 때
            if (!bluetoothAdapter.isEnabled()) {
                //블루투스 연결
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(i);
            }
        }
    }
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if(!bleDevice.contains(device)) {
//                        bleDevice.add(device);
//                        bleDeviceName.add(device.getName());



                        for(int i=0; i<aDeviceAddress.size();i++) {
                            if(device.getAddress().equals(aDeviceAddress.get(i))) {
                                Intent intent = new Intent(MainActivity.this, BleConnectDialog.class);
                                intent.putExtra("device_name", aDeviceName.get(i));
                                intent.putExtra("device_address", aDeviceAddress.get(i));
                                startActivity(intent);

                                bluetoothAdapter.stopLeScan(leScanCallback);
                                break;

                            }
                        }


                    }
//                    Log.d("DeviceAddress",device.getAddress());
                }
            });
        }
    };
}
