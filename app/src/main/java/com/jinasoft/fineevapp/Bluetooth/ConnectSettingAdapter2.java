package com.jinasoft.fineevapp.Bluetooth;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jinasoft.fineevapp.ElecDataBase.ElecDataBase;
import com.jinasoft.fineevapp.R;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

class ConnectSettingAdapter2 extends RecyclerView.Adapter<ConnectSettingAdapter2.ItemViewHolder> {



    public MyRecyclerViewClickListener mListener;
    public AddMemoClickListener AddMemoListener;
    private ArrayList<DataList> listData = new ArrayList<>();
    private static Context context;


    static ArrayList<String> allDeviceName = new ArrayList<>();
    static ArrayList<String> DeviceChk = new ArrayList<>();
    static ArrayList<String> mScanDeviceArray = new ArrayList<>();

        private static BLEService mBleService;
    static ProgressDialog progressDialog;


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_connect_setting_device, parent, false);
        return new ItemViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.onBind(listData.get(position));

        final int pos = position;
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
                    holder.MainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mListener.onProductClicked(pos);
                }catch (Exception e){
                    Toast.makeText(context, "주변에 없는 충전기 입니다.", Toast.LENGTH_SHORT).show();
                }
            }

        });

    }
    public void clearAlltems(){
        allDeviceName.clear();
        mScanDeviceArray.clear();
        listData.clear();
        DeviceChk.clear();
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    void addItem(DataList data) {
        if(!allDeviceName.contains(data.getDevice_Name()))
            listData.add(data);

        allDeviceName.clear();
        Cursor cursor = ElecDataBase.getDbHelper().getDevice();
        if (cursor.moveToFirst()) {
            do {
                if(cursor.getString(cursor.getColumnIndex("device_name")).equals(data.getDevice_Name())) {
                    allDeviceName.add(cursor.getString(cursor.getColumnIndex("device_name")));
                }
            }while (cursor.moveToNext());
        }
    }

    public void deleteItem(DataList data){
        listData.remove(data);
    }

    public void addScanDevice(String name){
        mScanDeviceArray.add(name);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder  {

        private DataList data;

        TextView deviceName1;
        TextView deviceName2;
        ImageView conState;
        TextView deviceDelete;
        LinearLayout PasswordLayout;
        LinearLayout MainLayout;
        Button btnConnect;

        EditText edtPassword;


        static ArrayList<String> aDeviceName = new ArrayList<>();
        static ArrayList<String> aDeviceAddress = new ArrayList<>();

        static ArrayList<String> mScanDeviceName = new ArrayList<>();
        static ArrayList<String> nScanDeviceAddress = new ArrayList<>();

//        private String FoodType;

    int c = 0;


        ItemViewHolder(View view) {
            super(view);



           deviceName1 = view.findViewById(R.id.charge1);
           deviceName2 = view.findViewById(R.id.bleName);
           conState = view.findViewById(R.id.idLinked);
           deviceDelete = view.findViewById(R.id.tvDel);
           PasswordLayout = view.findViewById(R.id.passwordLayout);
           MainLayout = view.findViewById(R.id.MainLayout);
           btnConnect = view.findViewById(R.id.btnConnect);
           edtPassword = view.findViewById(R.id.edtPassword);


        }
        public void onBind(DataList data) {
            this.data = data;

            final String deviceName = data.getDevice_Name();
            final String deviceAddress= data.getDevice_Address();
            aDeviceName = data.getDbDeviceName();
            mScanDeviceName = data.getScanDeviceName();
            nScanDeviceAddress= data.getmScanDeviceAddress();

            if (deviceName != null && deviceName.length() > 0) {
               deviceName1.setText("충전기"+(getAdapterPosition()+1));
                deviceName2.setText(deviceName);
            }else{
                deviceName2.setText(R.string.unknown_device);
            }
            if(BLEService.getBleService()!=null) {
                if (BLEService.getBleService().getConnectionState(deviceAddress)) {
                    conState.setBackground(context.getResources().getDrawable(R.drawable.icon_linked));
                } else {
                    conState.setBackground(context.getResources().getDrawable(R.drawable.icon_unlinked));
                }
            }else{
                conState.setBackground(context.getResources().getDrawable(R.drawable.icon_unlinked));
            }

            Cursor cursor = ElecDataBase.getDbHelper().getDevice();
            if (cursor.moveToFirst()) {
                do {
                    DeviceChk.add(cursor.getString(cursor.getColumnIndex("device_name")));
                }while (cursor.moveToNext());
            }

            if(DeviceChk.contains(deviceName)) {
                deviceDelete.setText("삭제");
            }else {
                deviceDelete.setText("추가");
            }

            MainLayout.setOnClickListener(n->{
                if(mScanDeviceName.contains(deviceName) || mScanDeviceArray.contains(deviceName)) {
                    if(c == 0 ){
                        if(BLEService.getBleService().getConnectionState(deviceAddress)){

                        }else {
                            PasswordLayout.setVisibility(View.VISIBLE);
                        }
                        c=1;
                    }else{
                        PasswordLayout.setVisibility(View.GONE);
                        c=0;
                    }
                }else{
                    Toast.makeText(context, "주변에 없는 충전기입니다.", Toast.LENGTH_SHORT).show();
                }
            });

            deviceDelete.setOnClickListener(n->{
                if(deviceDelete.getText().toString().equals("삭제")){

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("저장된 충전기 삭제");
                    builder.setMessage("저장된 충전기를 삭제할까요?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            if (BLEService.getBleService().getConnectBLE()) {
                                ConnectSettingActivity.getCon().IntentBleDisConnectDialog();
                            }

                            if(BLEService.getBleService().getConnectBLE()) {
                                if(BLEService.getBleService().getDeviceAddress().equals(deviceAddress)) {
                                    BLEService.getBleService().disconnect();
//                                    BLEService.getBleService().close();
                                    SharedPreferences pref = context.getSharedPreferences("info",MODE_PRIVATE);
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("ClickDisconnect","0");
                                    editor.commit();
                                }
                            }
                            aDeviceAddress.remove(deviceAddress);
                            aDeviceName.remove(deviceName);

//                            conState.setBackground(context.getResources().getDrawable(R.drawable.icon_unlinked));
                            ElecDataBase.getDbHelper().DeleteDevice(deviceName,deviceAddress);
//                            Toast.makeText(context, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
//                            if(BLEService.getBleService()!=null) {

//                            }
                            deviceDelete.setText("추가");
                            DeviceChk.clear();

                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    builder.create().show();
                }else{

//                    Toast.makeText(context, "저장 완료", Toast.LENGTH_SHORT).show();
//                    ElecDataBase.getDbHelper().SetDevice(deviceName,deviceAddress);
//                    deviceDelete.setText("삭제");

                    if(mScanDeviceName.contains(deviceName) || mScanDeviceArray.contains(deviceName)) {
                        if(c == 0 ){
                            if(BLEService.getBleService().getConnectionState(deviceAddress)){

                            }else {
                                PasswordLayout.setVisibility(View.VISIBLE);
                            }
                            c=1;
                        }else{
                            PasswordLayout.setVisibility(View.GONE);
                            c=0;
                        }
                    }else{
                        Toast.makeText(context, "주변에 없는 충전기입니다.", Toast.LENGTH_SHORT).show();
                    }

                }

            });


            btnConnect.setOnClickListener(n->{

                if(edtPassword.getText().toString().equals("1234")){
                if (!BLEService.getBleService().getConnectBLE()) {

                    ConnectSettingActivity.getCon().startActivityConnect(deviceName, deviceAddress);
                    PasswordLayout.setVisibility(View.GONE);

                } else {
                    Toast.makeText(context, "이미 연결된 충전기가 있습니다", Toast.LENGTH_SHORT).show();
                    PasswordLayout.setVisibility(View.GONE);

                }
                }else{
                    Toast.makeText(context, "비밀번호가 올바르지 않습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }

            });


            if(data.getVisible() ==true){
                PasswordLayout.setVisibility(View.VISIBLE);
            }else{
                PasswordLayout.setVisibility(View.GONE);
            }

        }
    }

    public void setOnClickListener(MyRecyclerViewClickListener listener) {
        mListener = listener;

    }
    public interface  MyRecyclerViewClickListener {

        void onProductClicked(int position);
    }
    public void setAddMemoListener(AddMemoClickListener listener) {
        AddMemoListener = listener;

    }
    public interface  AddMemoClickListener {

        void AddMemoClicked(int position);
    }

    public static class DataList {
        private String Device_Name;
        private String Device_Address;
        private  ArrayList<String> aDeviceName;
        private  ArrayList<String> aDeviceAddress;

        private  ArrayList<String> mScanDeviceName;
        private  ArrayList<String> mScanDeviceAddress;
        private boolean setVisible;


        public void setDevice_Name(String name) {Device_Name = name;}
        public void setDevice_Address(String address) {Device_Address = address;}
        public void setDbDeviceName(ArrayList<String> DeviceName) {aDeviceName =DeviceName;}
        public void setDbDeviceAddress(ArrayList<String> DeviceAddress) {aDeviceAddress =DeviceAddress;}
        public void setScanDeviceName(ArrayList<String> scanDeviceName) {mScanDeviceName =scanDeviceName;}
        public void setScanDeviceAddress(ArrayList<String> scanDeviceAddress) {mScanDeviceAddress =scanDeviceAddress;}
        public void setSetVisible(boolean visible){setVisible = visible;}

        public String getDevice_Name() {return Device_Name;}
        public String getDevice_Address() {return  Device_Address;}
        public ArrayList<String> getDbDeviceName() {return aDeviceName;}
        public ArrayList<String> getDbDeviceAddress() {return aDeviceAddress;}
        public ArrayList<String> getScanDeviceName(){return mScanDeviceName;}
        public ArrayList<String> getmScanDeviceAddress(){return mScanDeviceAddress;}
        public boolean getVisible(){return setVisible;}
    }


}
