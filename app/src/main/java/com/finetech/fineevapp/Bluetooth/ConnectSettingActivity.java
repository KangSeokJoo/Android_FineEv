package com.finetech.fineevapp.Bluetooth;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
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
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finetech.fineevapp.ElecDataBase.ElecDataBase;
import com.finetech.fineevapp.Main.MainActivity;
import com.finetech.fineevapp.R;
import com.finetech.fineevapp.SetCodeActivity;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectSettingActivity extends AppCompatActivity implements AutoPermissionsListener,ConnectSettingAdapter.MyRecyclerViewClickListener {
    private ConnectSettingAdapter mLeDeviceListAdapter;


    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private boolean mScanning;
    private boolean mBluetoothStatus = false;
    private List<Integer> mRSSIs;
    private List<Long> mAdvTimes;
    private List<Long> mpAdvTimes;
    private List<ScanResult> mScanResults;
    private List<byte[]> mScanResultsDep;
    private ArrayList<BluetoothDevice> mListScanDevice = new ArrayList<>();

    //    ConstraintLayout[] btnShowPassword = new ConstraintLayout[];
    ArrayList<ConstraintLayout> btnShowPassword = new ArrayList<>();

    ArrayList<String> mScanDeviceName = new ArrayList<>();

    private ArrayList<BluetoothDevice> mScanDevice = new ArrayList<>();
    private Handler mHandler;
    private Runnable mRunnable;
    private ScanCallback mScanCallback;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_LOCATION = 1;
    private static final long SCAN_PERIOD = 10000;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static BLEService mBleService;


    ArrayList<String> aDeviceName = new ArrayList<>();
    ArrayList<String> aDeviceAddress = new ArrayList<>();


    ArrayList<String> scanDeviceName = new ArrayList<>();
    ArrayList<String> allDeviceName = new ArrayList<>();
    ArrayList<String> allDeviceAddress = new ArrayList<>();


    public static Integer mCharacteristicIndex;
    public static Integer mServiceIndex;
    Timer timer;

    String term_id, user_id;
    boolean getDataState;
    SharedPreferences pref;
    SharedPreferences.Editor editor;


    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    case BluetoothAdapter.STATE_OFF:
                        //case BluetoothAdapter.STATE_TURNING_OFF:
                        mBluetoothStatus = true;
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.app_name)
                                .setMessage(R.string.error_bluetooth_off)
                                .setIcon(R.mipmap.ic_launcher)
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(ConnectSettingActivity.this, R.string.error_bluetooth_off, Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                })
                                .show();
                        break;

                    case BluetoothAdapter.STATE_ON:
                        //case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                mLeDeviceListAdapter.notifyDataSetChanged();
                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (BLEService.getBleService().getConnectBLE()) {
                                if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                                    mBleService.setCharacteristicIndication(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), true);
                                } else {
                                    mBleService.setCharacteristicNotification(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), true);
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }, 2000);

