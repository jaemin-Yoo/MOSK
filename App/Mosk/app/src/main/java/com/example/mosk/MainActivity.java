package com.example.mosk;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar); //상단 toolbar(카테고리별 메뉴 확인 가능)
        AppBarLayout appBarLayout=findViewById(R.id.appbar); //상단 appbar(title, 메뉴 아이콘 위치)

        if(appBarLayout.getLayoutParams()!=null){
            CoordinatorLayout.LayoutParams layoutParams= (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
            AppBarLayout.Behavior appBarLayoutBehaviour=new AppBarLayout.Behavior();
            appBarLayoutBehaviour.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                @Override
                public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                    return false;
                }
            });
            layoutParams.setBehavior(appBarLayoutBehaviour);
        }
        appBarLayout.setExpanded(true);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //앱 타이틀 없애기
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        mViewPager = (ViewPager) findViewById(R.id.container);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        setupTabIcons(tabLayout);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

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

        imageView1.setImageResource(R.drawable.menu_virus);
        imageView2.setImageResource(R.drawable.menu_news);
        imageView3.setImageResource(R.drawable.menu_location);

        textView1.setText("감염확률 확인");
        textView2.setText("코로나 뉴스");
        textView3.setText("GPS 모아보기");

        tabLayout.addTab(tabLayout.newTab().setCustomView(view1));
        tabLayout.addTab(tabLayout.newTab().setCustomView(view2));
        tabLayout.addTab(tabLayout.newTab().setCustomView(view3));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }
}