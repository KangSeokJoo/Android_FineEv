package com.jinasoft.fineevapp.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.jinasoft.fineevapp.ElecDataBase.ElecDataBase;
import com.jinasoft.fineevapp.R;

import java.util.ArrayList;


public class BleConnectDialog extends AppCompatActivity {

    private BLEService mBleService;

    private int skipIndex = 0;
    private boolean mConnected = false;
    private boolean mConnecting = false;

    String device_name;
    String device_address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_ble_connect);


        Intent gattServiceIntent = new Intent(this, BLEService.class);  //블루투스 서비스 연결 ( background에서도 블루투스 연결이 끊기지 않게 함)
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);      // 서비스 bind

        ImageView imageView = findViewById(R.id.gif_image);
        Glide.with(this).load(R.drawable.loadinggif).into(imageView);           //Glide 사용 -> Glide : 안드로이드에서 가장 많이 사용되는 이미지 핸들링 라이브러리

        Intent intent = getIntent();
        device_name = intent.getStringExtra("device_name");
        device_address = intent.getStringExtra("device_address");



        SharedPreferences pref = getApplication().getSharedPreferences("info",MODE_PRIVATE);     //SharedPreference (앱내 저장)
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("device_name",device_name);
        editor.putString("device_address",device_address);
        editor.commit();

        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {                       //postDelayed => 지정된 시간 뒤에 실행 (1초 : 1000)
            @Override
            public void run() {
                mBleService.connect(device_address,false);
            }     //BLE 연결
        }, 500);                                                    //0.5초 뒤에 실행

        Log.d("name",device_name);
        Log.d("address",device_address);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());                            // BLE 상태를 확인하는 BroadcastReceiver
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));


        Cursor cursor = ElecDataBase.getDbHelper().getDevice();                                 //sqlite 사용하기(데이터 불러오기)    ElecDataBase -> ElecDataBase 확인
            if (cursor.moveToFirst()) {
                do {
                    aDeviceAddress.add(cursor.getString(cursor.getColumnIndex("device_address")));
                    aDeviceName.add(cursor.getString(cursor.getColumnIndex("device_name")));


                } while (cursor.moveToNext());
            }

    }
        ArrayList<String> aDeviceAddress  = new ArrayList<>();
        ArrayList<String> aDeviceName  = new ArrayList<>();

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {                 //브로드캐스트 리시버
            final String action = intent.getAction();
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {                             //기기 상태가 연결됨 일때
                skipIndex = 0;
                mConnected = true;
                mConnecting = false;
                updateConnectionState(R.string.connected);                                                  //상태를 연결됨으로 변경
                Toast.makeText(context, "충전기가 연결되었습니다.", Toast.LENGTH_SHORT).show();
                if(!aDeviceAddress.contains(device_address)) {                  //DB 에 현재 연결된 기기가 없으면 저장
                    ElecDataBase.getDbHelper().SetDevice(device_name, device_address);
                }
                unregisterReceiver(mGattUpdateReceiver);  // 브로드캐스트 리시버 해제

                finish();


//                updateConnectionState(R.string.connected);
//                invalidateOptionsMenu();
            } else if (BLEService.ACTION_GATT_BONDED.equals(action)) {    //기기가 페어리됨
//                updateConnectionState(R.string.disconnected);
//                invalidateOptionsMenu();

            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {  //기기 연결이 해제됨
                skipIndex = 0;
                mConnected = false;
                mConnecting = false;
//                updateConnectionState(R.string.disconnected);

//                invalidateOptionsMenu();
                Toast.makeText(context, "연결 실패.", Toast.LENGTH_SHORT).show();
                ElecDataBase.getDbHelper().DeleteDevice(device_name,device_address);        // DB에서 기기를 삭제함
                finish();

                //                clearUI();
            } else if (BLEService.ACTION_GATT_CONNECTING.equals(action)) {    //기기가 연결중
                mConnecting = true;
                mConnected = false;
//                updateConnectionState(R.string.connecting);
//                invalidateOptionsMenu();

            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {  //기기에서 정보를 받아오는중
                // Show all the supported services and characteristics on the user interface.
//                displayGattServices(mBleService.getSupportedGattServices());
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) { // 데이터 송수신 가능
//                displayData(intent.getStringExtra(BLEService.EXTRA_DATA));
            }
        }
    };

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {    //기기의 상태가 변경됨
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
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mBleService.isBonded(device_address)) {
//                    mConnectionState.setText(getResources().getString(resourceId) + " - Bonded");
                } else {
//                    mConnectionState.setText(resourceId);
                }
            }
        });
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent e){    //다이얼로그가 생성됐을때 주변터치로 취소되는 것을 막는다.
        Rect dialogBounds = new Rect();
        getWindow().getDecorView().getHitRect(dialogBounds);
        if(!dialogBounds.contains((int)e.getX(),(int) e.getY())){
            return false;
        }
        return super.dispatchTouchEvent(e);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private static IntentFilter makeGattUpdateIntentFilter() {     //BroadcastReceiver 에 Intent filter 추가
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTING);
        intentFilter.addAction(BLEService.ACTION_GATT_BONDED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);

        return intentFilter;
    }

}
