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
    String[] DataTitleSet={"누적 확진자 수","신규 확진자 수","병원정보"}; //서버에서 받는 데이터셋
    String[] DataContentsSet;

    /*국가 카테고리 리스트*/
    ListView listView=null;
    ArrayAdapter adapter; //검색하는 지역 list 어댑터
    EditText editText;
    int country_mode=0; // 국가 선택 모드 default(국내) 0, 해외 1
    RadioButton rgbtn_korea,rgbtn_abroad;
    Button btn_Search;
    ArrayList<String> NewsDataset;

    /*4월 12일*/
    Spinner spinner;
    String[] city_names;
    ArrayAdapter<String> stringArrayAdapter;
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
        viewGroup= (ViewGroup) inflater.inflate(R.layout.news_fragment_test,container,false); //xml과 연결

//        mRecyclerView=viewGroup.findViewById(R.id.recycler_View);
//
//        mRecyclerView.setHasFixedSize(true);
//        mLayoutManager=new LinearLayoutManager(mContext);
//        mRecyclerView.setLayoutManager(mLayoutManager);
//
//
//
//        NewsDataset=new ArrayList<String>();
        //DefaultData();

//        listView=viewGroup.findViewById(R.id.category_list);
//        adapter=new ArrayAdapter(mContext, android.R.layout.simple_list_item_1,NewsDataset);
//        listView.setAdapter(adapter);


//
//        editText=(EditText)viewGroup.findViewById(R.id.editTextFilter);
//        editText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                String filterText=editable.toString();
//                adapter.getFilter().filter(filterText);
//            }
//        });

        rgbtn_korea=viewGroup.findViewById(R.id.rg_korea);
        rgbtn_abroad=viewGroup.findViewById(R.id.rg_abroad);
//
//        btn_Search=viewGroup.findViewById(R.id.search_btn);
//
        rgbtn_abroad.setOnClickListener(listener);
        rgbtn_korea.setOnClickListener(listener);
//        btn_Search.setOnClickListener(listener);
//        listView.setOnItemClickListener(itemClickListener);

        /*4월 12일 코딩 */
        spinner=viewGroup.findViewById(R.id.spinner);

        city_names=getResources().getStringArray(R.array.korea_city); //디폴트 국내
        stringArrayAdapter= new ArrayAdapter<String>(mContext, R.layout.support_simple_spinner_dropdown_item, city_names);
        stringArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(stringArrayAdapter);

        return viewGroup;
    }

//    private void  DefaultData(){
//
//        /*디폴트*/
//        NewsDataset.add("1");
//        NewsDataset.add("@");
//    }
//
//    private void Mod_listData(){
//        if(country_mode==0){
//            NewsDataset.clear();
//            NewsDataset.add("서울 특별시");  NewsDataset.add("대구 광역시"); NewsDataset.add("인천 광역시"); NewsDataset.add("부산 광역시");
//            NewsDataset.add("울산 광역시"); NewsDataset.add("광주 광역시"); NewsDataset.add("대전 광역시"); NewsDataset.add("경기도");
//            NewsDataset.add("경상북도"); NewsDataset.add("경상남도"); NewsDataset.add("충척북도"); NewsDataset.add("충청남도"); NewsDataset.add("강원도");  NewsDataset.add("전라북도");
//            NewsDataset.add("전라남도");  NewsDataset.add("전라남도");  NewsDataset.add("제주 특별 자치도"); NewsDataset.add("세종 특별 자치시");
//            adapter.notifyDataSetChanged();
//        }
//        else{
//            NewsDataset.clear();
//            NewsDataset.add("해외");
//            adapter.notifyDataSetChanged();
//        }
//    }
//
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
//                case R.id.search_btn:
//                    Toast.makeText(mContext, "검색 버튼 클릭", Toast.LENGTH_SHORT).show();
//                    SetNewsView();
//                    break;
                default:
                    break;
            }
        }
    }
//
//    AdapterView.OnItemClickListener itemClickListener= new AdapterView.OnItemClickListener() {
//
//        @Override
//        public void onItemClick(AdapterView<?> adapterView, View view,  int position, long id) {
//            Toast.makeText(mContext,NewsDataset.get(position),Toast.LENGTH_SHORT).show();
//            editText.setText(NewsDataset.get(position));
//        }
//
//    };
//
//    private void SetNewsView(){
//        DataContentsSet= new String[]{"에엥", "삐용", "띠영"};
//
//        mAdapter=new RC_Adapter(DataTitleSet,DataContentsSet);
//        mRecyclerView.setAdapter(mAdapter);
//
//    }
}
