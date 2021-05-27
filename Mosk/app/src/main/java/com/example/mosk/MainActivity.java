package com.example.mosk;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "moskLog";
    private ViewPager viewPager;
    private TabLayout tabLayout;

    //SQLite
    SQLiteDatabase locationDB = null;
    private final String dbname = "Mosk";
    private final String tablename = "location";
    private final String tablehome = "place";

    //Calender
    public static Boolean dateset = false;
    public static int year, month, day;
    public static String markerDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.pager);

//        String str = getIntent().getStringExtra("ExtraFragment");
//        if(str != null){
//            if(str.equals("Notification")){
//                Handler mHandler = new Handler();
//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        viewPager.setCurrentItem(2); // 알림바 클릭 시 지도 Fragment로 이동
//                    }
//                }, 100);
//            }
//        }

        //현재날짜 가져오기
        if (dateset == false){
            long now = System.currentTimeMillis();
            Date mDate = new Date(now);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            markerDate = simpleDateFormat.format(mDate);
        }

        //Create DB, Table
        locationDB = this.openOrCreateDatabase(dbname, MODE_PRIVATE, null);
        locationDB.execSQL("CREATE TABLE IF NOT EXISTS "+tablename
                +" (preTime datetime PRIMARY KEY, curTime datetime DEFAULT NULL, Latitude double NOT NULL, Longitude double NOT NULL)");

        locationDB.execSQL("CREATE TABLE IF NOT EXISTS "+tablehome
                +" (name VARCHAR(32) PRIMARY KEY, Latitude double NOT NULL, Longitude double NOT NULL)");

        // 2주 전 위치정보 삭제
        Cursor cursor = locationDB.rawQuery("SELECT * FROM "+tablename+" WHERE curTime<datetime('now','localtime','-14 days')", null);
        if (cursor.getCount() != 0){
            locationDB.execSQL("DELETE FROM "+tablename+" WHERE curTime<datetime('now','localtime','-14 days')");
            Log.d(TAG, "2주 전 위치정보가 삭제되었습니다.");
            Toast.makeText(this, "2주 전 위치정보가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
        }

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar); //상단 toolbar(카테고리별 메뉴 확인 가능)
        AppBarLayout appBarLayout=findViewById(R.id.appbar); //상단 appbar(title, 메뉴 아이콘 위치)

        if(appBarLayout.getLayoutParams()!=null){
            LinearLayout.LayoutParams layoutParams= (LinearLayout.LayoutParams) appBarLayout.getLayoutParams();
            AppBarLayout.Behavior appBarLayoutBehaviour=new AppBarLayout.Behavior();
            appBarLayoutBehaviour.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                @Override
                public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                    return false;
                }
            });
            //layoutParams.setBehavior(appBarLayoutBehaviour);
        }
        appBarLayout.setExpanded(true);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //앱 타이틀 없애기
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        setupTabIcons(tabLayout);

        final com.example.mosk.PagerAdapter adapter=new com.example.mosk.PagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount());

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void setupTabIcons(TabLayout tabLayout){
        View view1=getLayoutInflater().inflate(R.layout.customtab_icon,null);
        ImageView imageView1=view1.findViewById(R.id.img_tab);
        TextView textView1= view1.findViewById(R.id.txt_tab);

        View view2=getLayoutInflater().inflate(R.layout.customtab_icon,null);
        ImageView imageView2=view2.findViewById(R.id.img_tab);
        TextView textView2= view2.findViewById(R.id.txt_tab);

        View view3=getLayoutInflater().inflate(R.layout.customtab_icon,null);
        ImageView imageView3=view3.findViewById(R.id.img_tab);
        TextView textView3= view3.findViewById(R.id.txt_tab);

        Glide.with(this).load("https://i.imgur.com/QxYtzvt.png").into(imageView1);
        Glide.with(this).load("https://i.imgur.com/Fmsenfe.png").into(imageView2);
        Glide.with(this).load("https://i.imgur.com/EY6WNmH.png").into(imageView3);

        textView1.setText("감염확률 확인");
        textView2.setText("코로나 뉴스");
        textView3.setText("GPS 모아보기");

        tabLayout.addTab(tabLayout.newTab().setCustomView(view1));
        tabLayout.addTab(tabLayout.newTab().setCustomView(view2));
        tabLayout.addTab(tabLayout.newTab().setCustomView(view3));

    }
}