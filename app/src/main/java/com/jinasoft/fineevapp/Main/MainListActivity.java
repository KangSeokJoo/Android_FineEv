package com.jinasoft.fineevapp.Main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.jinasoft.fineevapp.Bluetooth.BLEService;
import com.jinasoft.fineevapp.Bluetooth.ConnectSettingActivity;
import com.jinasoft.fineevapp.DialogShare;
import com.jinasoft.fineevapp.ElecDataBase.ElecDataBase;
import com.jinasoft.fineevapp.R;
import com.jinasoft.fineevapp.UserSettingActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainListActivity extends AppCompatActivity implements MainListAdapter.MyRecyclerViewClickListener,MainListAdapter.MyRecyclerViewLongClickListener {

    //메인 액티비티에서 충전 목록 리스트를 클릭했을때 보여지는 full 리스트 액티비티

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

    MainListAdapter adapter;

    RecyclerView main_recyclerView;

    TextView tvCarInfo;
    Button btnShare;

    Timer timer;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String term_id,user_id;
    boolean getDataState;


    LinearLayout btnBack;

    boolean DoubleItemClick= false;
    int savePosition = 77700;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        pref = getSharedPreferences("info",MODE_PRIVATE);
        editor = pref.edit();

        btnBack = findViewById(R.id.btnClose);
        btnBack.setOnClickListener(n->{
            onBackPressed();
        });

        main_recyclerView = findViewById(R.id.main_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        main_recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new MainListAdapter();
        adapter.setOnClickListener(this);
        adapter.setLongListener(this);
        main_recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(main_recyclerView);                        //리사이클러뷰 아이템 좌우로 슬라이드 해서 삭제하기


        Intent get = getIntent();
        String carInfo = get.getStringExtra("CarInfo");
        getDataState = get.getBooleanExtra("getDataState",true);
        tvCarInfo = findViewById(R.id.tvCarInfo);
        tvCarInfo.setText(carInfo);

        AddItemToAdapter();

        btnShare = findViewById(R.id.btnShare);
        btnShare.setOnClickListener(n->{
            Intent intent = new Intent(MainListActivity.this, DialogShare.class);
            startActivity(intent);
        });

        findViewById(R.id.IbtnSetting).setOnClickListener(n->{
            Intent intent = new Intent(MainListActivity.this, UserSettingActivity.class);
            startActivity(intent);
        });

//        sendData();         //페이지 정보 전송
    }


    @Override
    protected void onResume(){
        super.onResume();

//        if(timer!=null){
//            timer.cancel();
//        }
//
//        timer = new Timer();
//        TimerTask tt= timertaskMaker();
//        timer.schedule(tt,0,1000);

    }
    public void sendData(){
        Log.d("확인","몹시필요");
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


//                ConnectSettingActivity.getCon().WriteBleData("#T400CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE+"C6F43772;");
            ConnectSettingActivity.getCon().WriteBleData("#0084T400CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE + str16num + ";");
            Log.d("SendData", "#0084T100CM01" + term_id + user_id + "V01" + REQ + DATE + CURRENT + TIME + MODE + str16num + ";");
        }
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



                String a = "0084T400CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE;
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


//                ConnectSettingActivity.getCon().WriteBleData("#T400CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE+"C6F43772;");
                ConnectSettingActivity.getCon().WriteBleData("#0084T400CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE+str16num+";");
                Log.d("SendData","#0084T100CM01"+term_id+user_id+"V01"+REQ+DATE+CURRENT+TIME+MODE+str16num+";");

            }
        };
        return addTask;
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
                setting_mode  .add(cursor.getString(cursor.getColumnIndex("setting_mode")));
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
        Collections.reverse(tempArray);

        for(int i =0; i<charging_dateArray.size(); i++) {
            MainListAdapter.DataList data = new MainListAdapter.DataList();
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

            String battery = pref.getString("Battery","160");
            data.setMode(setting_mode.get(i) +"/"+battery);
            String a = charging_start_time.get(i).substring(3,5);
            data.setStartTime(charging_start_time.get(i).substring(0,2)+" : "+a);

            data.setChargingTime(carTime[0]+"시간 " +carTime[1]+"분");

            data.setFinishTime(charging_end_time.get(i).substring(0,2)+" : "+charging_end_time.get(i).substring(3,5));
            data.setMileage(distance.get(i));
            data.setV(contact_pressure.get(i));
            data.setA(electric_current.get(i));
            data.setC(tempArray.get(i));
            data.setSetVisible(false);

            adapter.addItem(data);
            adapter.notifyDataSetChanged();
        }
    }

    //날짜 요일로 바꾸기
    public static String getDateDay(String date, String dateType) throws Exception {

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
    protected void onPause(){
        super.onPause();
//        if(timer!=null) {
//            timer.cancel();
//        }
    }

    @Override
    public void onProductClicked(int position) {
        adapter.clearAlltems();
        if(adapter.getItemCount()!=0) {
            int count = adapter.getItemCount() - 1;
        }
        for(int i =0; i<charging_dateArray.size(); i++) {
            MainListAdapter.DataList data = new MainListAdapter.DataList();
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


            String battery = pref.getString("Battery","160");
            data.setMode(setting_mode.get(i) +"/"+battery);
            data.setStartTime(charging_start_time.get(i).substring(0,2)+" : "+charging_start_time.get(i).substring(3,5));

            data.setChargingTime(carTime[0]+"시간 " +carTime[1]+"분");

            data.setFinishTime(charging_end_time.get(i).substring(0,2)+" : "+charging_end_time.get(i).substring(3,5));
            data.setMileage(distance.get(i)+"km");
            data.setV(contact_pressure.get(i));
            data.setA(electric_current.get(i));
            data.setC(tempArray.get(i));
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
            adapter.addItem(data);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLongClick(int position) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(MainListActivity.this);
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



    // 슬라이드해서 목록 삭제
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


//            AlertDialog.Builder builder = new AlertDialog.Builder(MainListActivity.this);
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
}
