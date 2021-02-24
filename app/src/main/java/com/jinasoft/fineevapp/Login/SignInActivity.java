package com.jinasoft.fineevapp.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jinasoft.fineevapp.Main.MainActivity;
import com.jinasoft.fineevapp.R;

import java.util.ArrayList;

public class SignInActivity extends AppCompatActivity {

    EditText edit_email, edit_pass, edit_pass_ok, edit_name;
    Button emailcheck, submit;
    private String[] editTextSave = new String[4];
    boolean isSignIncheck = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        SharedPreferences pref = getSharedPreferences("Sign",MODE_PRIVATE);

        edit_name = (EditText)findViewById(R.id.Login_EDTSign1);
        edit_email = (EditText)findViewById(R.id.Login_EDTSign2);
        edit_pass = (EditText)findViewById(R.id.Login_EDTSign3);
        edit_pass_ok = (EditText)findViewById(R.id.Login_EDTSign4);

        emailcheck = (Button)findViewById(R.id.Sgin_BTNSignEmailCheck);
        emailcheck.setOnClickListener(v -> {

            editTextSave[1] = (edit_email.getText().toString());

           if (editTextSave[1] == "" || editTextSave[1] == null || !editTextSave[1].contains("@")){
               edit_email.setText("");
               Toast.makeText(this, R.string.Email_None, Toast.LENGTH_SHORT).show();
           }else if (editTextSave[1].equals(pref.getString("email",""))){
               edit_email.setText("");
               Toast.makeText(this, R.string.Email_have, Toast.LENGTH_SHORT).show();
           }else {
               isSignIncheck = true;
               Toast.makeText(this, R.string.Email_possible, Toast.LENGTH_SHORT).show();
           }
        });

        submit = (Button)findViewById(R.id.Sign_BTNSignIn);
        submit.setOnClickListener(v -> {

            editTextSave[0] = (edit_name.getText().toString());
            editTextSave[2] = (edit_pass.getText().toString());
            editTextSave[3] = (edit_pass_ok.getText().toString());

            if (isSignIncheck == false){
                Toast.makeText(this, R.string.Email_check, Toast.LENGTH_SHORT).show();
            }else if (!editTextSave[2].equals(editTextSave[3])){
                Toast.makeText(this, R.string.Password_check, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, R.string.Submit_Success, Toast.LENGTH_SHORT).show();
                Submit();
            }

        });

    }
    public void Submit(){
        SharedPreferences pref = getSharedPreferences("Sign",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

            editor.putString("name", editTextSave[0]);
            editor.putString("email", editTextSave[1]);
            editor.putString("pass", editTextSave[2]);

            editor.putBoolean("isLogin", true);
            editor.commit();



        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}