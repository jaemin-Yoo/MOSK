package com.example.mosk;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class NewsRequest extends StringRequest {
    // 서버 URL 설정 ( PHP 파일 연동 )
    final static private String URL = "http://220.122.46.204:8002/news_search.php";
    private Map<String, String> map;


    public NewsRequest(String local, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("Local",local);

    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }

}

