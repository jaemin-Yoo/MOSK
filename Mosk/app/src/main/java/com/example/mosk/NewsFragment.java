package com.example.mosk;

import android.content.Context;
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

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class NewsFragment extends Fragment {

    Context mContext;
    ViewGroup viewGroup;
    SearchView searchView;
    String TAG="NewsFragement";

    /*리사이클뷰*/
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter; //서버에서 받는 데이터 어댑터
    private RecyclerView.LayoutManager mLayoutManager;

    int country_mode=0; // 국가 선택 모드 default(국내) 0, 해외 1
    RadioButton rgbtn_korea,rgbtn_abroad;

    TextView T1V1,T1V2,T1V3,T2V1,T2V2;

    Spinner spinner;
    String[] city_names;
    ArrayAdapter<String> stringArrayAdapter;
    Button searchbtn;

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

        T1V1=viewGroup.findViewById(R.id.txtT1V1);
        T1V2=viewGroup.findViewById(R.id.txtT1V2);
        T1V3=viewGroup.findViewById(R.id.txtT1V3);
        T2V1=viewGroup.findViewById(R.id.txtT2V1);
        T2V2=viewGroup.findViewById(R.id.txtT2V2);

        T1V1.setText("");  T1V2.setText("");  T1V3.setText("");  T2V1.setText("");
        T2V2.setText("");


        return viewGroup;
    }

    /*버튼 클릭 리스너와 리스트 뷰 클릭 리스너*/
    private class ClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.rg_korea:
                    country_mode=0;
                    //Mod_listData();
                    Log.d(TAG,"국내 선택");
                    city_names=getResources().getStringArray(R.array.korea_city); //디폴트 국내
                    stringArrayAdapter= new ArrayAdapter<String>(mContext, R.layout.support_simple_spinner_dropdown_item, city_names);
                    stringArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    spinner.setAdapter(stringArrayAdapter);
                    break;
                case R.id.rg_abroad:
                    country_mode=1;
                    //Mod_listData();
                    city_names=getResources().getStringArray(R.array.abroad_city); //디폴트 국내
                    stringArrayAdapter= new ArrayAdapter<String>(mContext, R.layout.support_simple_spinner_dropdown_item, city_names);
                    stringArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    spinner.setAdapter(stringArrayAdapter);
                    break;
                case R.id.search_button:
                    Toast.makeText(mContext, "검색 버튼 클릭", Toast.LENGTH_SHORT).show();
                    setData();
                default:
                    break;
            }
        }
    }

    private void setData(){
        T1V1.setText("1");  T1V2.setText("2");  T1V3.setText("3");  T2V1.setText("4");
        T2V2.setText("5");
    }

}
