package com.finetech.fineevapp;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;


import com.finetech.fineevapp.Bluetooth.BLEService;
import com.finetech.fineevapp.Bluetooth.ConnectSettingActivity;
import com.finetech.fineevapp.Main.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.GONE;

public class UserSettingActivity extends AppCompatActivity {

    Switch SwitchUserMode,SwitchFullMode,SwitchSmartMode, SwitchOver,SwitchLow;
    SeekBar seekBarVolt,seekBarTime;
    LinearLayout IbtnSetting;
    Button btnSave;

    LinearLayout btnClose;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    ConstraintLayout FullModeLayout,SmartModeLayout,VoltLayout,TimeLayout;

    Timer timer;
    boolean getDataState;
    String term_id,user_id;

    ConstraintLayout setTimeLayout;
    EditText edtTime;

    LinearLayout Full_Linear;
    Context mContext;
    float pressedX;

    boolean conCheck = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        TextViewSetting();
        mContext = getApplicationContext();

        Intent get =getIntent();
        getDataState = get.getBooleanExtra("getDataState",true);
        pref = getApplication().getSharedPreferences("info",MODE_PRIVATE);
        editor = pref.edit();

        setTimeLayout = findViewById(R.id.setTimeLayout);
        edtTime = findViewById(R.id.edtTime);


        FullModeLayout = findViewById(R.id.FullModeLayout);
        SmartModeLayout = findViewById(R.id.SmartModeLayout);
        SwitchOver = findViewById(R.id.OverValtageSW);
        SwitchLow = findViewById(R.id.LowValtageSW);
        VoltLayout = findViewById(R.id.VoltLayout);
        TimeLayout = findViewById(R.id.TimeLayout);

        SwitchUserMode = findViewById(R.id.SwitchUserMode);
        SwitchFullMode = findViewById(R.id.SwitchFullMode);
        SwitchSmartMode = findViewById(R.id.SwitchSmartMode);
        seekBarTime = findViewById(R.id.seekBarTime);
        seekBarVolt = findViewById(R.id.seekBarVolt);

        /**스위치를 켜고 끌때  다른 스위치들도 함께 동작해야함
         * 1. 사용자 모드 활성화시  모든 스위치 활성화
         * 2. (사용자 모드는 활성화) Full Mode 활성화시 Smart Mode 스위치를 제외한 seekBar 비활성화
         * 3. (사용자 모드는 활성화) Smar Mode 활성화시 Full Mode 스위치를 제외한 seekBar 비활성화
         * 4. 사용자 모드만 활성화 하고 Full mode 와 Smart mode 가 비활성화 상태여야 충전 전류, 충전시간 Seekbar 활성화.
         * 5. (새로운 수정사항) 충전시간은 항상 비활성화**/

        SwitchFullModeSetting();
        SwitchSmartModeSetting();
        SeekBarVoltSetting();
        SeekBarTimeSetting();
        SwitchUserModeSetting();
        FullModeLayout.setClickable(false);
        SwitchSmartMode.setClickable(false);

