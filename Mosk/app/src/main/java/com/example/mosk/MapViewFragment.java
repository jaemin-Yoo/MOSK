package com.example.mosk;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.google.android.material.datepicker.MaterialCalendar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapViewFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "moskLog";
    ViewGroup viewGroup;
    Context mContext;

    //Socket
    private String data = "";

    // Fab
    private FloatingActionButton fab_more,fab_home,fab_send,fab_cal, fab_develop;
    private Animation fab_open, fab_close;
    private boolean isFabOpen=false;
    private ClickListener listener = new ClickListener();

    // GPS
    private GoogleMap mMap;
    private Marker[] currentMarker = new Marker[100];
    private int marker_cnt = 0;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;//1초간격 (gps가 켜져 있는 동안에 실시간으로 바뀌는 위치정보를 얻기 위함)
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초
    private static final int PERMISSIONS_REQUEST_CODE = 100; // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    boolean needRequest = false;

    // Permission
    String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    //Location
    Location mCurrentLocatiion;
    LatLng currentPosition;
    private double Lat, Long;
    private double Lat_h = 0.0, Long_h = 0.0;
    private String name_h = "";
    private String color = "";
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private int wait = 0;

    //SQLite
    SQLiteDatabase locationDB;
    private final String dbname = "Mosk";
    private final String tablename = "location";
    private final String tablehome = "place";

    //Notification state
    public static Boolean nonot = false;

    //Calendar
    private DatePickerDialog.OnDateSetListener callbackMethod;
    private TimePickerDialog.OnTimeSetListener callbackpretime;
    private TimePickerDialog.OnTimeSetListener callbackcurtime;

    //List
    List<Map<String, Object>> dialogItemList;
    int[] img = {R.drawable.icon_marker, R.drawable.icon_delete};
    String[] text = {"현재위치등록","위치삭제"};

    //Developer mode
    private Boolean mode = false;
    private String pretime_picker, curtime_picker;
    private double lat_picker = 0.0, lng_picker = 0.0;

    //View
    private TextView textView;


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

        textView = viewGroup.findViewById(R.id.textView);

        if (MyService.serviceIntent != null){
            textView.setText("서비스 동작 중");
        }

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
        dialogItemList = new ArrayList<>();

        for(int i=0;i<img.length;i++){
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("img", img[i]);
            itemMap.put("text", text[i]);

            dialogItemList.add(itemMap);
        }
        return viewGroup;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");

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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mode == true){
                    pretime_picker = "";
                    curtime_picker = "";
                    showTimePicker(callbackpretime, "preTime");

                    lat_picker = latLng.latitude; // 위도
                    lng_picker = latLng.longitude; // 경도
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                if (mode == true){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("위치 삭제");
                    builder.setMessage("해당 위치를 삭제하시겠습니까?");
                    builder.setPositiveButton("예",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String[] time = marker.getTitle().split(" ~ ");
                                    String pretime = time[0];
                                    try{
                                        locationDB.execSQL("DELETE FROM "+tablename+" WHERE preTime='"+pretime+"'");
                                        markerUpdate();
                                    } catch (Exception e){
                                        Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    builder.setNegativeButton("아니오",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                    builder.show();
                    return true;
                }
                return false;
            }
        });

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    private void showTimePicker(TimePickerDialog.OnTimeSetListener callback, final String title){
        callback = new TimePickerDialog.OnTimeSetListener(){
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (title == "preTime"){
                    pretime_picker = MainActivity.markerDate+" "+String.format("%02d",hourOfDay)+":"+String.format("%02d",minute)+":00";
                    showTimePicker(callbackcurtime, "curTime");
                } else{
                    if (pretime_picker != ""){
                        curtime_picker = MainActivity.markerDate+" "+String.format("%02d",hourOfDay)+":"+String.format("%02d",minute)+":00";
                        Log.d(TAG, "Select Date and Location : "+pretime_picker+" "+curtime_picker+" "+lat_picker+" "+lng_picker);
                        locationDB.execSQL("INSERT INTO "+tablename+" VALUES('"+pretime_picker+"', '"+curtime_picker+"', "+lat_picker+", "+lng_picker+")");
                        markerUpdate();
                        Toast.makeText(mContext, "마커가 생성되었습니다.", Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(mContext, "preTime을 설정해주세요.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        final TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),android.R.style.Theme_Holo_Light_Dialog_NoActionBar,callback,hour,min,false);
        timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        timePickerDialog.setTitle(title);
        timePickerDialog.show();
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);

                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                if(wait!=1) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentPosition);
                    mMap.moveCamera(cameraUpdate);
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                }
                wait=1;

                mCurrentLocatiion = location;
            }
        }
    };

    public void markerUpdate(){
        int i=0;
        while (currentMarker[i]!=null){
            currentMarker[i].remove();
            i++;
        }


        marker_cnt=0;
        Log.d(TAG, "markerDate : "+MainActivity.markerDate);
        //Cursor cursor = locationDB.rawQuery("SELECT * FROM "+tablename+" WHERE preTime<='"+MainActivity.markerDate+" 23:59:59' AND curTime>='"+MainActivity.markerDate+" 00:00:00'", null);
        Cursor cursor = locationDB.rawQuery("SELECT * FROM "+tablename+" WHERE preTime LIKE '"+MainActivity.markerDate+"%' OR curTime LIKE '"+MainActivity.markerDate+"%'", null);
        Cursor cursor_h = locationDB.rawQuery("SELECT * FROM "+tablehome, null);
        Log.d(TAG, "Home cnt = "+cursor_h.getCount());
        if (cursor_h.getCount() != 0){
            while(cursor_h.moveToNext()){
                name_h = cursor_h.getString(0);
                Lat_h = cursor_h.getDouble(1);
                Long_h = cursor_h.getDouble(2);

                color = "blue";
                setCurrentLocation("", "", Lat_h, Long_h, color); // 자주가는 장소 마커표시

                while(cursor.moveToNext()){
                    String pretime = cursor.getString(0);
                    String curtime = cursor.getString(1);
                    Lat = cursor.getDouble(2);
                    Long = cursor.getDouble(3);

                    if (getDistance(Lat_h, Long_h, Lat, Long) > 50){
                        color = "red";
                        for(int cnt=0; cnt<MyService.infloc.size(); cnt++){
                            if (MyService.infloc.get(cnt).equals(pretime)){
                                color = "inf";
                                Log.d(TAG, "inf marker!");
                                break;
                            }
                        }

                        for(int cnt=0; cnt<MyService.warnloc.size(); cnt++){
                            if (MyService.warnloc.get(cnt).equals(pretime)){
                                color = "warn";
                                Log.d(TAG, "warn marker!");
                                break;
                            }
                        }
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
                Log.d(TAG, "infloc.size() : "+MyService.infloc.size());
                for(int cnt=0; cnt<MyService.infloc.size(); cnt++){
                    if (MyService.infloc.get(cnt).equals(pretime)){
                        color = "inf";
                        Log.d(TAG, "inf marker!");
                        break;
                    }
                }

                Log.d(TAG, "warnloc.size() : "+MyService.warnloc.size());
                for(int cnt=0; cnt<MyService.warnloc.size(); cnt++){
                    if (MyService.warnloc.get(cnt).equals(pretime)){
                        color = "warn";
                        Log.d(TAG, "warn marker!");
                        break;
                    }
                }
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
            markerOptions.title(name_h);
        } else if (color == "red"){
            if (curtime == null){
                markerOptions.title("동선 저장 중..");
            } else{
                markerOptions.title(pretime+" ~ "+curtime);
            }
        } else if (color == "inf"){
            BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.virus); // maker icon 변경
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false); // maker 크기
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            markerOptions.title(pretime+" ~ "+curtime);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
            mMap.moveCamera(cameraUpdate);
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18));

            wait = 0;
        } else{
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            markerOptions.title(pretime+" ~ "+curtime);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
            mMap.moveCamera(cameraUpdate);
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
        }
        currentMarker[marker_cnt] = mMap.addMarker(markerOptions);
        marker_cnt++;
    }



    private void startLocationUpdates() //위치를 이동하면서 계속 업데이트하는 과정
    {
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());


            if (checkPermission())
                mMap.setMyLocationEnabled(true); // 현재위치 파란색 동그라미로 표시
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        if (checkPermission()) {
            Log.d(TAG, "CheckPermission");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mMap != null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {
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
        fab_develop = viewGroup.findViewById(R.id.fab_develop);

        Glide.with(this).load("https://i.imgur.com/n76lRoV.png").into(fab_home);
        Glide.with(this).load("https://i.imgur.com/ga31O56.png").into(fab_send);
        Glide.with(this).load("https://i.imgur.com/NUCaHI0.png").into(fab_cal);
        Glide.with(this).load("https://i.imgur.com/6D7W3f3.png").into(fab_develop);

        fab_open = AnimationUtils.loadAnimation(mContext, R.anim.fab_open);
        fab_close= AnimationUtils.loadAnimation(mContext, R.anim.fab_close);

        fab_more.setOnClickListener(listener);
        fab_home.setOnClickListener(listener);
        fab_send.setOnClickListener(listener);
        fab_cal.setOnClickListener(listener);
        fab_develop.setOnClickListener(listener);
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
                    showCalendar();
                    toggleFab();
                    break;
                case R.id.fab_develop:
                    onDevelopMode();
                    toggleFab();
                    break;
                default:
                    break;
            }
        }
    }

    private void onDevelopMode(){
        if (mode == false){
            mode = true;
            textView.setText("개발자 모드 ON");
            Toast.makeText(mContext, "개발자 모드 ON", Toast.LENGTH_SHORT).show();
        } else{
            mode = false;
            textView.setText("");
            Toast.makeText(mContext, "개발자 모드 OFF", Toast.LENGTH_SHORT).show();
        }
    }

    public void showCalendar(){
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout dialog = (LinearLayout) inflater.inflate(R.layout.dialog_calendar, null);

        long before2week = System.currentTimeMillis() - (24 * 60 * 60 * 1000) * 14;
        Date mDate = new Date(before2week);

        final MaterialCalendarView materialCalendarView = dialog.findViewById(R.id.calenderView);
        if (MainActivity.dateset == false){
            materialCalendarView.setSelectedDate(CalendarDay.today());
        } else{
            try {
                Date setDate = new SimpleDateFormat("yyyy-MM-dd").parse(MainActivity.markerDate);
                materialCalendarView.setSelectedDate(setDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (MyService.warnloc.size()!=0){
            try {
                for(int cnt=0; cnt<MyService.warnloc.size(); cnt++){
                    Date warnDate = new SimpleDateFormat("yyyy-MM-dd").parse(MyService.warnloc.get(cnt));
                    materialCalendarView.addDecorator(new EventDecorator(Color.YELLOW, Collections.singleton(CalendarDay.from(warnDate))));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (MyService.infloc.size()!=0){
            try {
                for(int cnt=0; cnt<MyService.infloc.size(); cnt++){
                    Date infDate = new SimpleDateFormat("yyyy-MM-dd").parse(MyService.infloc.get(cnt));
                    materialCalendarView.addDecorator(new EventDecorator(Color.RED, Collections.singleton(CalendarDay.from(infDate))));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        materialCalendarView.setSelectionColor(Color.GRAY);
        materialCalendarView.addDecorators(new SundayDecorator(), new SaturdayDecorator());
        materialCalendarView.state().edit()
                .setMinimumDate(mDate)
                .setMaximumDate(CalendarDay.today())
                .commit();

        final android.app.AlertDialog.Builder calendar = new android.app.AlertDialog.Builder(getContext());
        calendar.setView(dialog);
        calendar.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CalendarDay selectedDate = materialCalendarView.getSelectedDate();
                String strDate[] = selectedDate.toString().replaceAll("[^0-9|\\-]","").split("-");
                Log.d(TAG, "strDate : "+strDate[0]+" "+strDate[1]+" "+strDate[2]);
                String y = strDate[0];
                String m = strDate[1];
                String d = strDate[2];

                MainActivity.year = Integer.parseInt(y);
                MainActivity.month = Integer.parseInt(m);
                MainActivity.day = Integer.parseInt(d);
                MainActivity.dateset = true;

                String year_string = y;
                String month_string = String.format("%02d",MainActivity.month+1);
                String day_string = String.format("%02d",MainActivity.day);
                String dateMessage = year_string+"-"+month_string+"-"+day_string;
                MainActivity.markerDate = dateMessage;
                Log.d(TAG, "Select Date : "+dateMessage);

                markerUpdate();
            }
        });
        calendar.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        calendar.show();
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
                                nonot = true;
                                PrintWriter out = new PrintWriter(MyService.networKWriter, true);
                                data = pretime+"/"+curtime+"/"+Lat+"/"+Long;
                                out.println(data);
                                Log.d(TAG,"Send Data : "+data);
                            }
                        }.start();
                    }
                }

                if (data==""){
                    Toast.makeText(getContext(), "전송 할 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                } else{
                    InfectionChartFragment.state = true;
                    data = "";
                    Toast.makeText(getContext(), "데이터를 전송하였습니다.", Toast.LENGTH_SHORT).show();
                }
            } else{
                Toast.makeText(getContext(), "서버 상태 확인 및 1분 후 재시도하세요.", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(getContext(), "백그라운드 서비스를 먼저 시작해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void dialog_alert_sending(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("확진자 데이터 전송");
        builder.setMessage("코로나 양성 판정을 받으셨습니까?");
        builder.setIcon(R.drawable.virus);
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SendingService();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //아니오
                    }
                });
        builder.show();
    }

    private void dialog_home(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.home_menu, null);
        builder.setView(view);

        TextView title = view.findViewById(R.id.home_menu_title);
        title.setText("자주가는장소");

        final ListView listview = (ListView)view.findViewById(R.id.listview_alterdialog_list);
        final AlertDialog dialog = builder.create();

        SimpleAdapter simpleAdapter = new SimpleAdapter(getContext(), dialogItemList,
                R.layout.home_menu_item,
                new String[]{"img", "text"},
                new int[]{R.id.item_img, R.id.item_text});

        listview.setAdapter(simpleAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        setHome();
                        break;
                    case 1:
                        deleteHome();
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }
        });

        ImageButton close = view.findViewById(R.id.btn_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void deleteHome(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.home_menu, null);
        builder.setView(view);

        TextView title = view.findViewById(R.id.home_menu_title);
        title.setText("위치삭제");

        final List<String> list = new ArrayList<>();
        Cursor cursor = locationDB.rawQuery("SELECT name FROM "+tablehome, null);
        while(cursor.moveToNext()){
            String home = cursor.getString(0);
            list.add(home);
        }

        final ListView listview = view.findViewById(R.id.listview_alterdialog_list);
        final AlertDialog dialog = builder.create();

        ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, list);

        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String select_home = list.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("위치 삭제");
                builder.setMessage("'"+select_home+"' 장소를 삭제하시겠습니까?");
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try{
                                    locationDB.execSQL("DELETE FROM "+tablehome+" WHERE name='"+select_home+"'");
                                    Toast.makeText(getContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                }catch (Exception e){
                                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                                }
                                markerUpdate();
                            }
                        });
                builder.setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                builder.show();
                dialog.dismiss();
            }
        });

        ImageButton close = view.findViewById(R.id.btn_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void setHome(){
        final EditText edittext = new EditText(mContext);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        builder.setTitle("자주 가는 장소 설정");
        builder.setMessage("해당 장소의 이름을 입력하세요.");
        builder.setView(edittext);
        builder.setPositiveButton("입력",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String name = edittext.getText().toString();
                        try{
                            locationDB.execSQL("INSERT INTO "+tablehome+" VALUES('"+name+"', "+currentPosition.latitude+", "+currentPosition.longitude+")");
                            Toast.makeText(mContext,"저장이 완료되었습니다.",Toast.LENGTH_LONG).show();
                        } catch(Exception e){
                            Toast.makeText(mContext, "중복된 이름이 존재합니다.", Toast.LENGTH_SHORT).show();
                        }
                        markerUpdate();
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
        if(isFabOpen){
            fab_home.startAnimation(fab_close);
            fab_send.startAnimation(fab_close);
            fab_cal.startAnimation(fab_close);
            fab_develop.startAnimation(fab_close);

            fab_home.setClickable(false);
            fab_send.setClickable(false);
            fab_cal.setClickable(false);
            fab_develop.setClickable(false);

            fab_more.setImageResource(R.drawable.ic_add);
        }else{
            fab_home.startAnimation(fab_open);
            fab_send.startAnimation(fab_open);
            fab_cal.startAnimation(fab_open);
            fab_develop.startAnimation(fab_open);

            fab_home.setClickable(true);
            fab_send.setClickable(true);
            fab_cal.setClickable(true);
            fab_develop.setClickable(true);

            fab_more.setImageResource(R.drawable.ic_close);
        }
        isFabOpen=!isFabOpen;
    }

}

