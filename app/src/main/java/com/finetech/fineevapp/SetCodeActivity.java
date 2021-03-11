package com.finetech.fineevapp;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.finetech.fineevapp.Bluetooth.BLEService;
import com.finetech.fineevapp.Bluetooth.ConnectSettingActivity;
import com.finetech.fineevapp.Main.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class SetCodeActivity extends AppCompatActivity {

    // 2020.12.23 수정본에서 부터 사용되지 않는 기능

    LinearLayout btnClose;
    TextView btnSetting;
    EditText edtCode;

    Timer timer;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    boolean getDataState;

    String term_id,user_id;

    Button btnChkCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_code);

        pref = getSharedPreferences("info",MODE_PRIVATE);
        editor = pref.edit();

        Intent get = getIntent();
        getDataState = get.getBooleanExtra("getDataState",true);


        btnChkCode = findViewById(R.id.btnChkCode);
        btnChkCode.setOnClickListener(n->{
            String code = pref.getString("ChargerCode","");
            if(!code.equals("")) {
                Toast.makeText(this, "현재 충전기 코드는"+code.replace(" ","")+"입니다.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "충전기 코드가 설정되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        btnClose = findViewById(R.id.btnClose);

        btnClose.setOnClickListener(n->{
            onBackPressed();
        });

        edtCode = findViewById(R.id.edtCode);
//        edtCode.setFilters(new InputFilter[] {filter});
//        edtCode.setMaxL
        edtCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(edtCode.getText().toString().length()==22){
//                    String str  = edtCode.getText().toString().substring( 0, edtCode.getText().toString().length() - 1 );
//                    edtCode.setText(str);
                    Toast.makeText(SetCodeActivity.this, "영문, 숫자 포함 22자리 이내로 입력해주세요", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        btnSetting = findViewById(R.id.IbtnSetting);
        btnSetting.setOnClickListener(n->{
            if(!edtCode.getText().toString().matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) {
                if (!edtCode.getText().toString().equals("")) {
                    SharedPreferences pref = getApplication().getSharedPreferences("info", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();

                    String Code = edtCode.getText().toString();
                    if (Code.length() < 22) {
                        int num = 22 - Code.length();
                        String space = " ";
                        String spaceAll = "";
                        for (int a = 0; a < num; a++) {
                            spaceAll = spaceAll + space;
                        }
                        Code = Code + spaceAll;
                    }
                    editor.putString("ChargerCode", Code);


                    editor.commit();
                    Toast.makeText(this, "코드 설정 완료", Toast.LENGTH_SHORT).show();


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

                        term_id = pref.getString("term_id", "0000000000");
                        user_id = pref.getString("ChargerCode", "0000000000000000000000");


//                    String full = "0084T100CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE+"C6F43772;";
                        String a = "0084T100CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE;
//                    String a ="0084T100CM01FTEV00000145827E88BE59083D14A59V012S00120200920081708C032T0152M0100";
                        byte[] val = a.getBytes();

//
                        CalculationCRC32 crc2 = new CalculationCRC32();
                        long c = 0;
                        c = crc2.update(val);
                        String str16num = Integer.toHexString((int) c);
                        Log.d("C", String.valueOf(str16num));


                        ConnectSettingActivity.getCon().WriteBleData("#0084T220CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE + str16num + ";");
                        Log.d("SendData", "#0084T100CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE + str16num + ";");
                    }


//                finish();
                    Intent intent = new Intent(SetCodeActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "코드를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "영문, 숫자 포함 22자리 이내로 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected  void onResume() {
        super.onResume();

//        timer = new Timer();
//        TimerTask tt= timertaskMaker();
//        timer.schedule(tt,0,1000);


            if(BLEService.getBleService().getConnectBLE()) {
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
//                    String full = "0084T100CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE+"C6F43772;";
                String a = "0084T100CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE;
//                    String a ="0084T100CM01FTEV00000145827E88BE59083D14A59V012S00120200920081708C032T0152M0100";
                byte[] val = a.getBytes();

//
                CalculationCRC32 crc2 = new CalculationCRC32();
                long c = 0;
                c = crc2.update(val);
                String str16num = Integer.toHexString((int) c);
                Log.d("C", String.valueOf(str16num));


                term_id = pref.getString("term_id", "0000000000");
                user_id = pref.getString("ChargerCode", "0000000000000000000000");   //설정된 코드


                ConnectSettingActivity.getCon().WriteBleData("#0084T220CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE + str16num + ";");
                Log.d("SendData", "#0084T100CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE + str16num + ";");
            }
    }


    @Override
    protected void onPause() {
        super.onPause();

//        timer.cancel();
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
                        TIME = "T0400";
                        break;
                    case 6:
                        TIME = "T0500";
                        break;
                }

                String UserMode = pref.getString("UserMode","0");
                String FullMode = pref.getString("FullMode","0");
                String SmartMode = pref.getString("SmartMode","0");

                String MODE = "M0"+UserMode+FullMode+SmartMode;

                if(term_id== null){
                    term_id="0000000000";
                }
                if(user_id ==null){
                    user_id = "0000000000000000000000";
                }
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


                term_id = pref.getString("term_id","0000000000");
                user_id =pref.getString("user_id","0000000000000000000000");


                ConnectSettingActivity.getCon().WriteBleData("#0084T220CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE+str16num+";");
                Log.d("SendData","#0084T100CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE+str16num+";");


            }
        };
        return addTask;
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


    protected InputFilter filter= new InputFilter() {

        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {

            Pattern ps = Pattern.compile("^[a-zA-Z0-9]+$");
            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }

    };
}