        seekBarTime.setProgress(7);
        seekBarTime.setEnabled(false);
        seekBarTime.setThumb(getResources().getDrawable(R.drawable.seekbar_circle_off));
        seekBarTime.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar_off));


        IbtnSetting =findViewById(R.id.IbtnSetting);
        IbtnSetting.setOnClickListener(n->{
            Intent intent = new Intent(UserSettingActivity.this, ConnectSettingActivity.class);
            startActivityForResult(intent,0);
//            finish();

        });

        btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(n->{
//            onBackPressed();
            finish();
        });

        SwitchOver = findViewById(R.id.OverValtageSW);
        SwitchOver.setChecked(false);
        SwitchOver.setEnabled(false);
        SwitchLow = findViewById(R.id.LowValtageSW);
        SwitchLow.setChecked(true);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==111){
            finish();
        }
    }



    @Override
    protected  void onResume() {
        super.onResume();

        Save();
//        timer = new Timer();
//        TimerTask tt= timertaskMaker();
//        timer.schedule(tt,0,1000);

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

                int Timer = Integer.parseInt(pref.getString("ChargingTime","0"));
                String TIME = "T0000";
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

                if(term_id== null){
                    term_id="0000000000";
                }
                if(user_id ==null){
                    user_id = "0000000000000000000000";
                }

                String a = "0084T200CM01"+term_id+user_id+"V02"+REQ+DATE+CURRENT+TIME+MODE;
//                    String a ="0084T100CM01FTEV00000145827E88BE59083D14A59V022S00120200920081708C032T0152M0100";
                byte[] val = a.getBytes();

//
                CalculationCRC32 crc2 = new CalculationCRC32();
                long c =0;
                c = crc2.update(val);
                String str16num = Integer.toHexString((int) c);
                Log.d("C", String.valueOf(str16num));

                term_id = pref.getString("term_id","0000000000");
                user_id =pref.getString("user_id","0000000000000000000000");

                ConnectSettingActivity.getCon().WriteBleData("#0084T200CM01"+term_id+user_id+"V02"+REQ+DATE+CURRENT+TIME+MODE+str16num+";");
                Log.d("SendData","#0084T300CM01"+term_id+user_id+"V02"+REQ+DATE+CURRENT+TIME+MODE+str16num+";");
//                ConnectSettingActivity.getCon().WriteBleData("#T200CM01"+term_id+user_id+"V02"+REQ+DATE+CURRENT+TIME+MODE+"C6F43772;");

            }
        };
        return addTask;
    }
    public void SwitchUserModeSetting(){

        if(pref.getString("UserMode","0").equals("0")){
            SwitchUserMode.setChecked(false);
        }else{
            SwitchUserMode.setChecked(true);
        }

        if(SwitchOver.isChecked()) {
            SwitchUserMode.setThumbDrawable(getResources().getDrawable(R.drawable.switch_thumb_on));
        }
        if(SwitchLow.isChecked()){
            SwitchUserMode.setThumbDrawable(getResources().getDrawable(R.drawable.switch_thumb_on));
        }
        if(SwitchUserMode.isChecked()){
            SwitchUserMode.setThumbDrawable(getResources().getDrawable(R.drawable.switch_thumb_on));
//            tvFull.setTextColor(Color.parseColor("#000000"));
            tvFull2.setTextColor(Color.parseColor("#555555"));
//            tvSmart.setTextColor(Color.parseColor("#000000"));
            tvSmart2.setTextColor(Color.parseColor("#555555"));
            tvSetTime.setTextColor(Color.parseColor("#000000"));
            tvSetVolt.setTextColor(Color.parseColor("#000000"));

//            SwitchFullMode.setClickable(true);
            SwitchSmartMode.setClickable(true);
            if(!SwitchFullMode.isChecked() && !SwitchSmartMode.isChecked()) {
                tvSetTime.setTextColor(Color.parseColor("#000000"));
                tvSetVolt.setTextColor(Color.parseColor("#000000"));
//                seekBarTime.setEnabled(true);
//                seekBarTime.setThumb(getResources().getDrawable(R.drawable.seekbar_circle));
//                seekBarTime.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar));
                seekBarVolt.setEnabled(true);
                seekBarVolt.setThumb(getResources().getDrawable(R.drawable.seekbar_circle));
                seekBarVolt.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar));

            }
//            TimeLayout.setVisibility(View.VISIBLE);
        }else{
            SwitchUserMode.setThumbDrawable(getResources().getDrawable(R.drawable.switch_thumb_off));
//            FullModeLayout.setVisibility(GONE);
//            SmartModeLayout.setVisibility(GONE);
//            TimeLayout.setVisibility(GONE);
            SwitchFullMode.setClickable(false);
            SwitchSmartMode.setClickable(false);

            seekBarTime.setProgress(7);
            seekBarTime.setEnabled(false);
            seekBarTime.setThumb(getResources().getDrawable(R.drawable.seekbar_circle_off));
            seekBarTime.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar_off));
            seekBarVolt.setProgress(5);
            seekBarVolt.setEnabled(false);
            seekBarVolt.setThumb(getResources().getDrawable(R.drawable.seekbar_circle_off));
            seekBarVolt.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar_off));
            tvSetTime.setTextColor(Color.parseColor("#AAAAAA"));
            tvSetVolt.setTextColor(Color.parseColor("#AAAAAA"));
            editor.putString("Mode","Normal");
            Save();

            tvFull.setTextColor(Color.parseColor("#AAAAAA"));
            tvFull2.setTextColor(Color.parseColor("#AAAAAA"));
            tvSmart.setTextColor(Color.parseColor("#AAAAAA"));
            tvSmart2.setTextColor(Color.parseColor("#AAAAAA"));
            tvSetTime.setTextColor(Color.parseColor("#AAAAAA"));
            tvSetVolt.setTextColor(Color.parseColor("#AAAAAA"));


            setTimeLayout.setVisibility(GONE);
            editor.putString("ChargingTime", "0");
            editor.commit();
            Save();
