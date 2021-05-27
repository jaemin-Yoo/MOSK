package com.example.mosk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "moskLog";

    EditText EditID,EditPW;
    ImageView Img_icon;

    Button BtnLogin,BtnRegister;
    ImageButton BtnBack;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (MyService.serviceIntent != null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        EditID=findViewById(R.id.et_id);
        EditPW=findViewById(R.id.et_pw);

        BtnLogin=findViewById(R.id.btn_login);
        BtnRegister=findViewById(R.id.btn_register);
        BtnBack=findViewById(R.id.imageButton);

        intent=new Intent(LoginActivity.this,RegisterActivity.class);

        BtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClickLogin();

            }
        });
        BtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);
            }
        });
        BtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Img_icon=findViewById(R.id.app_iconimg);
        Glide.with(this).load("https://i.imgur.com/SWhuueA.png").into(Img_icon);
        Glide.with(this).load("https://i.imgur.com/sfjPGF1.png").into(BtnBack);

    }


    private void ClickLogin(){
        String userID = EditID.getText().toString();
        String userPass = EditPW.getText().toString();

        if (userID.equals("0") & userPass.equals("0")){
            Intent MainIntent=new Intent(LoginActivity.this,MainActivity.class);
            startActivity(MainIntent);
            finish();
            Toast.makeText(this, "개발자 모드 진입", Toast.LENGTH_SHORT).show();
        }

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    // TODO : 인코딩 문제때문에 한글 DB인 경우 로그인 불가
                    JSONObject  login_jsonObject= new JSONObject(response);
                    boolean success = login_jsonObject.getBoolean("success");

                    if (success) { // 로그인에 성공한 경우
                        /*서버에서 받는 아이디와 PW*/
                        String userID = login_jsonObject.getString("userID");
                        String userPass = login_jsonObject.getString("userPW");

                        Toast.makeText(getApplicationContext(), "로그인에 성공하셨습니다.", Toast.LENGTH_SHORT).show();
                        Intent MainIntent=new Intent(LoginActivity.this,MainActivity.class);
                        startActivity(MainIntent);
                        finish();
                    } else { // 로그인에 실패한 경우
                        Toast.makeText(getApplicationContext(), "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                }
            };

        LoginRequest loginRequest=new LoginRequest(userID,userPass,responseListener);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(loginRequest);

    }

}