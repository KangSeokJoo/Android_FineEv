package com.finetech.fineevapp.Bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.finetech.fineevapp.R;

public class BleDisConnectDialog extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_ble_dis_connect);

        ImageView imageView = findViewById(R.id.gif_image);
        Glide.with(this).load(R.drawable.loadinggif).into(imageView);     //Glide

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());        //브로드캐스트 리시버 추가
//        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        Handler mHandler = new Handler(Looper.getMainLooper());         //5초뒤에 실행되는 postDelayed 핸들러
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 5000);

    }
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {             //브로드 캐스트 리시버에서 상태가 바뀌었을때 동작할 작업 생성
            final String action = intent.getAction();
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {

            } else if (BLEService.ACTION_GATT_BONDED.equals(action)) {
//                updateConnectionState(R.string.disconnected);
//                invalidateOptionsMenu();

            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                unregisterReceiver(mGattUpdateReceiver);
                finish();

            } else if (BLEService.ACTION_GATT_CONNECTING.equals(action)) {

            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
//                displayGattServices(mBleService.getSupportedGattServices());
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
//                displayData(intent.getStringExtra(BLEService.EXTRA_DATA));
            }
        }
    };
    private static IntentFilter makeGattUpdateIntentFilter() {      // 인텐트 필터 추가
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTING);
        intentFilter.addAction(BLEService.ACTION_GATT_BONDED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);

        return intentFilter;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e){
        Rect dialogBounds = new Rect();
        getWindow().getDecorView().getHitRect(dialogBounds);
        if(!dialogBounds.contains((int)e.getX(),(int) e.getY())){
            return false;
        }
        return super.dispatchTouchEvent(e);
    }
}