//            seekBarTime.setThumb(getResources().getDrawable(R.drawable.seekbar_circle_off));
//            seekBarTime.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar_off));
        }

        SwitchUserMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(SwitchUserMode.isChecked()){

                    editor.putString("UserMode","1");
                    Save();

                    SwitchUserMode.setThumbDrawable(getResources().getDrawable(R.drawable.switch_thumb_on));
//                    FullModeLayout.setVisibility(View.VISIBLE);
//                    SmartModeLayout.setVisibility(View.VISIBLE);
//                    TimeLayout.setVisibility(View.VISIBLE);

//                    tvFull.setTextColor(Color.parseColor("#000000"));
//                    tvFull2.setTextColor(Color.parseColor("#555555"));
//                    tvSmart.setTextColor(Color.parseColor("#000000"));
//                    tvSmart2.setTextColor(Color.parseColor("#555555"));
//                    tvSetTime.setTextColor(Color.parseColor("#000000"));
                    tvSetVolt.setTextColor(Color.parseColor("#000000"));


//                    SwitchFullMode.setClickable(true);
                    SwitchSmartMode.setClickable(true);
                    if(!SwitchSmartMode.isChecked() && !SwitchFullMode.isChecked()) {
//                        seekBarTime.setEnabled(true);
                        seekBarVolt.setEnabled(true);
                    }else{
                        seekBarTime.setEnabled(false);
                        seekBarVolt.setEnabled(false);
                    }
//                    seekBarTime.setThumb(getResources().getDrawable(R.drawable.seekbar_circle));
//                    seekBarTime.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar));
//                    seekBarTime.setProgress(5);
                    seekBarVolt.setThumb(getResources().getDrawable(R.drawable.seekbar_circle));
                    seekBarVolt.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar));
                    seekBarVolt.setProgress(5);
                    setTimeLayout.setVisibility(GONE);
                    editor.putString("ChargingTime", "0");
                    editor.commit();
                    Save();
                }else{
                    editor.putString("UserMode","0");
                    Save();
                    SwitchUserMode.setThumbDrawable(getResources().getDrawable(R.drawable.switch_thumb_off));

//                    FullModeLayout.setVisibility(GONE);
//                    SmartModeLayout.setVisibility(GONE);
//                    TimeLayout.setVisibility(GONE);

                    tvFull.setTextColor(Color.parseColor("#AAAAAA"));
                    tvFull2.setTextColor(Color.parseColor("#AAAAAA"));
                    tvSmart.setTextColor(Color.parseColor("#AAAAAA"));
                    tvSmart2.setTextColor(Color.parseColor("#AAAAAA"));
                    tvSetTime.setTextColor(Color.parseColor("#AAAAAA"));
                    tvSetVolt.setTextColor(Color.parseColor("#AAAAAA"));

                    SwitchFullMode.setClickable(false);
                    SwitchSmartMode.setClickable(false);

                    SwitchFullMode.setChecked(false);
                    SwitchSmartMode.setChecked(false);
                    seekBarTime.setEnabled(false);
//                    seekBarTime.setProgress(5);
                    seekBarVolt.setEnabled(false);
                    seekBarVolt.setProgress(5);
                    setTimeLayout.setVisibility(GONE);
                    editor.putString("ChargingTime", "0");
                    editor.commit();
                    Save();
                    seekBarTime.setThumb(getResources().getDrawable(R.drawable.seekbar_circle_off));
                    seekBarTime.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar_off));
                    seekBarVolt.setThumb(getResources().getDrawable(R.drawable.seekbar_circle_off));
                    seekBarVolt.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar_off));
                }

            }
        });

    }
    public void SwitchFullModeSetting(){
        if(pref.getString("SmartMode","0").equals("0")){
            SwitchSmartMode.setChecked(false);
        }else{
//            SwitchSmartMode.setChecked(true);
        }
        if(pref.getString("FullMode","0").equals("0")){
            SwitchFullMode.setChecked(false);
        }else{
//            SwitchFullMode.setChecked(true);
        }

        if(SwitchFullMode.isChecked()){
            tvSetTime.setTextColor(Color.parseColor("#AAAAAA"));
            tvSetVolt.setTextColor(Color.parseColor("#AAAAAA"));
            SwitchFullMode.setThumbDrawable(getResources().getDrawable(R.drawable.switch_thumb_on));
//            seekBarTime.setThumb(getResources().getDrawable(R.drawable.seekbar_circle_off));
//            seekBarTime.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar_off));
//            seekBarTime.setEnabled(false);
            seekBarVolt.setThumb(getResources().getDrawable(R.drawable.seekbar_circle_off));
            seekBarVolt.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar_off));
            seekBarVolt.setEnabled(false);
        }else{
            SwitchFullMode.setThumbDrawable(getResources().getDrawable(R.drawable.switch_thumb_off));
            if(!SwitchSmartMode.isChecked()){
                tvSetTime.setTextColor(Color.parseColor("#000000"));
                tvSetVolt.setTextColor(Color.parseColor("#000000"));
//                seekBarTime.setThumb(getResources().getDrawable(R.drawable.seekbar_circle));
//                seekBarTime.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar));

                seekBarVolt.setThumb(getResources().getDrawable(R.drawable.seekbar_circle));
                seekBarVolt.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar));
            }
        }

        SwitchFullMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(SwitchFullMode.isChecked()){
                    tvSetTime.setTextColor(Color.parseColor("#AAAAAA"));
                    tvSetVolt.setTextColor(Color.parseColor("#AAAAAA"));
//                    SwitchFullMode.setThumbDrawable(getResources().getDrawable(R.drawable.switch_thumb_on));
                    SwitchSmartMode.setChecked(false);
                    editor.putString("FullMode","1");
                    editor.putString("SmartMode","0");
                    editor.putString("Mode","Full");
                    seekBarTime.setEnabled(false);
//                    seekBarTime.setProgress(5);
                    seekBarVolt.setEnabled(false);
                    seekBarVolt.setProgress(5);
                    setTimeLayout.setVisibility(GONE);
                    editor.putString("ChargingTime", "0");
                    editor.commit();
                    Save();
//                    seekBarTime.setThumb(getResources().getDrawable(R.drawable.seekbar_circle_off));
//                    seekBarTime.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar_off));
                    seekBarVolt.setThumb(getResources().getDrawable(R.drawable.seekbar_circle_off));
                    seekBarVolt.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar_off));
                    Save();
                }else{
                    SwitchFullMode.setThumbDrawable(getResources().getDrawable(R.drawable.switch_thumb_off));
                    editor.putString("FullMode","0");

                    if(!SwitchSmartMode.isChecked()){
                        tvSetTime.setTextColor(Color.parseColor("#000000"));
                        tvSetVolt.setTextColor(Color.parseColor("#000000"));
                        editor.putString("Mode","Normal");
//                        seekBarTime.setEnabled(true);
//                        seekBarTime.setThumb(getResources().getDrawable(R.drawable.seekbar_circle));
//                        seekBarTime.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar));
//                        seekBarTime.setProgress(5);
                        seekBarVolt.setEnabled(true);
                        seekBarVolt.setThumb(getResources().getDrawable(R.drawable.seekbar_circle));
                        seekBarVolt.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar));
                        seekBarVolt.setProgress(5);
                        setTimeLayout.setVisibility(GONE);
                        editor.putString("ChargingTime", "0");
                        editor.commit();
                        Save();

                    }else{
                        seekBarTime.setEnabled(false);
                        seekBarVolt.setEnabled(false);
                    }
                    Save();
                }

            }
        });

    }
    public void SwitchSmartModeSetting(){
        if(pref.getString("SmartMode","0").equals("0")){
            SwitchSmartMode.setChecked(false);
        }else{
//            SwitchSmartMode.setChecked(true);
        }


        if(SwitchSmartMode.isChecked()){
//                SwitchSmartMode.setThumbDrawable(getResources().getDrawable(R.drawable.switch_thumb_on));
                tvSetTime.setTextColor(Color.parseColor("#AAAAAA"));
                tvSetVolt.setTextColor(Color.parseColor("#AAAAAA"));
                seekBarTime.setThumb(getResources().getDrawable(R.drawable.seekbar_circle_off));
                seekBarTime.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar_off));
                seekBarTime.setEnabled(false);
            seekBarVolt.setThumb(getResources().getDrawable(R.drawable.seekbar_circle_off));
            seekBarVolt.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar_off));
            seekBarVolt.setEnabled(false);
        }else{
            SwitchSmartMode.setThumbDrawable(getResources().getDrawable(R.drawable.switch_thumb_off));
            if(!SwitchFullMode.isChecked()) {
                tvSetTime.setTextColor(Color.parseColor("#000000"));
                tvSetVolt.setTextColor(Color.parseColor("#000000"));
                seekBarTime.setThumb(getResources().getDrawable(R.drawable.seekbar_circle));
                seekBarTime.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar));
                seekBarTime.setEnabled(false);
                seekBarVolt.setThumb(getResources().getDrawable(R.drawable.seekbar_circle));
                seekBarVolt.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar));
            }
        }

        SwitchSmartMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(SwitchSmartMode.isChecked()){
                    tvSetTime.setTextColor(Color.parseColor("#AAAAAA"));
                    tvSetVolt.setTextColor(Color.parseColor("#AAAAAA"));
//                    SwitchSmartMode.setThumbDrawable(getResources().getDrawable(R.drawable.switch_thumb_on));
                    SwitchFullMode.setChecked(false);
                    seekBarTime.setEnabled(false);
                    seekBarTime.setProgress(7);
                    seekBarVolt.setEnabled(false);
                    seekBarVolt.setProgress(5);
                    setTimeLayout.setVisibility(GONE);
                    editor.putString("ChargingTime", "0");
                    editor.commit();
                    Save();
                    seekBarTime.setThumb(getResources().getDrawable(R.drawable.seekbar_circle_off));
                    seekBarTime.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar_off));
                    seekBarVolt.setThumb(getResources().getDrawable(R.drawable.seekbar_circle_off));
                    seekBarVolt.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar_off));
                    editor.putString("SmartMode","1");
                    editor.putString("FullMode","0");
                    editor.putString("Mode","Smart");
                    Save();

                }else{
                    SwitchSmartMode.setThumbDrawable(getResources().getDrawable(R.drawable.switch_thumb_off));
                    editor.putString("SmartMode","0");
                    if(!SwitchFullMode.isChecked()){
                        tvSetTime.setTextColor(Color.parseColor("#000000"));
                        tvSetVolt.setTextColor(Color.parseColor("#000000"));
                        editor.putString("Mode","Normal");
                        seekBarTime.setEnabled(false);
//                        seekBarTime.setThumb(getResources().getDrawable(R.drawable.seekbar_circle));
//                        seekBarTime.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar));
                        seekBarTime.setProgress(7);
                        seekBarVolt.setEnabled(true);
                        seekBarVolt.setThumb(getResources().getDrawable(R.drawable.seekbar_circle));
                        seekBarVolt.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_bar));
                        seekBarVolt.setProgress(5);
                        setTimeLayout.setVisibility(GONE);
                        editor.putString("ChargingTime", "0");
                        editor.commit();
                        Save();
                    }else{
                        seekBarTime.setEnabled(false);
                        seekBarVolt.setEnabled(false);
                    }
                    Save();

                }

            }
        });

    }
    public void SeekBarVoltSetting(){
        String strVolt = pref.getString("ChargingVolt","0");
        int intVolt = Integer.parseInt(strVolt);

        seekBarVolt.setProgress(intVolt);
        seekBarVolt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editor.putString("ChargingVolt",String.valueOf(seekBarVolt.getProgress()));
                editor.commit();
                Save();
            }
        });
    }
    public void SeekBarTimeSetting(){
        String time = pref.getString("ChargingTime", "0");
        if(time.equals("")){
            time = "0";
        }
        int intTime = Integer.parseInt(time);

        seekBarTime.setProgress(intTime-1);
        seekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(seekBarTime.getProgress()==0){

                    setTimeLayout.setVisibility(View.VISIBLE);
                    edtTime.setText(pref.getString("ChargingTime",""));
                    edtTime.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if(!edtTime.getText().toString().equals("")) {
                                editor.putString("ChargingTime", edtTime.getText().toString());
                                editor.commit();
                                Save();
                            }
                        }
                    });
                }else{
                    setTimeLayout.setVisibility(GONE);
                    editor.putString("ChargingTime", String.valueOf(seekBarTime.getProgress() + 1));
                    editor.commit();
                    Save();
                }
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
//        if(timer!=null) {
//            timer.cancel();
//        }
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


    TextView tvFull, tvFull2,tvSmart,tvSmart2,tvSetTime,tvSetVolt;
    public void TextViewSetting(){
        tvFull = findViewById(R.id.tvFullmode);
        tvFull2 = findViewById(R.id.tvFull2);
        tvSmart = findViewById(R.id.tvSmartmode);
        tvSmart2 = findViewById(R.id.tvSmart2);
        tvSetTime = findViewById(R.id.tvSetTime);
        tvSetVolt = findViewById(R.id.tvSetVolt);

    }
