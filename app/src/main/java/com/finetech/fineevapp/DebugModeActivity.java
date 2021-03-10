package com.finetech.fineevapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.finetech.fineevapp.Bluetooth.BLEService;
import com.finetech.fineevapp.Bluetooth.ConnectSettingActivity;
import com.finetech.fineevapp.Main.DialogShareDebug;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DebugModeActivity extends AppCompatActivity {

    //들어오는 데이터를 텍스트롤 보여줌

    LinearLayout btnClose;

    Button btnShare,btnSendDebug;

    TextView tvDebug;


    boolean getDataState;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_mode);

        pref = getApplication().getSharedPreferences("info",MODE_PRIVATE);
        editor = pref.edit();

        Intent get = getIntent();
        getDataState = get.getBooleanExtra("getDataState",false);

        tvDebug = (TextView) findViewById(R.id.tvDebug);
        DebugText="";

        btnClose= findViewById(R.id.btnClose);
        btnClose.setOnClickListener(n->{
            onBackPressed();
        });

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        Intent gattServiceIntent = new Intent(this, BLEService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        mCharacteristicIndex = 0; //intent.getIntExtra(EXTRAS_CHARACTERISTIC_INDEX, 0);
        mServiceIndex =3; // + 2;


        if(BLEService.getBleService().getConnectBLE()) {
            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toggleNotify(true);
                }
            }, 1000);
        }else{
            tvDebug.setText("연결된 기기 없음");
        }

        btnShare = findViewById(R.id.btnShare);
        btnShare.setOnClickListener(n->{

            Intent intent = new Intent(DebugModeActivity.this,DialogShareDebug.class);
            intent.putExtra("DebugText",tvDebug.getText().toString());
            startActivity(intent);

        });

        sendData();

        btnSendDebug = findViewById(R.id.btnSendDebug);
        btnSendDebug.setOnClickListener(n->{
            sendDebugData();
        });

    }

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
    String temp;
    String set_current;
    String set_timer;
    String set_mode;
    String error;
    String ver;
    String req;
    String v_code;

    String DebugText;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBleService = ((BLEService.LocalBinder) service).getService();
            if (!mBleService.initialize()) {
                //Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
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
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                SetResult();
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            } else if (BLEService.ACTION_GATT_WRITE.equals(action)) {
//                ((Button) findViewById(R.id.characteristic_write_button)).setTextColor(Color.argb(0xFF, 0x00, 0x70, 0x00));
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                result = intent.getStringExtra(BLEService.EXTRA_DATA);
//                ((TextView) findViewById(R.id.characteristic_read)).setText(intent.getStringExtra(BLEService.EXTRA_DATA));
//                Log.d("result",result);


                // result 자르기
            try {
                rx_mode = result.substring(5, 8);
                term_id = result.substring(8, 18);
                user_id = result.substring(18, 40);
                start = result.substring(40, 54);
                end = result.substring(54, 68);
                elapse = result.substring(68, 74);
                count = result.substring(74, 78);
                status = result.substring(78, 82);
                volt = result.substring(82, 88);
                currunt = result.substring(88, 94);
                power = result.substring(94, 100);
                energy = result.substring(100, 106);
                temp = result.substring(106, 112);
                set_current = result.substring(112, 118);
                set_timer = result.substring(118, 123);
                set_mode = result.substring(123, 128);
                error = result.substring(128, 133);
                ver = result.substring(133, 137);
                req = result.substring(137, 141);
                v_code = result.substring(141, 150);

                Log.d("rx_mode", rx_mode);
                Log.d("term_id", term_id);
                Log.d("user_id", user_id);
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


                DebugText += "\n\n"+result + "\n" + "rx_mode : " + rx_mode + "\n" + "term_id : " + term_id + "\n" + "user_id : " + user_id + "\n" + "start : " + start + "\n" + "end : " + end + "\n" +
                        "elapse : " + elapse + "\n" + "count : " + count + "\n" +
                        "status : " + status + "\n" + "volt : " + volt + "\n" + "currunt : " + currunt + "\n" + "power : " + power + "\n" +
                        "energy : " + energy + "\n" + "temp : " + temp + "\n" + "set_current : " + set_current + "\n" + "set_timer : " + set_timer + "\n" + "set_mode : " + set_mode + "\n" +
                        "error : " + error + "\n" + "ver : " + ver + "\n" + "req : " + req + "\n" + "v_code : " + v_code;

                if(DebugText!=null) {
                    tvDebug.setText(DebugText);
                }


            }catch (Exception e){
                e.printStackTrace();
            }
            }
        }
    };

    private void SetResult() {
        setResult(RESULT_DISCONNECT, new Intent());
        finish();

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
        mNotifyStatus = isChecked;
        if (isChecked) {
            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                mBleService.setCharacteristicIndication(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), true);
            } else {
                mBleService.setCharacteristicNotification(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), true);
            }
