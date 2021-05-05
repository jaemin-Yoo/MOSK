package com.example.mosk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    String TAG="Log";

    ClickListener listener = new ClickListener(); //class 선언하기
    EditText EditNAME,EditLOCAL,EditID,EditPW;
    Spinner SpinnerAGE;
    RadioGroup RG_Infected;

    ImageButton BtnBack;
    ImageView Img_icon;
    Button Btn_BACK,Btn_REGISTER;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditNAME=findViewById(R.id.et_name);
        EditLOCAL=findViewById(R.id.et_local);
        EditID=findViewById(R.id.et_id);
        EditPW=findViewById(R.id.et_pw);
        SpinnerAGE=findViewById(R.id.sp_age);

        RG_Infected=findViewById(R.id.radioGroup);

        BtnBack=findViewById(R.id.imageButton);
        Btn_REGISTER=findViewById(R.id.btn_register);

        Btn_REGISTER.setOnClickListener(listener);
        BtnBack.setOnClickListener(listener);

        Img_icon=findViewById(R.id.app_iconimg);
        Glide.with(this).load("https://i.imgur.com/SWhuueA.png").into(Img_icon);
        Glide.with(this).load("https://i.imgur.com/sfjPGF1.png").into(BtnBack);
    }

    private class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btn_register:
                    Log.d(TAG,"회원가입");
                    ClickRegister();
                    Intent MainIntent=new Intent(RegisterActivity.this,MainActivity.class);
                    startActivity(MainIntent);
                    finish();
                case R.id.imageButton:
                    finish();
                default:
                    break;
            }
        }
    }

    private void ClickRegister(){
        String userID = EditID.getText().toString();
        String userPass = EditPW.getText().toString();
        String userName = EditNAME.getText().toString();
        String userLocal = EditLOCAL.getText().toString();
        String userAge= (String) SpinnerAGE.getSelectedItem();

        int id = RG_Infected.getCheckedRadioButtonId();
        RadioButton rb = (RadioButton) findViewById(id);
        String userInfected=rb.getText().toString();

        Toast.makeText(getApplicationContext(), "id: "+userID+"pw: "+userPass+"name: "+userName+"Local: "+userLocal+"AGE: "+userAge+"확진여부"+userInfected,Toast.LENGTH_SHORT).show();

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    // TODO : 인코딩 문제때문에 한글 DB인 경우 로그인 불가
                    JSONObject register_jsonObject= new JSONObject(response);
                    int success = register_jsonObject.getInt("success");
                    success=0;
                    if (success==1) { // 로그인에 성공한 경우

                        Toast.makeText(getApplicationContext(), "회원가입에 성공하셨습니다.", Toast.LENGTH_SHORT).show();



                    } else { // 로그인에 실패한 경우
                        Toast.makeText(getApplicationContext(), "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        RegisterRequest registerRequest = new RegisterRequest(userID, userPass, userName,userLocal,userAge,userInfected,responseListener);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(registerRequest);
    }
}
