package com.example.mosk;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {
    // 서버 URL 설정 ( PHP 파일 연동 )
    final static private String URL = "http://자신의주소IP/login.php";
    private Map<String, String> map;


    public RegisterRequest(String userID, String userPassword, String userName, String userLocal, String userAge, String userInfected, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("userID",userID);
        map.put("userPassword", userPassword);
        map.put("userName", userName);
        map.put("userLocal", userLocal);
        map.put("userAge", userAge);
        map.put("userInfected", userInfected);


    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }

}

