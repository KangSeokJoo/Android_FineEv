package com.jinasoft.fineevapp.Login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.jinasoft.fineevapp.Main.MainActivity;
import com.jinasoft.fineevapp.R;
import com.kakao.auth.AuthType;
import com.kakao.auth.Session;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

public class LoginActivity extends AppCompatActivity {

    private static String OAUTH_CLIENT_ID = "NC4qJt4KlFsB46mDqBEJ";
    private static String OAUTH_CLIENT_SECRET = "Hf6MN4DRLI";
    private static String OAUTH_CLIENT_NAME = "로그인서비스";
    private SessionCallback sessionCallback = new SessionCallback();
    Session session;
    private FirebaseAuth mAuth = null;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private SignInButton signInButton;

    private static OAuthLogin mOAuthLoginInstance;
    private static Context mContext;

    private OAuthLoginButton mOAuthLoginButton;

    private Button btnnaver, btnkakao, btngoogle, btnLogout;
    public boolean googleloginOn = false;
    String KakaoCheck;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mContext = this;

        //초기화
        initDate_naver();

//        btnemail = (Button)findViewById(R.id.Login_btn0);
        btnnaver = (Button)findViewById(R.id.Login_BTNNaver);
        btnkakao = (Button)findViewById(R.id.Login_BTNKakao);
        btngoogle = (Button)findViewById(R.id.Login_BTNGoogle);
        session = Session.getCurrentSession();
        session.addCallback(sessionCallback);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(500);
                        if (sessionCallback.kakaoLoginOn == true) {
                            GetMain();
                            break;
                        }
//                        if (accessToken != null){
//                            mOAuthLoginInstance.startOauthLoginActivity((Activity) mContext, mOAuthLoginHandler);
//                            finish();
//                            break;
//                        }
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();






        //구글버튼
        signInButton = findViewById(R.id.btnGoogle);
        mAuth = FirebaseAuth.getInstance();


        //구글
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (mAuth.getCurrentUser()!= null) {
            Toast.makeText(this, "구글로그인에 성공했습니다.", Toast.LENGTH_SHORT).show();
//            Log.e("확인", ""+mAuth+ "   /    " + mAuth.getCurrentUser() + mAuth.getUid());
            GetMain();
        }
//        btnemail.setOnClickListener(v -> {
//            Intent intent = new Intent(this, SignInActivity.class);
//            startActivity(intent);
//        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (googleloginOn == true){
                    GetMain();
                }else {
                    signIn();
                }
            }
        });


        btngoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (googleloginOn == true){
                    GetMain();
                }else {
                    signIn();
                }
            }
        });

        btnkakao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sessionCallback.kakaoLoginOn == false) {
                    session.open(AuthType.KAKAO_LOGIN_ALL, LoginActivity.this);
                }else{
                    GetMain();
                }
            }
        });

        btnnaver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOAuthLoginInstance.startOauthLoginActivity((Activity) mContext, mOAuthLoginHandler);
            }
        });


    }

    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (Session.getCurrentSession().handleActivityResult(requestCode,resultCode, data)){
            return;
        }
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            googleloginOn = true;
                        } else {
                            // If sign in fails, display a message to the user.
                            updateUI(null);
                            googleloginOn = false;
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) { //update ui code here
        if (user != null) {
            GetMain();
            googleloginOn = true;
        }
    }

    private void initDate_naver(){
        //초기화
        mOAuthLoginInstance = OAuthLogin.getInstance();
        mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);

        mOAuthLoginButton = (OAuthLoginButton) findViewById(R.id.btnNaver);
        mOAuthLoginButton.setOAuthLoginHandler(mOAuthLoginHandler);
    }



    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if(success){
                String accessToken = mOAuthLoginInstance.getAccessToken(mContext);
                String refreshToken = mOAuthLoginInstance.getRefreshToken(mContext);
                long expiresAt = mOAuthLoginInstance.getExpiresAt(mContext);
                String tokenType = mOAuthLoginInstance.getTokenType(mContext);

//                Toast.makeText(mContext, "success:" + accessToken, Toast.LENGTH_SHORT).show();
                Toast.makeText(mContext, "네이버 로그인에 성공했습니다. \n 잠시만 기다려주세요", Toast.LENGTH_LONG).show();
                Log.e("확인",""+accessToken+"   /   "+refreshToken+"   /   "+expiresAt+"   /   "+tokenType);
                //본인이 이동할 액티비티를 입력
                GetMain();
            }else {
                String errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).getCode();
                String errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext);
                Toast.makeText(mContext,"errorCode:" + errorCode +", errorDesc:"+errorDesc, Toast.LENGTH_LONG).show();
            }
        }
    };
    protected void GetMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}