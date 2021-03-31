package com.example.mosk;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ramotion.foldingcell.FoldingCell;

public class NewsFragment extends Fragment {

    Context mContext;
    ViewGroup viewGroup;


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


        return viewGroup;
    }


    private class ClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch(view.getId()){

                default:
                    break;
            }
        }
    }
}