//                timer = new Timer();
//                TimerTask tt= timertaskMaker();
//                timer.schedule(tt,0,1000);
                SharedPreferences pref = getApplication().getSharedPreferences("info", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("ClickDisconnect", "1");
                editor.commit();

            } else if (BLEService.ACTION_GATT_BONDED.equals(action)) {

            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {

                invalidateOptionsMenu();
//                reScanBLE();
//                BLEScan();
//                ScanBleResume();
                mLeDeviceListAdapter.notifyDataSetChanged();
                mBleService.disconnect();
//                Toast.makeText(context, "연결 해제 됨", Toast.LENGTH_SHORT).show();
                //                clearUI();
            } else if (BLEService.ACTION_GATT_CONNECTING.equals(action)) {
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {


            }
        }
    };

    public TimerTask timertaskMaker() {
        TimerTask addTask = new TimerTask() {
            @Override
            public void run() {
                String REQ = "S000";
                if (getDataState) {
                    REQ = "S001";
                } else {
                    REQ = "S002";
                }

                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdfNOW = new SimpleDateFormat("yyyyMMddHHmmss");
                String DATE = sdfNOW.format(date);
                String CURRENT = "C032";
                int Current = Integer.parseInt(pref.getString("ChargingVolt", "4"));
                switch (Current) {
                    case 0:
                        CURRENT = "C007";
                        break;
                    case 1:
                        CURRENT = "C009";
                        break;
                    case 2:
                        CURRENT = "C012";
                        break;
                    case 3:
                        CURRENT = "C016";
                        break;
                    case 4:
                        CURRENT = "C020";
                        break;
                    case 5:
                        CURRENT = "C025";
                        break;
                    case 6:
                        CURRENT = "C032";
                        break;
                }

                int Timer = Integer.parseInt(pref.getString("ChargingTime", "6"));
                String TIME = "T0600";
                switch (Timer) {
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

                String UserMode = pref.getString("UserMode", "0");
                String FullMode = pref.getString("FullMode", "0");
                String SmartMode = pref.getString("SmartMode", "0");

                String MODE = "M0" + UserMode + FullMode + SmartMode;

                if (term_id == null) {
                    term_id = "0000000000";
                }
                if (user_id == null) {
                    user_id = "0000000000000000000000";
                }
                String a = "0084T400CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE;
//                    String a ="0084T100CM01FTEV00000145827E88BE59083D14A59V012S00120200920081708C032T0152M0100";
                byte[] val = a.getBytes();

//
                CalculationCRC32 crc2 = new CalculationCRC32();
                long c = 0;
                c = crc2.update(val);
                String str16num = Integer.toHexString((int) c);
                Log.d("C", String.valueOf(str16num));

                term_id = pref.getString("term_id", "0000000000");
                user_id = pref.getString("ChargerCode", "0000000000000000000000");

                ConnectSettingActivity.getCon().WriteBleData("#0084T300CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE + str16num + ";");
                Log.d("SendData", "#0084T100CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE + str16num + ";");
                //                ConnectSettingActivity.getCon().WriteBleData("#T100CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE+"C6F43772;");

            }
        };
        return addTask;
    }

    Button setCode;
    TextView IbtnSetting;
    LinearLayout btnClose;
    RecyclerView device_listView;
    Button Search_Charger;

    EditText edtCarName, edtVolt, edtBattery;


    static ConnectSettingActivity conActivity;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_setting);

        /**와이파이 검색 안함**/
//        scanWIFI();


        pref = getSharedPreferences("info", MODE_PRIVATE);
        editor = pref.edit();

        Intent get = getIntent();
        getDataState = get.getBooleanExtra("getDataState", true);
        mCharacteristicIndex = 0; //intent.getIntExtra(EXTRAS_CHARACTERISTIC_INDEX, 0);
        mServiceIndex = 3; // + 2;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {           //위치 권한 허용
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }

        conActivity = this;
        new ElecDataBase(this);

        Intent gattServiceIntent = new Intent(this, BLEService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        setCode = findViewById(R.id.setCode);
        setCode.setOnClickListener(n -> {
                Intent intent = new Intent(ConnectSettingActivity.this, SetCodeActivity.class);
                intent.putExtra("getDataState", getDataState);
                startActivity(intent);
//            mLeDeviceListAdapter.notifyDataSetChanged();

        });


        if (MainActivity.getMain() != null) {
            btnClose = findViewById(R.id.btnClose);
            btnClose.setOnClickListener(n -> {
                editor.putString("CarName", edtCarName.getText().toString());
                editor.putString("VoltMileage", edtVolt.getText().toString());
                editor.putString("Battery", edtBattery.getText().toString());
                editor.commit();

                onBackPressed();
            });
            IbtnSetting = findViewById(R.id.IbtnSetting);
            IbtnSetting.setOnClickListener(n -> {
//                if(!pref.getString("wifiName","0").equals("0")){
                if (BLEService.getBleService().getConnectBLE()) {
                    if (!edtCarName.getText().toString().equals("")) {
                        if (!edtVolt.getText().toString().equals("")) {
                            if (!edtBattery.getText().toString().equals("")) {
                                String chargerCode = pref.getString("ChargerCode", "");
//                                if (chargerCode.equals("")) {
//                                    AlertDialog.Builder builder = new AlertDialog.Builder(ConnectSettingActivity.this);
//                                    builder.setTitle("코드 설정");
//                                    builder.setMessage("충전기 코드를 입력해주세요.");
//                                    builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int id) {
//
//                                            editor.putString("CarName", edtCarName.getText().toString());
//                                            editor.putString("VoltMileage", edtVolt.getText().toString());
//                                            editor.putString("Battery", edtBattery.getText().toString());
//                                            editor.commit();
//                                            Intent intent = new Intent(ConnectSettingActivity.this, SetCodeActivity.class);
//                                            startActivity(intent);
//                                        }
//                                    });
//
//                                    builder.create().show();
//                                } else {
                                    if (BLEService.getBleService().getConnectBLE()) {
                                        String REQ = "S003";                    // 전송 포멧에 맞게 데이터를 생성하여 송신한다.     ElectronicCarData -> BLE_프로토콜_R3 확인

                                        long now = System.currentTimeMillis();
                                        Date date = new Date(now);
                                        SimpleDateFormat sdfNOW = new SimpleDateFormat("yyyyMMddHHmmss");
                                        String DATE = sdfNOW.format(date);
                                        String CURRENT = "C032";
                                        int Current = Integer.parseInt(pref.getString("ChargingVolt", "4"));
                                        switch (Current) {
                                            case 0:
                                                CURRENT = "C007";
                                                break;
                                            case 1:
                                                CURRENT = "C009";
                                                break;
                                            case 2:
                                                CURRENT = "C012";
                                                break;
                                            case 3:
                                                CURRENT = "C016";
                                                break;
                                            case 4:
                                                CURRENT = "C020";
                                                break;
                                            case 5:
                                                CURRENT = "C025";
                                                break;
                                            case 6:
                                                CURRENT = "C032";
                                                break;
                                        }

                                        String time = pref.getString("ChargingTime", "0");
                                        if (time.equals("")) {
                                            time = "0";
                                        }
                                        int Timer = Integer.parseInt(time);

                                        String TIME = "T0000";
                                        switch (Timer) {
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
                                                TIME = "T0400";
                                                break;
                                            case 6:
                                                TIME = "T0500";
                                                break;
                                            case 7:
                                                TIME = "T0600";
                                                break;
                                        }

                                        String UserMode = pref.getString("UserMode", "0");
                                        String FullMode = pref.getString("FullMode", "0");
                                        String SmartMode = pref.getString("SmartMode", "0");

                                        String MODE = "M0" + UserMode + FullMode + SmartMode;

                                        if (term_id == null) {
                                            term_id = "0000000000";
                                        }
                                        if (user_id == null) {
                                            user_id = "0000000000000000000000";
                                        }
                                        String a = "0084T400CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE;
//                                      String a ="0084T100CM01FTEV00000145827E88BE59083D14A59V012S00120200920081708C032T0152M0100";


                                        byte[] val = a.getBytes();

//
                                        CalculationCRC32 crc2 = new CalculationCRC32();
                                        long c = 0;
                                        c = crc2.update(val);
                                        String str16num = Integer.toHexString((int) c);
                                        Log.d("C", String.valueOf(str16num));

                                        term_id = pref.getString("term_id", "0000000000");
                                        user_id = pref.getString("ChargerCode", "0000000000000000000000");

                                        ConnectSettingActivity.getCon().WriteBleData("#0084T210CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE + str16num + ";");
                                        Log.d("SendData", "#0084T100CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE + str16num + ";");
                                        //                ConnectSettingActivity.getCon().WriteBleData("#T100CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE+"C6F43772;");

                                    }
                                    editor.putString("CarName", edtCarName.getText().toString());
                                    editor.putString("VoltMileage", edtVolt.getText().toString());
                                    editor.putString("Battery", edtBattery.getText().toString());
                                    editor.commit();
                                    setResult(111);
                                    finish();
//                                }

                            } else {
                                Toast.makeText(this, "배터리 용량을 입력해주세요.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "전비를 입력해주세요", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "차 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ConnectSettingActivity.this, "연결된 기기가 없습니다.", Toast.LENGTH_SHORT).show();
                }

//            else{
//                    Toast.makeText(ConnectSettingActivity.this, "AP 설정이 필요합니다.", Toast.LENGTH_SHORT).show();
//            }
            });


        } else {
            btnClose = findViewById(R.id.btnClose);
            btnClose.setVisibility(View.GONE);
            IbtnSetting = findViewById(R.id.IbtnSetting);
            IbtnSetting.setOnClickListener(n -> {
                if (!edtCarName.getText().toString().equals("")) {
                    if (!edtVolt.getText().toString().equals("")) {
                        if (!edtBattery.getText().toString().equals("")) {
                            editor.putString("CarName", edtCarName.getText().toString());
                            editor.putString("VoltMileage", edtVolt.getText().toString());
                            editor.putString("Battery", edtBattery.getText().toString());
                            editor.commit();
                            Intent intent2 = new Intent(ConnectSettingActivity.this, MainActivity.class);
                            startActivity(intent2);

                        } else {
                            Toast.makeText(this, "배터리 용량을 입력해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "전비를 입력해주세요", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "차 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }

            });
        }


        init();
        BLEScan();
        ScanBleResume();


        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

//        mLeDeviceListAdapter.addDevice(null,null);

//        finish();
    }


    public void reScanBLE() {
        mLeDeviceListAdapter.clearAlltems();
        //            setListAdapter(mLeDeviceListAdapter);

        device_listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ScrollView scroll = findViewById(R.id.ScrollView);
                scroll.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        scanLeDevice(true);
        Search_Charger.setText("검색 중지");
    }


    public void init() {

        device_listView = findViewById(R.id.device_listView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        device_listView.setLayoutManager(linearLayoutManager);

        mLeDeviceListAdapter = new ConnectSettingAdapter();
        mLeDeviceListAdapter.setOnClickListener(this);
        device_listView.setAdapter(mLeDeviceListAdapter);


        edtCarName = findViewById(R.id.edtCarName);
        edtVolt = findViewById(R.id.edtVolt);
        edtVolt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!edtVolt.getText().toString().equals("")) {
                    if (Integer.parseInt(edtVolt.getText().toString()) > 20) {      //입력된 text 가 20보다 크면
                        edtVolt.setText("20");                                                    // 20으로 변경
                        Toast.makeText(ConnectSettingActivity.this, "전비 설정 범위는 1 ~ 20 입니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        edtBattery = findViewById(R.id.edtBattery);
        edtBattery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!edtBattery.getText().toString().equals("")) {
                    if (Integer.parseInt(edtBattery.getText().toString()) > 120) {
                        edtBattery.setText("120");
                        Toast.makeText(ConnectSettingActivity.this, "배터리 용량 범위는 1 ~ 120 입니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        SharedPreferences pref = getApplication().getSharedPreferences("info", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        edtCarName.setText(pref.getString("CarName", ""));
        edtVolt.setText(pref.getString("VoltMileage", ""));
        edtBattery.setText(pref.getString("Battery", ""));


    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBleService = ((BLEService.LocalBinder) service).getService();
            if (!mBleService.initialize()) {
                //Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

//            updateConnectionState(R.string.disconnected);
//            invalidateOptionsMenu();

            /*
            mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    invalidateOptionsMenu();
                }
            }, 1000);
            */

            // Automatically connects to the device upon successful start-up initialization.
            //mBleService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBleService = null;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void BLEScan() {

        Search_Charger = findViewById(R.id.search_charger);

        Search_Charger.setOnClickListener(n -> {
            if (Search_Charger.getText().toString().equals("충전기 검색")) {
                mLeDeviceListAdapter = new ConnectSettingAdapter();
                mLeDeviceListAdapter.clearAlltems();
                //            setListAdapter(mLeDeviceListAdapter);
//                scanLeDevice(true);

                scanDeviceName.clear();
                allDeviceName.clear();
                aDeviceName.clear();
                aDeviceAddress.clear();
                mScanDeviceName.clear();

//                BLEScan();
                ScanBleResume();
//                mLeDeviceListAdapter.addItem("","");
                Search_Charger.setText("검색 중지");
            } else {
                if (mHandler != null && mRunnable != null) {
                    mHandler.removeCallbacks(mRunnable);
                }
//                scanLeDevice(false);
                Search_Charger.setText("충전기 검색");

            }
        });


        device_listView = findViewById(R.id.device_listView);


        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void ScanBleResume() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            mScanResultsDep = new ArrayList<byte[]>();
        else
            mScanResults = new ArrayList<ScanResult>();

//        setListAdapter(mLeDeviceListAdapter);
        device_listView.setAdapter(mLeDeviceListAdapter);

//        ((ListView) getListView()).setCacheColorHint(0);
//        ((ListView) getListView()).setBackgroundResource(R.drawable.backgroundimage);

        setLECallbacks();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled() && !mBluetoothStatus) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder() //
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) //
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) //
                        .build();

                ScanFilter filter = new ScanFilter.Builder().setDeviceName(null).build();
                filters = new ArrayList<ScanFilter>();
                filters.add(filter);
            }

            scanLeDevice(true);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();


        if (BLEService.getBleService().getConnectBLE()) {
            String REQ = "S003";

            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdfNOW = new SimpleDateFormat("yyyyMMddHHmmss");
            String DATE = sdfNOW.format(date);
            String CURRENT = "C032";
            int Current = Integer.parseInt(pref.getString("ChargingVolt", "4"));
            switch (Current) {
                case 0:
                    CURRENT = "C007";
                    break;
                case 1:
                    CURRENT = "C009";
                    break;
                case 2:
                    CURRENT = "C012";
                    break;
                case 3:
                    CURRENT = "C016";
                    break;
                case 4:
                    CURRENT = "C020";
                    break;
                case 5:
                    CURRENT = "C025";
                    break;
                case 6:
                    CURRENT = "C032";
                    break;
            }

            String time = pref.getString("ChargingTime", "6");
            if (time.equals("")) {
                time = "0";
            }
            int Timer = Integer.parseInt(time);

            String TIME = "T0000";
            switch (Timer) {
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
                    TIME = "T0400";
                    break;
                case 6:
                    TIME = "T0500";
                    break;
                case 7:
                    TIME = "T0600";
                    break;
            }

            String UserMode = pref.getString("UserMode", "0");
            String FullMode = pref.getString("FullMode", "0");
            String SmartMode = pref.getString("SmartMode", "0");

            String MODE = "M0" + UserMode + FullMode + SmartMode;


            String a = "0084T400CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE;
//                    String a ="0084T100CM01FTEV00000145827E88BE59083D14A59V012S00120200920081708C032T0152M0100";


            byte[] val = a.getBytes();

//
            CalculationCRC32 crc2 = new CalculationCRC32();
            long c = 0;
            c = crc2.update(val);
            String str16num = Integer.toHexString((int) c);
            Log.d("C", String.valueOf(str16num));

            term_id = pref.getString("term_id", "0000000000");
            user_id = pref.getString("ChargerCode", "0000000000000000000000");

            ConnectSettingActivity.getCon().WriteBleData("#0084T210CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE + str16num + ";");
            Log.d("SendData", "#0084T100CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE + str16num + ";");
            //                ConnectSettingActivity.getCon().WriteBleData("#T100CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE+"C6F43772;");

        }


//        if(timer!=null){
//            timer.cancel();
//        }
//        timer = new Timer();
//        TimerTask tt= timertaskMaker();
//        timer.schedule(tt,0,1000);

    }

    @Override
    protected void onPause() {
        super.onPause();

//        if(BLEService.getBleService()!=null) {
//            if (BLEService.getBleService().getConnectBLE()) {
//                if(timer!=null) {
//                    timer.cancel();
//                }
//            }
//        }
    }

    @Override
    protected void onDestroy() {   // connectSettingActivity 가 Destroy되면

//        if (mGattUpdateReceiver != null) {
//            unregisterReceiver(mGattUpdateReceiver);
//        }
//        if (bluetoothReceiver != null) {
//            unregisterReceiver(bluetoothReceiver);
//        }
//
//        if (mBleService.serviceStatus) {
//            unbindService(mServiceConnection);
//        }
//        mBleService.close();
//        mBleService = null;

        unregisterReceiver(bluetoothReceiver); //브로드 캐스트 리시버 해제
        super.onDestroy();
    }

    private void scanLeDevice(final boolean enable) { // 디바이스 스캔
        if (enable) {
            // Stops scanning after a pre-defined scan period.

            mRunnable = new Runnable() {
                @Override
                public void run() {
//                    if (mLeDeviceListAdapter.isEmpty() && mScanning) {
//                        Toast.makeText(ConnectSettingActivity.this, R.string.ble_scan_empty, Toast.LENGTH_SHORT).show();
//                    }
                    mScanning = false;
                    if (mBluetoothAdapter.isEnabled()) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);  // 이전버전 스캐너
                        } else {
                            mBLEScanner.stopScan(mScanCallback);        // 최근 버전 스캐너
                        }
                        invalidateOptionsMenu();
                    }

                }
            };

            mHandler.postDelayed(mRunnable, SCAN_PERIOD);

            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    Search_Charger.setText("충전기 검색");

                }
            }, 10000);
            mScanning = true;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mBLEScanner.startScan(filters, settings, mScanCallback);   //스캔 시작
            }
        } else {
            mScanning = false;
            if (mBluetoothAdapter.isEnabled()) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                } else {
                    mBLEScanner.stopScan(mScanCallback);
                }
            }
        }
        invalidateOptionsMenu();


    }

    private void setLECallbacks() { // 기기가 스캔되었을때 동작할 CallBack

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Device scan callback.
            mLeScanCallback =
                    new BluetoothAdapter.LeScanCallback() {

                        @Override
                        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                            //Log.i("Result + RSSI", scanRecord.toString() + rssi);
                            /*
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mLeDeviceListAdapter.addDeviceDep(device, rssi, scanRecord);
                                    mLeDeviceListAdapter.notifyDataSetChanged();
                                }
                            });
                            */


                            runOnUiThread(new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Cursor cursor = ElecDataBase.getDbHelper().getDevice();   //데이터베이스에 저장되어있는 기기들과 스캔된 기기들을 비교하고 , 스캔된 기기에 없어도 DB에 있는 기기도 일단 리스트에 추가한다.
                                                                                                                    // 스캔은 되어있지 않고 DB 에서만 가져온 기기 목록을 클릭하면 주변에 없는 기기라는것을 알려야함. ( ConnectSettingAdapter에 구현되어있음)
                                    if (cursor.moveToFirst()) {
                                        do {
                                            aDeviceAddress.add(cursor.getString(cursor.getColumnIndex("device_address")));
                                            aDeviceName.add(cursor.getString(cursor.getColumnIndex("device_name")));
                                        } while (cursor.moveToNext());
                                    }

                                    for (int i = 0; i < aDeviceAddress.size(); i++) {
                                        if (!allDeviceName.contains(aDeviceName.get(i))) {
                                            allDeviceName.add(aDeviceName.get(i));
                                            allDeviceAddress.add(aDeviceAddress.get(i));
                                            ConnectSettingAdapter.DataList data = new ConnectSettingAdapter.DataList();
                                            data.setDevice_Name(aDeviceName.get(i));
                                            data.setDevice_Address(aDeviceAddress.get(i));
                                            data.setDbDeviceName(aDeviceName);
                                            data.setDbDeviceAddress(aDeviceAddress);
                                            data.setScanDeviceName(scanDeviceName);
                                            data.setSetVisible(false);
                                            mLeDeviceListAdapter.addItem(data);
                                            mLeDeviceListAdapter.notifyDataSetChanged();
                                        }
                                    }
                                    if (!scanDeviceName.contains(device.getName())) {
                                        mLeDeviceListAdapter.addScanDevice(device.getName());
                                        if (!allDeviceName.contains(device.getName())) {
                                            scanDeviceName.add(device.getName());
                                            ConnectSettingAdapter.DataList data = new ConnectSettingAdapter.DataList();
                                            data.setDevice_Name(device.getName());
                                            data.setDevice_Address(device.getAddress());
                                            data.setDbDeviceName(aDeviceName);
                                            data.setDbDeviceAddress(aDeviceAddress);
                                            data.setScanDeviceName(scanDeviceName);
                                            data.setSetVisible(false);
                                            mLeDeviceListAdapter.addItem(data);
                                            mLeDeviceListAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            }));
                        }
                    };

        } else {                    //최근 버전일때 실행되는 코드  위에 부분은 롤리팝 이하
            mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    //Log.i("Result + RSSI", result.toString() + result.getRssi());
                    /*
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mLeDeviceListAdapter.addDevice(result.getDevice(), result.getRssi(), result);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                    */

                    runOnUiThread(new Thread(new Runnable() {
                        @Override
                        public void run() {

                            aDeviceName.clear();            //기기 목록을 한번 clear 해야함  , 안하면 같은 이름의 기기가 반복적으로 계속 추가됨
                            aDeviceAddress.clear();
                            Cursor cursor = ElecDataBase.getDbHelper().getDevice();
                                if (!cursor.isFirst())
                                cursor.moveToFirst();             //DB 에서 저장목록 가져오기
                                while (cursor.isLast()){
                                     cursor.moveToNext();
                                     aDeviceAddress.add(cursor.getString(cursor.getColumnIndex("device_address")));
                                     aDeviceName.add(cursor.getString(cursor.getColumnIndex("device_name")));
                                 };


                            for (int i = 0; i < aDeviceAddress.size(); i++) {
                                if (!allDeviceName.contains(aDeviceName.get(i))) {
                                    allDeviceName.add(aDeviceName.get(i));
                                    allDeviceAddress.add(aDeviceAddress.get(i));
                                    ConnectSettingAdapter.DataList data = new ConnectSettingAdapter.DataList();
                                    data.setDevice_Name(aDeviceName.get(i));
                                    data.setDevice_Address(aDeviceAddress.get(i));
                                    data.setDbDeviceName(aDeviceName);
                                    data.setDbDeviceAddress(aDeviceAddress);
                                    data.setScanDeviceName(scanDeviceName);
                                    data.setSetVisible(false);
                                    mLeDeviceListAdapter.addItem(data);
                                    mLeDeviceListAdapter.notifyDataSetChanged();

                                }
                            }

                            if (result.getDevice().getName() != null && result.getDevice().getName().contains("FTEV")) {            //검색된 기기중 FTEV 가 포함된 기기만 리스트에 추가
//                                if (result.getDevice().getName()!=null) {
                                if (!scanDeviceName.contains(result.getDevice().getName())) {
                                    mLeDeviceListAdapter.addScanDevice(result.getDevice().getName());
                                    if (!allDeviceName.contains(result.getDevice().getName())) {
                                        scanDeviceName.add(result.getDevice().getName());
                                        allDeviceName.add(result.getDevice().getName());
                                        allDeviceAddress.add(result.getDevice().getAddress());
                                        ConnectSettingAdapter.DataList data = new ConnectSettingAdapter.DataList();
                                        data.setDevice_Name(result.getDevice().getName());
                                        data.setDevice_Address(result.getDevice().getAddress());
                                        data.setDbDeviceName(aDeviceName);
                                        data.setDbDeviceAddress(aDeviceAddress);
                                        data.setScanDeviceName(scanDeviceName);
                                        data.setSetVisible(false);
                                        mLeDeviceListAdapter.addItem(data);
                                        mLeDeviceListAdapter.notifyDataSetChanged();
                                    }
                                }

                            }
                        }

                    }));

                    //super.onScanResult(callbackType, result);
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    //for (ScanResult sr : results) {
                    //    Log.i("Scan Result", sr.toString());
                    //}
                    //super.onBatchScanResults(results);
                }

                @Override
                public void onScanFailed(int errorCode) {
                    //Log.i("Scan Failed", "Code:" + errorCode);
                    //super.onScanFailed(errorCode);
                }
            };
        }


    }

    public static ConnectSettingActivity getCon() {
        return conActivity;
    }

    public void startActivityConnect(String deviceName, String deviceAddress) {                 // 기기의 이름과 주소를 BLEConnectDialog(로딩Dialog) 에 전송해서 연결이 완료되면 로딩창을 끄는 방식
        Intent intent = new Intent(ConnectSettingActivity.this, BleConnectDialog.class);
        intent.putExtra("device_name", deviceName);
        intent.putExtra("device_address", deviceAddress);
        startActivity(intent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, R.string.error_bluetooth_off, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
//        else{
//                    Intent intent = new Intent(ConnectSettingActivity.this,DeviceCharacteristicActivity.class);
//                    startActivity(intent);
//        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {     // 퍼미션 허용 dialog를 띄웠다가 사용자가 허용, 취소 를 클릭했을때 동작함.
//        switch (requestCode) {
//            case PERMISSION_REQUEST_COARSE_LOCATION: {
//                try {
//                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                        //Log.i("Permission", "Granted");
//
//                        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//                        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                return;
//            }
//        }
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }


    long backKeyPressedTime;

    @Override
    public void onBackPressed() {

        if (MainActivity.getMain() != null) {
            super.onBackPressed();
            finish();
        } else {
            //1번째 백버튼 클릭
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                Toast.makeText(this, "뒤로가기 버튼을 두번 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
            }
             //2초 안에 2번째 클릭
//            //2번째 백버튼 클릭 (종료)
            else {
                AppFinish();
            }
        }
    }

    public void AppFinish() { // 앱 종료
//        finish();
//        System.exit(0);
//        android.os.Process.killProcess(android.os.Process.myPid());
        finishAffinity();
//        System.runFinalization();
        System.exit(0);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTING);
        intentFilter.addAction(BLEService.ACTION_GATT_BONDED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);

        return intentFilter;
    }


    public void setListViewSize(ListView myListView) {
        ListAdapter myListAdapter = myListView.getAdapter();
        if (myListAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int size = 0; size < myListAdapter.getCount(); size++) {
            View listItem = myListAdapter.getView(size, null, myListView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = myListView.getLayoutParams();
        params.height = totalHeight + (myListView.getDividerHeight() * (myListAdapter.getCount() - 1));
        myListView.setLayoutParams(params);

    }

    public static void WriteBleData(String var) {           //블루투스 데이터 전송
        //ASCII 를 HEX로 변환.
        //값은 16진수 Hex로 전송
//        byte[] value = {Byte.parseByte("00"), Byte.parseByte("00"), Byte.parseByte("00")};

        String start = "#";
//        var = "#T100CM01FTEV000010000000000000 0000000000V01S00220200917031223C32T0115M0100C6F43772;";
        String end = ";";
        int length = var.length();
        String strLength = String.valueOf(length);
        if (strLength.length() < 4) {
            strLength = "0" + strLength;
        }
        String LastValue = start + strLength + var;
        byte[] value = var.getBytes();


//        Log.d("value",String.valueOf(value));
        try {
            if (BLEService.getBleService() != null) {
                if (BLEService.getBleService().getConnectBLE()) {
                    mBleService.writeCharacteristic(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
//            Toast.makeText(getCon(), "연결된 기기가 정상적이지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    int savePosition = 77700;
    @Override
    public void onProductClicked(int position) {                // 기기가 추가되어있는 리사이클러뷰 목록 클릭 시 동작
        mLeDeviceListAdapter.clearAlltems();
        for(int i =0; i<allDeviceName.size();i++) {
            ConnectSettingAdapter.DataList data = new ConnectSettingAdapter.DataList();
            data.setDevice_Name(allDeviceName.get(i));
            data.setDevice_Address(allDeviceAddress.get(i));
            data.setDbDeviceName(aDeviceName);
            data.setDbDeviceAddress(aDeviceAddress);
            data.setScanDeviceName(scanDeviceName);
            if(i==position) {
                if (savePosition != position) {
                    data.setSetVisible(true);
                    savePosition = position;
                } else {
                    data.setSetVisible(false);
                    savePosition = 77000;
                }
            }else {
                data.setSetVisible(false);
            }

            mLeDeviceListAdapter.addItem(data);
            mLeDeviceListAdapter.notifyDataSetChanged();
        }
    }

    public class CalculationCRC32 {     // CRC =>  데이터를 전송할때 전송하려는 데이터가 처음부터 끝까지 잘 전송 되었는지 확인하는 데이터
                                                    // 앱 의뢰하는 쪽에서 전달해준대로 생성해서 전송하는 데이터 뒤에 붙임


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
            for (int i = 0; i < buffer.length; i++) {
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

    public void IntentBleDisConnectDialog() {
        Intent intent = new Intent(ConnectSettingActivity.this, BleDisConnectDialog.class);
        startActivity(intent);
    }




    IntentFilter intentFilter = new IntentFilter();
    WifiManager wifiManager;

    RecyclerView recyclerView;
    RecyclerView.Adapter mAdapter;

    public void scanWIFI() {                //와이파이 스캔 ( 수정후 사용안함)

        recyclerView=findViewById(R.id.rv_recyclerview);

        //권한에 대한 자동 허가 요청 및 설명
        AutoPermissions.Companion.loadAllPermissions(this,101);

        //Wifi Scna 관련
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        if (!success) Toast.makeText(ConnectSettingActivity.this, "Wifi Scan에 실패하였습니다." ,Toast.LENGTH_SHORT).show();
    }


    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {   // wifiManager.startScan(); 시  발동되는 메소드 ( 예제에서는 버튼을 누르면 startScan()을 했음. )

            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false); //스캔 성공 여부 값 반환
            if (success) {
                scanSuccess();
            } else {
                scanFailure();
            }
        }// onReceive()..
    };

    private void scanSuccess() {    // Wifi검색 성공
        List<android.net.wifi.ScanResult> results = wifiManager.getScanResults();
        for(int a = 0; a <results.size(); a++){
            String c = results.get(a).SSID;
            if(results.get(a).SSID.equals("")){
                results.remove(a);
            }
        }
        mAdapter=new MyAdapter(results);
        recyclerView.setAdapter(mAdapter);
    }

    private void scanFailure() {    // Wifi검색 실패
    }


    public void clickWifiScan(View view) {
//        boolean success = wifiManager.startScan();
//        if (!success) Toast.makeText(ConnectSettingActivity.this, "Wifi Scan에 실패하였습니다." ,Toast.LENGTH_SHORT).show();
    }// clickWifiScan()..

    //Permission에 관한 메소드
    @Override
    public void onDenied(int i, String[] strings) {
//        Toast.makeText(this, "onDenied~~", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGranted(int i, String[] strings) {
//        Toast.makeText(this, "onGranted~~", Toast.LENGTH_SHORT).show();
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
//    }


    public void sendWIFIstate(){

        try {
            term_id = pref.getString("term_id", "0000000000");
            user_id = pref.getString("ChargerCode", "0000000000000000000000");
            if (user_id.length() < 22) {
                int num = 22 - user_id.length();
                String space = " ";
                String spaceAll = "";
                for (int a = 0; a < num; a++) {
                    spaceAll = spaceAll + space;
                }
                user_id = user_id+spaceAll;
            }





            Log.d("user ID Length", String.valueOf(user_id.length()));
            String wifiName = pref.getString("wifiName", "");
            if (wifiName.length() < 30) {
                int num = 30 - wifiName.length();
                String space = " ";
                String spaceAll = "";
                for (int a = 0; a < num; a++) {
                    spaceAll = spaceAll + space;
                }
                wifiName = wifiName+spaceAll;
            }


            String wifiPassword = pref.getString("wifiPassword", "");
            if (wifiPassword.length() < 20) {
                int num = 20 - wifiPassword.length();
                String space = " ";
                String spaceAll = "";
                for (int a = 0; a < num; a++) {
                    spaceAll = spaceAll + space;
                }
                wifiPassword = wifiPassword+spaceAll;
            }



            String a = "0102A310CM02" + term_id + user_id + "V01" + wifiName + wifiPassword;
            //태그


            byte[] val = a.getBytes();

//
            CalculationCRC32 crc2 = new CalculationCRC32();
            long c = 0;
            c = crc2.update(val);
            String str16num = Integer.toHexString((int) c);
            Log.d("C", String.valueOf(str16num));


            ConnectSettingActivity.getCon().WriteBleData("#0102A310CM02" + term_id + user_id + "V001" + wifiName + wifiPassword + str16num + ";");
        }catch(Exception e){
            e.printStackTrace();
        }

        }
}