//            ((TextView) findViewById(R.id.characteristic_notify)).setText("Notify/Indicate: " + "Yes");
        }
        else{
            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                mBleService.setCharacteristicIndication(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), false);
            } else {
                mBleService.setCharacteristicNotification(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), false);
            }
//            ((TextView) findViewById(R.id.characteristic_notify)).setText("Notify/Indicate: " + "No");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

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



        public void sendData(){
            if(BLEService.getBleService().getConnectBLE()) {
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
                // 2개로 나눠놨고 , T00 이 충전 시간 설정 , 1자리면 T01 , 10자리면 T10 -> 일단 T00으로 (Cont)보냄 , 밑에 UI가 생기는데
                // 그 뒤 자리 퍼센트라고 생각하면됌 01~ 99% 배터리 잔량
                // 현재는 T0000으로 보내면됌됌
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


                // 태그 SETMODE M3100 , M3110, M3111 으로 보내는건데
                // 사용자 설정이 저,과전압 활성시시 3/ 저전압만꺼지면 off 2/ 과전압만 꺼지면 1/ 다 꺼지면 0 (현재는 과전압 때문에 못함)
                // M3100 , M2100, M1000 이런식으로
                // 그 뒤에 숫자는 사용자 모드만 켜지면 1, Full모드까지 켜지면 11 스마트 모드까지 켜지면 111
                String UserMode = pref.getString("UserMode", "0");
                String FullMode = pref.getString("FullMode", "0");
                String SmartMode = pref.getString("SmartMode", "0");

                String MODE = "M0" + UserMode + FullMode + SmartMode;


                String a = "0084T400CM01" + term_id + user_id + "V02" + REQ + DATE + CURRENT + TIME + MODE;
//                    String a ="0084T100CM01FTEV00000145827E88BE59083D14A59V022S00120200920081708C032T0152M0100";
                byte[] val = a.getBytes();

//
                CalculationCRC32 crc2 = new CalculationCRC32();
                long c = 0;
                c = crc2.update(val);
                String str16num = Integer.toHexString((int) c);
                Log.d("C", String.valueOf(str16num));


                term_id = pref.getString("term_id", "0000000000");
                user_id = pref.getString("ChargerCode", "0000000000000000000000");


//                CM01 -> B000 ~ B120
//                B000~ B120 배터리 설정에서 설정한 값 불러오는거거//                ConnectSettingActivity.getCon().WriteBleData("#T400CM01"+term_id+user_id+"V02"+REQ+DATE+CURRENT+TIME+MODE+"C6F43772;");
                ConnectSettingActivity.getCon().WriteBleData("#0084T001CM01" + term_id + user_id + "V02" + REQ + DATE + CURRENT + TIME + MODE + str16num + ";");
                Log.d("SendData", "#0084T100CM01" + term_id + user_id + "V02" + REQ + DATE + CURRENT + TIME + MODE + str16num + ";");
                //태그
            }
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

        int countDebug;
    String packCount = "0000";
    public void sendDebugData(){
        if(countDebug<10000){
            if (countDebug>=0 && countDebug < 10){
                packCount = "000"+countDebug;
            }else if(countDebug >= 10 && countDebug <100){
                packCount = "00"+countDebug;
            }else if(countDebug >=100 && countDebug< 1000){
                packCount = "0"+countDebug;
            }else{
                packCount = String.valueOf(countDebug);
            }
            String Data = "";
            for(int a = 0; a<1000; a++) {
                int random = (int) (Math.random() * 10);
                Data += String.valueOf(random);
            }


            String message = "#1020D001V02" + packCount + Data;

          if(BLEService.getBleService().getConnectBLE()){

              String a = "1020D001V02" + packCount + Data;
              byte[] val = a.getBytes();
              CalculationCRC32 crc2 = new CalculationCRC32();
              long c =0;
              c = crc2.update(val);
              String str16num = Integer.toHexString((int) c);
              Log.d("C", String.valueOf(str16num));

              ConnectSettingActivity.getCon().WriteBleData(message+str16num+";");
              Log.d("DataLength", message+str16num+";");

          }

        }else{
            countDebug =0;
        }
        countDebug+=1;
        //for  문으로 1000바이트 랜덤 값으로 삽입입
    }

}