public void Save(){

    editor.commit();


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

        int Timer = Integer.parseInt(pref.getString("ChargingTime", "0"));
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
                TIME = "T0500";
                break;
            case 6:
                TIME = "T0600";
                break;
        }
        if(Timer>6){
            String TimerS=String.valueOf(Timer);
            if(TimerS.length()==1){
                TimerS = "0"+TimerS;
            }
            TIME= "T"+TimerS+"00";
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

        String a = "0084T200CM01" + term_id + user_id + "V02" + REQ + DATE + CURRENT + TIME + MODE;
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

        ConnectSettingActivity.getCon().WriteBleData("#0084T200CM01" + term_id + user_id + "V02" + REQ + DATE + CURRENT + TIME + MODE + str16num + ";");
        Log.d("SendData", "#0084T200CM01" + term_id + user_id + "V02" + REQ + DATE + CURRENT + TIME + MODE + str16num + ";");
//                ConnectSettingActivity.getCon().WriteBleData("#T200CM01"+term_id+user_id+"V02"+REQ+DATE+CURRENT+TIME+MODE+"C6F43772;");
    }


    }
            @Override
                public void onBackPressed(){
                    super.onBackPressed();
                overridePendingTransition(R.anim.right_in, R.anim.right_out);
            }

            public void onBackpressedLeft(){
                super.onBackPressed();
                overridePendingTransition(R.anim.left_in, R.anim.left_out);
            }
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                conCheck=true;
            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                editor.putString("disconnect_check","1");
                editor.commit();
                if(conCheck=true) {
//                    Toast.makeText(UserSettingActivity.this, "충전기 연결이 해제되었습니다.", Toast.LENGTH_SHORT).show();
                    conCheck= false;
                }
//                Intent intent1 = new Intent(UserSettingActivity.this,MainActivity.class);
//                startActivity(intent1);
//                onBackPressed();
                if(MainActivity.getMain()!=null) {
                    MainActivity.getMain().runAutoConnectTimer();
                }
                finish();

//                }, 10000);
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            } else if (BLEService.ACTION_GATT_WRITE.equals(action)) {

            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {


            }
        }
    };

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
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_GATT_WRITE);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
