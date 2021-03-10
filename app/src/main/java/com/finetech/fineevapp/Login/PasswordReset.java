//package com.finetech.fineevapp.Login;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import com.finetech.fineevapp.R;
//
//public class PasswordReset extends AppCompatActivity {
//
//    EditText edit_email, edit_pass, edit_pass_ok, edit_beforepass;
//    Button sub;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_password_reset);
//
//        edit_email = (EditText)findViewById(R.id.Pass_EDT1);
//        edit_pass = (EditText)findViewById(R.id.Pass_EDT3);
//        edit_pass_ok = (EditText)findViewById(R.id.Pass_EDT4);
//        edit_beforepass = (EditText)findViewById(R.id.Pass_EDT2);
//
//        sub = (Button)findViewById(R.id.Pass_BTNSubmit);
//
//        sub.setOnClickListener(v -> {
//            String email, beforepass, pass, pass_ok;
//
//            email = edit_email.getText().toString();
//            beforepass = edit_beforepass.getText().toString();
//            pass = edit_pass.getText().toString();
//            pass_ok = edit_pass_ok.getText().toString();
//
//            SharedPreferences pref = getSharedPreferences("Sign", MODE_PRIVATE);
//            SharedPreferences.Editor editor = pref.edit();
//            boolean a = false;
//            boolean b = false;
//            boolean c = false;
//
//            if (!email.equals(pref.getString("email",""))){
//                Toast.makeText(this, R.string.Password_Reset_EmailPail, Toast.LENGTH_SHORT).show();
//            }else {
//                a = true;
//            }
//            if (!beforepass.equals(pref.getString("pass","")) || beforepass == "" || beforepass == null){
//                Toast.makeText(this, R.string.Password_Reset_BeforePail, Toast.LENGTH_SHORT).show();
//            }else {
//                b = true;
//            }
//            if (!pass.equals(pass_ok)){
//                Toast.makeText(this, R.string.Password_Reset_NewPass, Toast.LENGTH_SHORT).show();
//            }else {
//                c = true;
//            }
//            if (a == true && b == true && c == true){
//                editor.putString("pass", pass_ok);
//                Intent intent = new Intent(this, LoginActivity.class);
//                startActivity(intent);
//            }
//
//        });
//    }
//}