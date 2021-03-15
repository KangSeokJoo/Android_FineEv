/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.finetech.fineevapp.Bluetooth;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BLEService extends Service {
    private final static String TAG = BLEService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private Handler mHandler = null;
    public boolean serviceStatus = false;
    private BluetoothDevice mDevice = null;
    private boolean mBond = false;

    private final static int NOTIFICATION_ID = 267;
    public static BLEService bleService;


    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =                                  //nrf toolbox UART 사용 가능
            "com.microchip.bluetoothsmartdiscover.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.microchip.bluetoothsmartdiscover.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_CONNECTING =
            "com.microchip.bluetoothsmartdiscover.ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.microchip.bluetoothsmartdiscover.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.microchip.bluetoothsmartdiscover.ACTION_DATA_AVAILABLE";
    public final static String ACTION_GATT_WRITE =
            "com.microchip.bluetoothsmartdiscover.ACTION_GATT_WRITE";
    public final static String EXTRA_DATA =
            "com.microchip.bluetoothsmartdiscover.EXTRA_DATA";
    public final static String ACTION_GATT_BONDED =
            "com.microchip.bluetoothsmartmesh.ACTION_GATT_BONDED";

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                //if (mDevice != null) {
                    //if (mDevice.getBondState() != BluetoothDevice.BOND_NONE) {
                        mBond = false;
                        broadcastUpdate(ACTION_GATT_BONDED);

                        if (mConnectionState == STATE_CONNECTED) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {}

                            disconnect();

                            //mBluetoothGatt.discoverServices();
                        }

                    //}
                //}
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        bleService = this;      //bleService 에 현재 서비스 context 삽입
    }
    public static BLEService getBleService(){
        return bleService;
    }
    public boolean getConnectionState(String address){
        if(mBluetoothDeviceAddress==null){
            return false;
        }else {
            if (mBluetoothDeviceAddress.equals(address) && mConnectionState == STATE_CONNECTED) {
                return true;
            } else {
                return false;
            }
        }

    }

    public boolean getConnectBLE(){             //블루투스의 연결 상태 확인
        if(mConnectionState == STATE_CONNECTED){
            return true;
        }else{
            return false;

        }
    }


    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {  // 블루투스의 상태가 변경 되었을때 발생하는 callback 메서드
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTED) { // 연결됨

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = STATE_CONNECTED;
                    broadcastUpdate(intentAction);                          //브로드캐스트리시버의 상태 업데이트
                    //Log.i(TAG, "Connected to GATT server.");
                    // Attempts to discover services after successful connection.
                    //Log.i(TAG, "Attempting to start service discovery:" +
                    //        mBluetoothGatt.discoverServices());

                    if (mDevice != null) {
                        if (mDevice.getBondState() == BluetoothDevice.BOND_NONE && mBond == true) {
                            mDevice.createBond();
                        } else {
                            mBluetoothGatt.discoverServices();
                        }

                    }

                } else {
                    //Log.i(TAG, "onConnectionStateChange: " + status + " : " + newState);
                    mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            connect(mBluetoothDeviceAddress, mBond);
                        }
                    }, 500);
                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) { // 해제됨
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                //Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
                //close(); //JAY
            } else if (newState == BluetoothProfile.STATE_CONNECTING) { //연결중
                intentAction = ACTION_GATT_CONNECTING;
                mConnectionState = STATE_CONNECTING;
                //Log.i(TAG, "Connecting to GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                //Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

                final byte[] data = characteristic.getValue();
                String str = null;
                try {
                    str = new String(data,"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Log.e("result",str);

            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {                     //
                if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                    broadcastUpdate(ACTION_GATT_WRITE, characteristic);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();

            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data) {
                    stringBuilder.append(String.format("%02X ", byteChar));
                }
                //Log.d(TAG, String.format("Received data:)" + new String(data) + "\n" + stringBuilder.toString()));

                if (BLEGattAttributes.lookup(characteristic.getService().getUuid().toString(), "Unknown Service").matches("Device Information")) {
                    intent.putExtra(EXTRA_DATA, stringBuilder.toString() + "\n" + new String(data));

                }

                else if (BLEGattAttributes.lookup(characteristic.getService().getUuid().toString(), "Unknown Service").matches("Battery"))
                    intent.putExtra(EXTRA_DATA, stringBuilder.toString() + "\n" + Integer.valueOf(stringBuilder.toString().trim(), 16) +  "%");
                else {
//                    intent.putExtra(EXTRA_DATA, stringBuilder.toString());
                    try {
                        String str = new String(data,"UTF-8");
//                        Log.d("result",str);
                        intent.putExtra(EXTRA_DATA, str);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BLEService getService() {
            return BLEService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        serviceStatus = true;
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        return mBinder;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        startMyOwnForeground();

        close();
        if (mHandler != null) {
            mHandler.removeCallbacks(null);
        }

        if (bluetoothReceiver != null) {
            unregisterReceiver(bluetoothReceiver);
        }
        serviceStatus = false;
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {                   //ble manager 초기화
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                //Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            //Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public void unBond(final String address) {
        try {
            if (mBluetoothAdapter != null) {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                if (device.getBondState() != BluetoothDevice.BOND_NONE) {
                    (device.getClass().getMethod("removeBond", (Class[]) null)).invoke(device, (Object[]) null);
                }
            }
        } catch (Exception e) {}//JAY
    }

    public boolean isBonded(final String address) {         //페어링이 되어있는 기기인지 확인함
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.getRemoteDevice(address).getBondState() == BluetoothDevice.BOND_NONE) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }
    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address, boolean bond) {  // 기기 연결하기
        if (mBluetoothAdapter == null || address == null) {             //기기의 주소가 null 이면 건너뛰기
            //Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        mBond = bond;
        mGattCallback.onConnectionStateChange(mBluetoothGatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTING);   //JAY

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            //Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        mDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (mDevice == null) {
            Log.d(TAG, "Device not found.  Unable to connect.");
            Toast.makeText(getApplication(), "연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = mDevice.connectGatt(this, false, mGattCallback);
        //Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;



        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {                          //기기 연결 해제
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            //Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        SharedPreferences pref = getApplication().getSharedPreferences("info",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("ClickDisconnect","1");
        editor.commit();
        mBluetoothGatt.disconnect();
    }

    public String getDeviceAddress() {
        return mBluetoothDeviceAddress;
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {           //블루투스 통신 통로 닫기
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            //Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicWrite(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {    //BLE 데이터 송신
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            //Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        //Log.i(TAG, Integer.toString(characteristic.getWriteType()));
        characteristic.setValue(value);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            //Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(BLEGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));

        if (descriptor != null) {
            if (enabled) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            } else {
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }
            mBluetoothGatt.writeDescriptor(descriptor);
        }

    }

    /**
     * Enables or disables indication on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicIndication(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            //Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(BLEGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        if (descriptor != null) {
            if (enabled) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            } else {
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }
            mBluetoothGatt.writeDescriptor(descriptor);
        }

    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){                        // 블루투스 서비스가 실행되고 있을때 앱이 백그라운드로 넘어가면 사용자에게 서비스가 실행되고 있음을 알려야한다. ( notification 을 띄움)
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }
    /**
     * Stops the service as a foreground service
     */
    private void stopForegroundService(){
        // when the activity rebinds to the service, remove the notification and stop the foreground service
        // on devices running Android 8.0 (Oreo) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        } else {
            cancelNotification();
        }
    }
    private void cancelNotification() {
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
    }

}
