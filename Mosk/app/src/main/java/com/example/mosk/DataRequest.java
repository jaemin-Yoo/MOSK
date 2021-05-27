package com.example.mosk;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class DataRequest extends StringRequest {

    // 서버 URL 설정 ( PHP 파일 연동 )
    final static private String URL = "http://220.122.46.204:8002/news_db.php";
    private Map<String, String> map;

    public DataRequest(String city_name,String total_pat,String today_pat,String social_step, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("city_name", city_name);
        map.put("total_pat", total_pat);
        map.put("today_pat", today_pat);
        map.put("social_step", social_step);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}