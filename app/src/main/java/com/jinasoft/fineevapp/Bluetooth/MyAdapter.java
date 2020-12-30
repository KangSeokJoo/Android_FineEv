package com.jinasoft.fineevapp.Bluetooth;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.jinasoft.fineevapp.R;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private List<ScanResult> items;
    private static Context context;
    public MyAdapter(List<ScanResult> items){
        this.items=items;
    }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_connect_setting_ap , parent, false);
        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
//        ScanResult item=items.get(position);
        holder.setItem(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {


        public TextView tvWifiName;
        public LinearLayout  passwordLayout;
        public Button  btnConnect;
        public EditText  edtPassword;
        public ConstraintLayout mainLayout;
        int c = 0;




        public MyViewHolder(View itemView) {
            super(itemView);
            tvWifiName=itemView.findViewById(R.id.wifi1);
            passwordLayout = itemView.findViewById(R.id.passwordLayout);
            btnConnect = itemView.findViewById(R.id.btnConnect);
            edtPassword = itemView.findViewById(R.id.edtPassword);
            mainLayout = itemView.findViewById(R.id.main_layout);
        }
        public void setItem(ScanResult item){
            String a = item.SSID;

            tvWifiName.setText(item.SSID);
            mainLayout.setOnClickListener(n->{
                if(c == 0 ){
                    passwordLayout.setVisibility(View.VISIBLE);
                    c=1;
                }else{
                    passwordLayout.setVisibility(View.GONE);
                    c=0;
                }
            });
            btnConnect.setOnClickListener(n->{
                if(BLEService.getBleService().getConnectBLE()) {
                    String password = edtPassword.getText().toString();
                    SharedPreferences pref = context.getSharedPreferences("info", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("wifiName", item.SSID);
                    editor.putString("wifiPassword", password);
                    editor.commit();
                    ConnectSettingActivity.getCon().sendWIFIstate();

                    passwordLayout.setVisibility(View.GONE);
                    Toast.makeText(context, "WiFi 설정 완료", Toast.LENGTH_SHORT).show();

                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(itemView.getWindowToken(), 0);
                }else{
                    Toast.makeText(context, "충전기를 먼저 연결해주세요.", Toast.LENGTH_SHORT).show();
                }
            });



        }
    }
}