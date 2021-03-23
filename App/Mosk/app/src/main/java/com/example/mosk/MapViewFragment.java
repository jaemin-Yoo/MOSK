package com.example.mosk;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MapViewFragment extends Fragment {
    String TAG="TabFragment3";
    ViewGroup viewGroup;
    Context mContext;

    /*fab 설정*/
    private FloatingActionButton fab_more,fab_home,fab_save,fab_cancel;
    private Animation fab_open, fab_close;
    private boolean isFabOpen=false;
    private ClickListener listener = new ClickListener();

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
        viewGroup= (ViewGroup) inflater.inflate(R.layout.mapview_fragment,container,false); //xml과 연결

        setFab();
        return viewGroup;
    }

    public void setFab(){
        fab_more=viewGroup.findViewById(R.id.fab_more);
        fab_home=viewGroup.findViewById(R.id.fab_home);
        fab_save=viewGroup.findViewById(R.id.fab_save);

        fab_open = AnimationUtils.loadAnimation(mContext, R.anim.fab_open);
        fab_close= AnimationUtils.loadAnimation(mContext, R.anim.fab_close);

        fab_more.setOnClickListener(listener);
        fab_home.setOnClickListener(listener);
        fab_save.setOnClickListener(listener);
    }

    private class ClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.fab_more:
                    toggleFab();
                    Toast.makeText(mContext,"더보기를 클릭",Toast.LENGTH_SHORT).show();
                    break;
                case R.id.fab_home:
                    toggleFab();
                    Toast.makeText(mContext,"자주 가는 장소 ",Toast.LENGTH_SHORT).show();
                    break;
                case R.id.fab_save:
                    toggleFab();
                    Toast.makeText(mContext,"저장 수정",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    public void toggleFab(){
        Log.d(TAG, String.valueOf(isFabOpen));
        if(isFabOpen){
            fab_home.startAnimation(fab_close);
            fab_save.startAnimation(fab_close);
            fab_home.setClickable(false);
            fab_save.setClickable(false);

            fab_more.setImageResource(R.drawable.ic_add);
        }else{
            fab_home.startAnimation(fab_open);
            fab_save.startAnimation(fab_open);
            fab_home.setClickable(true);
            fab_save.setClickable(true);
            fab_more.setImageResource(R.drawable.ic_close);
        }
        isFabOpen=!isFabOpen;
    }

}

