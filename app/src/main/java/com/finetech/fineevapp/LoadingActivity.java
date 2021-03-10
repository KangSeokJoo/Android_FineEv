package com.finetech.fineevapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

//import com.finetech.fineevapp.Login.LoginActivity; 태그0
//import com.finetech.fineevapp.Login.SessionCallback;
//import com.nhn.android.naverlogin.OAuthLogin;
//import com.nhn.android.naverlogin.data.OAuthLoginState;
import com.finetech.fineevapp.Main.MainActivity;

public class LoadingActivity extends AppCompatActivity {
//    SessionCallback sessionCallback = new SessionCallback(); 태그1

    Context context;
    Handler handler = new Handler();
    Runnable r = new Runnable() {
        @Override
        public void run() {

//                Intent intent = new Intent(LoadingActivity.this, ConnectSettingActivity.class);   태그2 ~~
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
//                        Log.e("확인2","쓰레드 살아있음");
//
//
//                        if (sessionCallback.kakaoLoginOn == true){
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(context, R.string.KakaoLoginSuccess, Toast.LENGTH_LONG).show();
//                                }
//                            });
                            Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                            startActivity(intent);
//                        }else {
//                            Intent intent = new Intent(LoadingActivity.this, LoginActivity.class);
//                            startActivity(intent);
//                            finish();
//                        }
//~~ 태그2 싹다 풀어야함
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
//        sessionCallback.requestMe(); 태그3
//        context = this;
//        SharedPreferences pref = getSharedPreferences("Sign", MODE_PRIVATE);
////
//        LoginActivity.mOAuthLoginInstance = OAuthLogin.getInstance();
//        LoginActivity.mOAuthLoginInstance.init(getApplicationContext(), LoginActivity.OAUTH_CLIENT_ID, LoginActivity.OAUTH_CLIENT_SECRET, LoginActivity.OAUTH_CLIENT_NAME);
//        String accessToken = LoginActivity.mOAuthLoginInstance.getAccessToken(getApplicationContext());
//        if (accessToken != null && OAuthLoginState.NEED_LOGIN.OK.equals(LoginActivity.mOAuthLoginInstance.getState(getApplicationContext()))) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(context, R.string.NaverLoginSuccess, Toast.LENGTH_LONG).show();
//                }
//            });
//            Intent intent = new Intent(context, MainActivity.class);
//            startActivity(intent);
//            Log.e("확인",""+accessToken);
//        }
//        else if (pref.getBoolean("isLogin", false) == true){
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
//        } //태그3
    }

    @Override
    protected  void onResume() {
        super.onResume();
        handler.postDelayed(r,500);
    }
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(r);
    }



}
