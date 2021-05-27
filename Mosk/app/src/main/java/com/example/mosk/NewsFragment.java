package com.example.mosk;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class NewsFragment extends Fragment {

    Context mContext;
    ViewGroup viewGroup;
    String TAG="NewsFragement";

    int country_mode=0; // 국가 선택 모드 default(국내) 0, 해외 1
    RadioButton rgbtn_korea,rgbtn_abroad;

    private TextView total_inf, today_inf, txtstep, textView;

    Spinner spinner;
    String[] city_names;
    ArrayAdapter<String> stringArrayAdapter;
    Button searchbtn;

    HospitalAdapter mhospitalAdapter;
    ListView mListView;
    //클릭리스너
    private NewsFragment.ClickListener listener = new NewsFragment.ClickListener();

    public void onAttach(Context context){
        super.onAttach(context);
        mContext=context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewGroup= (ViewGroup) inflater.inflate(R.layout.news_fragment,container,false); //xml과 연결

        textView = viewGroup.findViewById(R.id.textView);

        rgbtn_korea=viewGroup.findViewById(R.id.rg_korea);
        rgbtn_abroad=viewGroup.findViewById(R.id.rg_abroad);

        rgbtn_abroad.setOnClickListener(listener);
        rgbtn_korea.setOnClickListener(listener);

        spinner=viewGroup.findViewById(R.id.spinner);

        city_names=getResources().getStringArray(R.array.korea_city); //디폴트 국내
        stringArrayAdapter= new ArrayAdapter<String>(mContext, R.layout.support_simple_spinner_dropdown_item, city_names);
        stringArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(stringArrayAdapter);

        searchbtn=viewGroup.findViewById(R.id.search_button);
        searchbtn.setOnClickListener(listener);

        total_inf=viewGroup.findViewById(R.id.total_inf);
        today_inf=viewGroup.findViewById(R.id.today_inf);
        txtstep=viewGroup.findViewById(R.id.txtstep);

        total_inf.setText("");  today_inf.setText("");  txtstep.setText("");
        mhospitalAdapter= new HospitalAdapter();

        mListView = (ListView) viewGroup.findViewById(R.id.list_hospital);
        mhospitalAdapter = new HospitalAdapter();
        mListView.setAdapter(mhospitalAdapter);
        return viewGroup;
    }

    /*버튼 클릭 리스너와 리스트 뷰 클릭 리스너*/
    private class ClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.rg_korea:
                    country_mode=0;
                    Log.d(TAG,"국내 선택");
                    city_names=getResources().getStringArray(R.array.korea_city); //디폴트 국내
                    stringArrayAdapter= new ArrayAdapter<String>(mContext, R.layout.support_simple_spinner_dropdown_item, city_names);
                    stringArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    spinner.setAdapter(stringArrayAdapter);
                    break;
                case R.id.rg_abroad:
                    country_mode=1;
                    Log.d(TAG,"국외 선택");
                    city_names=getResources().getStringArray(R.array.abroad_city); //디폴트 국외
                    stringArrayAdapter= new ArrayAdapter<String>(mContext, R.layout.support_simple_spinner_dropdown_item, city_names);
                    stringArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    spinner.setAdapter(stringArrayAdapter);
                    break;
                case R.id.search_button:
                    setData();
                default:
                    break;
            }
        }
    }

    private void setData(){
        String local= (String) spinner.getSelectedItem();
        Log.d(TAG,"LOCAL "+local);

        mhospitalAdapter.removeAll();

        total_inf.setText("");
        today_inf.setText("");
        txtstep.setText("");
        textView.setText("");
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject news_jsonObject= new JSONObject(response);
                    boolean success = news_jsonObject.getBoolean("success");
                    Log.d(TAG,"SUCCESS"+success);
                    int index=0;

                    if (success) {

                        int Total=news_jsonObject.getInt("Total_InfNum");
                        int Today=news_jsonObject.getInt("Today_InfNum");
                        double Step=news_jsonObject.getDouble("Step");
                        int size=news_jsonObject.getInt("size");

                        String Name[]=new String[size];
                        String Address[]=new String[size];
                        String PHNum[]=new String[size];

                        if(size>0){
                            for(index=0;index<size;index++){
                                Name[index]=news_jsonObject.getString("Name"+index);
                                Address[index]=news_jsonObject.getString("Address"+index);
                                PHNum[index]=news_jsonObject.getString("PhoneNum"+index);

                                mhospitalAdapter.addItem(Name[index],PHNum[index],Address[index]);
                            }
                        } else{
                            textView.setText("해외 정보는\n제공하지 않습니다.");
                        }

                        mListView.setAdapter(mhospitalAdapter);
                        total_inf.setText(Total+"");
                        today_inf.setText(Today+"");
                        txtstep.setText((int) Step+"");
                        Log.d(TAG,success+"total "+Total+"Today "+Today+"Step "+Step);
                    } else {
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        NewsRequest newsRequest=new NewsRequest(local,responseListener);
        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(newsRequest);

    }

}
