package com.jinasoft.fineevapp.Main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;


import com.jinasoft.fineevapp.R;

import java.util.ArrayList;

import static android.view.View.GONE;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ItemViewHolder> {


    public MyRecyclerViewClickListener mListener;
    public MyRecyclerViewLongClickListener LongListener;
    private ArrayList<DataList> listData = new ArrayList<>();
    private static Context context;


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_recycler, parent, false);
        return new ItemViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.onBind(listData.get(position));

        final int pos = position;
        holder.item_ConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onProductClicked(pos);
            }
        });
            holder.item_ConstraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    LongListener.onLongClick(pos);
                    return false;
                }
            });

    }

    public void removeData(int position){
        listData.remove(position);
    }

    public void clearAlltems() {
        listData.clear();
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    void addItem(DataList data) {
        listData.add(data);
    }

    public void deleteItem(DataList data) {
        listData.remove(data);
    }


    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        private DataList data;

        int a =1;
        ConstraintLayout item_ConstraintLayout;
        TextView tvDate, tvUseTime, tvPower;
        ImageView ImageBatteryState;

        TextView tvMenuDate;
        TextView tvCharge2;
        TextView tvMode2;
        TextView tvStartTime, tvChargingTime, tvFinishTime;
        TextView tvMileage;
        TextView tvA, tvV, tvC;


        LinearLayout MoreInfoLayout;


        ItemViewHolder(View view) {
            super(view);

            item_ConstraintLayout = view.findViewById(R.id.item_contLayout);
            tvDate = view.findViewById(R.id.tvDate);
            tvUseTime = view.findViewById(R.id.useTime);
            tvPower = view.findViewById(R.id.tvPower);
            ImageBatteryState = view.findViewById(R.id.ImageBatteryState);

            tvMenuDate = view.findViewById(R.id.tvMenuDate);
            tvCharge2 = view.findViewById(R.id.tvCharge2);
            tvMode2 = view.findViewById(R.id.tvMode2);
            tvStartTime = view.findViewById(R.id.tvStartTime);
            tvChargingTime = view.findViewById(R.id.tvChargingTime);
            tvFinishTime = view.findViewById(R.id.tvFinishTime);

            tvMileage = view.findViewById(R.id.tvMileage);
            tvA = view.findViewById(R.id.tvA);
            tvV = view.findViewById(R.id.tvV);
            tvC = view.findViewById(R.id.tvC);

            MoreInfoLayout = view.findViewById(R.id.MoreInfoLayout);

        }

        public void onBind(DataList data) {
            this.data = data;


            MoreInfoLayout.setVisibility(GONE);
            tvDate.setText(data.getDate());
            tvUseTime.setText(data.getUseTime());
            tvPower.setText(data.getPower()+"kWh");
            tvCharge2.setText(data.getCharge()+"kWh");
            tvMode2.setText(data.getMode()+"kWh");
            tvStartTime.setText(data.getStartTime());
            tvChargingTime.setText(data.getChargingTime());
            tvFinishTime.setText(data.getFinishTime());
            tvMileage.setText(data.getMileage());
            tvV.setText(data.getV()+"V");
            tvA.setText(data.getA()+"A");
            tvC.setText(data.getC()+"˚C");

            String[] Battery = data.getCharge().split("\\.");
            int bat = Integer.parseInt(Battery[0]);

            if(bat < 3 ){
                ImageBatteryState.setBackground(context.getResources().getDrawable(R.drawable.icon_btgage1));
            }else if(bat < 7){
                ImageBatteryState.setBackground(context.getResources().getDrawable(R.drawable.icon_btgage2));
            }else if(bat < 12) {
                ImageBatteryState.setBackground(context.getResources().getDrawable(R.drawable.icon_btgage3));
            }else if(bat < 17) {
                ImageBatteryState.setBackground(context.getResources().getDrawable(R.drawable.icon_btgage4));
            }else if(bat < 22) {
                ImageBatteryState.setBackground(context.getResources().getDrawable(R.drawable.icon_btgage5));
            }else if(bat < 27) {
                ImageBatteryState.setBackground(context.getResources().getDrawable(R.drawable.icon_btgage6));
            }else if(bat < 40) {
                ImageBatteryState.setBackground(context.getResources().getDrawable(R.drawable.icon_btgage7));
            }else{
                ImageBatteryState.setBackground(context.getResources().getDrawable(R.drawable.icon_btgage8));
            }
            if(data.getCharge().equals("0.00")){
                ImageBatteryState.setBackground(context.getResources().getDrawable(R.drawable.icon_btgage_back));
            }

        }

    }



    public void setOnClickListener(MyRecyclerViewClickListener listener) {
        mListener = listener;

    }
    public interface MyRecyclerViewClickListener {

        void onProductClicked(int position);
    }

    public void setLongListener(MyRecyclerViewLongClickListener listener){
        LongListener = listener;
    }
    public interface MyRecyclerViewLongClickListener{
        void onLongClick(int position);
    }


    public static class DataList {
        private String Date;
        private String UseTime;
        private String Power;
        private String BatteryState;
        private String Charge;
        private String Mode;
        private String StartTime;
        private String ChargingTime;
        private String FinishTime;
        private String Mileage;
        private String V, A, C;

        public void setDate(String date) {
            Date = date;
        }
        public void setUseTime(String useTime) {
            UseTime = useTime;
        }
        public void setPower(String power) {
            Power = power;
        }
        public void setBatteryState(String batteryState) {
            BatteryState = batteryState;
        }
        public void setCharge(String charge) {
            Charge = charge;
        }
        public void setMode(String mode) {
            Mode = mode;
        }
        public void setStartTime(String startTime) {
            StartTime = startTime;
        }
        public void setChargingTime(String chargingTime) {
            ChargingTime = chargingTime;
        }
        public void setFinishTime(String finishTime) {
            FinishTime = finishTime;
        }
        public void setMileage(String mileage) {
            Mileage = mileage;
        }
        public void setV(String v) {
            V = v;
        }
        public void setA(String a) {
            A = a;
        }
        public void setC(String c) {
            C = c;
        }

        public String getDate() {
            return Date;
        }
        public String getUseTime(){
            return UseTime;
        }
        public String getPower(){
            return Power;
        }
        public String getBatteryState(){return BatteryState;}
        public String getCharge(){return Charge;}
        public String getMode(){return Mode;}
        public String getStartTime(){return StartTime;}
        public String getChargingTime(){return ChargingTime;}
        public String getFinishTime(){return FinishTime;}
        public String getMileage(){return Mileage;}
        public String getV(){return V; }
        public String getA(){return A;}
        public String getC(){return C;}
    }
}
