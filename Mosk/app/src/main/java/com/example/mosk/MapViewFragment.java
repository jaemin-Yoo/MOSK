package com.example.mosk;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.PrintWriter;
import java.util.List;

public class MapViewFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "MoskTag";
    ViewGroup viewGroup;
    Context mContext;

    //Socket
    private String data = "";

    /*fab 설정*/
    private FloatingActionButton fab_more,fab_home,fab_send,fab_cal;
    private Animation fab_open, fab_close;
    private boolean isFabOpen=false;
    private ClickListener listener = new ClickListener();

    //GPS
    private GoogleMap mMap;
    private Marker[] currentMarker = new Marker[100];
    private int marker_cnt = 0;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    //gps가 켜져 있는 동안에 실시간으로 바뀌는 위치정보를 얻기 위함
    private static final int UPDATE_INTERVAL_MS = 1000;//1초간격
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;

    private int wait = 0;

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };  // 외부 저장소

    Location mCurrentLocatiion;
    LatLng currentPosition;
    private double Lat, Long;
    private double Lat_h = 0.0, Long_h = 0.0;
    private String color = "";

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;

    private View mLayout;

    //SQLite
    SQLiteDatabase locationDB;
    private final String dbname = "Mosk";
    private final String tablename = "location";
    private final String tablehome = "place";

    //Notification state
    public static Boolean nonot = false;

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

        locationDB = getActivity().openOrCreateDatabase(dbname, getActivity().MODE_PRIVATE, null);

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);

        setFab();
        return viewGroup;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");

        mMap = googleMap;

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면
            startLocationUpdates(); // 3. 위치 업데이트 시작
        } else {
            //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(getView(), "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                    }
                }).show();
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        markerUpdate();

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                Log.d(TAG, "onLocationResult : 위도:" + String.valueOf(location.getLatitude()) + " 경도:" + String.valueOf(location.getLongitude()));

                if(wait!=1) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentPosition);
                    mMap.moveCamera(cameraUpdate);
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                }
                wait=1;
                mCurrentLocatiion = location;
            }
        }
    };

    public void markerUpdate(){
        int i=0;
        if (currentMarker[i]!=null){
            currentMarker[i].remove();
            i++;
        }

        marker_cnt=0;
        Cursor cursor = locationDB.rawQuery("SELECT * FROM "+tablename, null);
        Cursor cursor_h = locationDB.rawQuery("SELECT * FROM "+tablehome, null);
        Log.d(TAG, "cnt = "+cursor_h.getCount());
        if (cursor_h.getCount() != 0){
            while(cursor_h.moveToNext()){
                Lat_h = cursor_h.getDouble(0);
                Long_h = cursor_h.getDouble(1);

                color = "blue";
                setCurrentLocation("", "", Lat_h, Long_h, color); // 자주가는 장소 마커표시

                while(cursor.moveToNext()){
                    String pretime = cursor.getString(0);
                    String curtime = cursor.getString(1);
                    Lat = cursor.getDouble(2);
                    Long = cursor.getDouble(3);

                    if (getDistance(Lat_h, Long_h, Lat, Long) > 50){
                        color = "red";
                        setCurrentLocation(pretime, curtime, Lat, Long, color); // 나의 이동동선 마커표시
                    } else{
                        locationDB.execSQL("DELETE FROM "+tablename+" WHERE Latitude="+Lat+" AND Longitude="+Long); // 자주가는 장소 근처 위치 삭제
                    }
                }
            }
        } else{
            while(cursor.moveToNext()){
                String pretime = cursor.getString(0);
                String curtime = cursor.getString(1);
                Lat = cursor.getDouble(2);
                Long = cursor.getDouble(3);
                color = "red";
                setCurrentLocation(pretime, curtime, Lat, Long, color); // 나의 이동동선 마커표시
            }
        }

    }

    public double getDistance(double pre_lat, double pre_lng, double aft_lat, double aft_lng) {
        double distance = 0;
        Location locationA = new Location("A");
        locationA.setLatitude(pre_lat);
        locationA.setLongitude(pre_lng);

        Location locationB = new Location("B");
        locationB.setLatitude(aft_lat);
        locationB.setLongitude(aft_lng);

        distance = locationA.distanceTo(locationB);
        return distance; // m 단위
    }

    public void setCurrentLocation(String pretime, String curtime, double lat, double lng, String color) {
        LatLng currentLatLng = new LatLng(lat, lng); // maker 위치 ( 0.001 = 약 100m )

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);

        if (color == "blue"){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            markerOptions.title("자주가는장소");
            Log.d(TAG, "1");
        } else{
            if (curtime == null){
                markerOptions.title("동선 저장 중..");
            } else{
                markerOptions.title(pretime+" ~ "+curtime);
            }
            Log.d(TAG, "0");
        }
        currentMarker[marker_cnt] = mMap.addMarker(markerOptions);
        marker_cnt++;
    }



    private void startLocationUpdates() //위치를 이동하면서 계속 업데이트하는 과정
    {
        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());


            if (checkPermission())
                mMap.setMyLocationEnabled(true); // 현재위치 파란색 동그라미로 표시
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        if (checkPermission()) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mMap != null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);


        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void setDefaultLocation()
    {
        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";
        int i=0;
        if (currentMarker[i] != null) currentMarker[i].remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker[i] = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 18);
        mMap.moveCamera(cameraUpdate);
    }

    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   )
        {
            return true;
        }

        return false;
    }

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;
            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults)
            {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if ( check_result )
            {
                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0]) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[1]))
                {
                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(getView(), "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            getActivity().finish();
                        }
                    }).show();

                }else {
                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(getView(), "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            getActivity().finish();
                        }
                    }).show();
                }
            }

        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");
                        needRequest = true;

                        return;
                    }
                }

                break;
        }
    }

    public void setFab(){
        fab_more=viewGroup.findViewById(R.id.fab_more);
        fab_home=viewGroup.findViewById(R.id.fab_home);
        fab_send=viewGroup.findViewById(R.id.fab_send);
        fab_cal=viewGroup.findViewById(R.id.fab_cal);

        Glide.with(this).load("https://i.imgur.com/n76lRoV.png").into(fab_home);
        Glide.with(this).load("https://i.imgur.com/M5ywSIa.png").into(fab_send);
        Glide.with(this).load("https://i.imgur.com/NUCaHI0.png").into(fab_cal);

        fab_open = AnimationUtils.loadAnimation(mContext, R.anim.fab_open);
        fab_close= AnimationUtils.loadAnimation(mContext, R.anim.fab_close);

        fab_more.setOnClickListener(listener);
        fab_home.setOnClickListener(listener);
        fab_send.setOnClickListener(listener);
        fab_cal.setOnClickListener(listener);
    }

    /*클릭리스너 클래스*/
    private class ClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.fab_more:
                    toggleFab();
                    break;
                case R.id.fab_home:
                    dialog_home();
                    toggleFab();
                    break;
                case R.id.fab_send:
                    toggleFab();
                    dialog_alert_sending();
                    break;
                case R.id.fab_cal:
                    locationDB.execSQL("INSERT INTO "+tablename+" VALUES('2021-05-05 18:03:08','2021-05-05 21:12:32','35.83072','128.7543047')");
                    Toast.makeText(mContext,"캘린더로 보기",Toast.LENGTH_SHORT).show();
                    toggleFab();
                default:
                    break;
            }
        }
    }

    private void SendingService(){
        if (MyService.serviceIntent!=null){
            if (MyService.networKWriter!=null){
                Cursor cursor = locationDB.rawQuery("SELECT * FROM "+tablename, null);
                while(cursor.moveToNext()){
                    final String pretime = cursor.getString(0);
                    final String curtime = cursor.getString(1);
                    final double Lat = cursor.getDouble(2);
                    final double Long = cursor.getDouble(3);

                    if (curtime != null){
                        data = "Data exist";
                        new Thread(){
                            public void run(){
                                // 동선 저장 중인 위치는 전송 x
                                PrintWriter out = new PrintWriter(MyService.networKWriter, true);
                                data = pretime+"/"+curtime+"/"+Lat+"/"+Long;
                                out.println(data);
                                Log.d(TAG,"전송된 데이터: "+data);
                            }
                        }.start();
                    }
                }

                if (data==""){
                    Toast.makeText(getContext(), "전송 할 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                } else{
                    nonot = true;
                    Toast.makeText(getContext(), "데이터를 전송하였습니다.", Toast.LENGTH_SHORT).show();
                }
            } else{
                Toast.makeText(getContext(), "서버 상태를 확인하세요.", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(getContext(), "백그라운드 서비스를 먼저 시작해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void dialog_alert_sending(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("위치 데이터 전송");
        builder.setMessage("위치 데이터를 서버에 전송하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SendingService();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(mContext,"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                    }
                });
        builder.show();
    }

    private void dialog_home(){
        final EditText edittext = new EditText(mContext);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        builder.setTitle("자주 가는 장소 설정");
        builder.setMessage("해당 장소의 이름을 입력하세요.");
        builder.setView(edittext);
        builder.setPositiveButton("입력",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(mContext,edittext.getText().toString() ,Toast.LENGTH_LONG).show();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    public void toggleFab(){
        Log.d(TAG, String.valueOf(isFabOpen));
        if(isFabOpen){
            fab_home.startAnimation(fab_close);
            fab_send.startAnimation(fab_close);
            fab_cal.startAnimation(fab_close);

            fab_home.setClickable(false);
            fab_send.setClickable(false);
            fab_cal.setClickable(false);

            fab_more.setImageResource(R.drawable.ic_add);
        }else{
            fab_home.startAnimation(fab_open);
            fab_send.startAnimation(fab_open);
            fab_cal.startAnimation(fab_open);

            fab_home.setClickable(true);
            fab_send.setClickable(true);
            fab_cal.setClickable(true);

            fab_more.setImageResource(R.drawable.ic_close);
        }
        isFabOpen=!isFabOpen;
    }

}

