package com.example.mosk;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MyService extends Service {

    public static Intent serviceIntent;
    public String TAG = "moskLog";

    //SQLite
    SQLiteDatabase locationDB;
    private final String dbname = "Mosk";
    private final String tablename = "location";

    //GPS
    private String preTime;
    private double Latitude, Longitude;
    private double pre_lat = 0.0, pre_lng = 0.0;
    private LocationManager lm;
    private Location location;
    private static final int std_distance = 30; // 기준 거리

    //Socket
    private Socket socket;
    private BufferedReader networkReader;
    public static BufferedWriter networKWriter;
    private String recv_data;
    private String ip = "220.122.46.204";
    private int port = 8001;

    //State
    public static int infstate = 0;

    //Notification
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private NotificationManager mNotificationManager;
    private static final int NOTIFICATION_ID = 0;

    //Location Infected
    public static ArrayList<String> infloc = new ArrayList<String>();

    @Override
    public void onCreate() {
        super.onCreate();

        locationDB = this.openOrCreateDatabase(dbname, MODE_PRIVATE, null);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission
        }
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location != null) {
            Latitude = location.getLatitude();
            Longitude = location.getLongitude();

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, gpsLocationListener); //Location Update
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, gpsLocationListener);
        }

        createNotificationChannel();

        sThread.start();
        mThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceIntent = intent;

        initializeNotification();

        return START_STICKY;
    }

    private Thread sThread = new Thread("Socket thread"){
        @Override
        public void run() {
            while (true) {
                try {
                    setSocket(ip, port); // 서버 소켓 생성
                    Log.d(TAG, "Make Socket !");

                    while (true) {
                        recv_data = networkReader.readLine(); // 데이터 수신
                        Log.d(TAG, "Recv Data : "+recv_data);
                        String datalist[] = recv_data.split("/");
                        double infLat = Double.parseDouble(datalist[2]);
                        double infLong = Double.parseDouble(datalist[3]);

                        Cursor cursor = locationDB.rawQuery("SELECT * FROM "+tablename+" WHERE preTime<='"+datalist[1]+"' AND curTime>='"+datalist[0]+"'", null);
                        while(cursor.moveToNext()){
                            String pretime = cursor.getString(0);
                            String curtime = cursor.getString(1);
                            double myLat = cursor.getDouble(2);
                            double myLong = cursor.getDouble(3);

                            double distance = 0.0;
                            distance = getDistance(infLat, infLong, myLat, myLong);

                            if (distance<std_distance && MapViewFragment.nonot == false){
                                long diff = 0;
                                long sec = 0;

                                infloc.add(pretime);

                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:dd");
                                Date preDate = dateFormat.parse(pretime);
                                Date curDate = dateFormat.parse(curtime);
                                Date preInf = dateFormat.parse(datalist[0]);
                                Date curInf = dateFormat.parse(datalist[1]);

                                if(preInf.after(preDate)){
                                    if(curInf.after(curDate)){
                                        diff = curDate.getTime() - preInf.getTime();
                                    } else{
                                        diff = curInf.getTime() - preInf.getTime();
                                    }
                                } else{
                                    diff = curInf.getTime() - preDate.getTime();
                                }
                                sec = diff/1000;

                                if (sec<3600){
                                    Log.d(TAG, "1시간 미만 동안 겹침");
                                    infstate = 2;
                                } else if (sec>=3600 & sec<10800){
                                    Log.d(TAG, "1~3시간 동안 겹침");
                                    infstate = 3;
                                } else{
                                    Log.d(TAG, "3시간 이상 동안 겹침");
                                    infstate = 4;
                                }
                                warningNotification(R.drawable.warning, infstate);
                                break;
                            }
                        }

                        if (infstate == 0){
                            Cursor cursor2 = locationDB.rawQuery("SELECT * FROM "+tablename+" WHERE preTime<=datetime('"+datalist[1]+"','+1 hours')", null);
                            while(cursor2.moveToNext()){
                                double myLat = cursor2.getDouble(2);
                                double myLong = cursor2.getDouble(3);

                                double distance = 0.0;
                                distance = getDistance(infLat, infLong, myLat, myLong);

                                if (distance<std_distance && MapViewFragment.nonot == false){
                                    infstate = 1;
                                    warningNotification(R.drawable.warning2, infstate);
                                    Log.d(TAG, "동선 겹칠 뻔");
                                    break;
                                }
                            }
                        }

                        if (recv_data == null) {
                            networKWriter = null;
                            break;
                        }
                    }
                    MapViewFragment.nonot = false;
                } catch (IOException | ParseException e) {
                    if (sThread == null){
                        Log.d(TAG, "sThread Exit");
                        break; // 스레드를 종료해도 while문이 작동하는 현상 해결
                    } else{
                        try {
                            Log.d(TAG, "Socket Connection Wait..");
                            networKWriter = null;
                            sleep(60000); // 서버와 연결이 안되면, 주기적으로 서버와 연결을 요청함
                        } catch (InterruptedException interruptedException) {
                            Log.d(TAG, "sThread Error");
                            interruptedException.printStackTrace();
                        }
                    }
                }
            }
        }
    };

    private Thread mThread = new Thread("My thread") {
        @Override
        public void run() {
            while (true){
                try{

                    Log.d(TAG, "----------------------------");
                    Log.d(TAG, "이전 위치: "+pre_lat+" "+pre_lng);
                    Log.d(TAG, "최근 위치: "+Latitude+" "+Longitude);

                    double distance = 0.0;
                    distance = getDistance(pre_lat, pre_lng, Latitude, Longitude);

                    // DB 데이터 확인
                    Cursor cursor = locationDB.rawQuery("SELECT * FROM "+tablename+" WHERE curTime is NULL LIMIT 1", null);
                    int cnt = cursor.getCount();
                    Log.d(TAG, "MyLocation cnt : "+cnt);

                    if (distance < std_distance && cnt == 0){
                        try{
                            locationDB.execSQL("UPDATE "+tablename+" SET"+" curTime = null WHERE preTime ='"+preTime+"'");
                            Log.d(TAG, "Location Improvement..");
                        } catch (Exception e){
                            Log.d(TAG,"Error");
                        }
                    }

                    // DB 데이터 확인
                    Cursor cursor2 = locationDB.rawQuery("SELECT * FROM "+tablename+" WHERE curTime is NULL LIMIT 1", null);
                    cnt = cursor2.getCount();
                    Log.d(TAG, "cnt = "+cnt);

                    if (distance < std_distance && cnt == 0){
                        //현재시간 가져오기
                        long now = System.currentTimeMillis();
                        Date mDate = new Date(now);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        preTime = simpleDateFormat.format(mDate);
                        locationDB.execSQL("INSERT INTO "+tablename+"(preTime, Latitude, Longitude) VALUES('"+preTime+"', "+pre_lat+", "+pre_lng+")");
                        Log.d(TAG, "거리: "+distance+" // 최초저장");
                        Log.d(TAG, "최초저장 시간 : "+preTime);
                    } else if (distance > std_distance && distance < 99999 && cnt == 1){
                        locationDB.execSQL("UPDATE "+tablename+" SET"+" curTime = datetime('now', 'localtime')"+" WHERE curTime is NULL");
                        Log.d(TAG, "거리: "+distance+" // 위치저장");
                    } else if((distance > std_distance && cnt == 0) || pre_lat == 0.0){
                        Log.d(TAG, "거리: "+distance+" // 이동 중..");
                        preTime = null;
                        pre_lat = Latitude;
                        pre_lng = Longitude;
                    } else{
                        Log.d(TAG, "거리: "+distance+" // 동선 저장 중..");
                    }

                    Log.d(TAG, "pre_location: "+pre_lat+" "+pre_lng);

                    // DB 데이터 확인
                    Cursor cursor3 = locationDB.rawQuery("SELECT * FROM "+tablename, null);
                    while(cursor3.moveToNext()){
                        String pretime = cursor3.getString(0);
                        String curtime = cursor3.getString(1);
                        double Lat = cursor3.getDouble(2);
                        double Long = cursor3.getDouble(3);
                        Log.d(TAG,"Store Data : "+pretime+" "+curtime+" "+Lat+" "+Long);
                    }

                    sleep(300000);

                } catch (InterruptedException e){
                    Log.d(TAG, "mThread Error");
                    break;
                }
            }
        }
    };

    final LocationListener gpsLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Latitude = location.getLatitude();
            Longitude = location.getLongitude();
            Log.d(TAG, "Location Update : "+Latitude+" "+Longitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(@NonNull String provider) {}

        @Override
        public void onProviderDisabled(@NonNull String provider) {}
    };

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

    public void initializeNotification(){
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
        builder.setSmallIcon(R.drawable.app_icon_small);

        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText("설정을 보려면 누르세요.");
        style.setBigContentTitle(null);
        style.setSummaryText("서비스 동작중");
        builder.setContentText("서비스 동작 중");
        builder.setContentTitle("Mosk");
        builder.setOngoing(true);
        builder.setStyle(style);
        builder.setWhen(0);
        builder.setShowWhen(false);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        // notificationIntent.putExtra("ExtraFragment","Notification");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel("1", "undead_service", NotificationManager.IMPORTANCE_NONE));
        }
        Notification notification = builder.build();
        startForeground(1, notification);
    }

    public void createNotificationChannel()
    {
        //notification manager 생성
        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        // 기기(device)의 SDK 버전 확인 ( SDK 26 버전 이상인지 - VERSION_CODES.O = 26)
        if(android.os.Build.VERSION.SDK_INT
                >= android.os.Build.VERSION_CODES.O){
            //Channel 정의 생성자( construct 이용 )
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID
                    ,"Test Notification",mNotificationManager.IMPORTANCE_HIGH);
            //Channel에 대한 기본 설정
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification from Mascot");
            // Manager을 이용하여 Channel 생성
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

    }

    // Notification Builder를 만드는 메소드
    private NotificationCompat.Builder getNotificationBuilder(int drawble, int infstate) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID);
        builder.setSmallIcon(drawble);
        if (infstate == 1){
            builder.setContentText("확진자와 동선이 겹쳤을 수 있습니다.");
            builder.setContentTitle("경고");
        } else{
            builder.setContentText("확진자와 동선이 겹쳤습니다.");
            builder.setContentTitle("위험");
        }
        builder.setAutoCancel(true);
        builder.setVibrate(new long[]{1000,1000});
        builder.setWhen(0);
        builder.setShowWhen(true);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);
        return builder;
    }

    // Notification을 보내는 메소드
    public void warningNotification(int drawable, int infstate){
        // Builder 생성
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder(drawable, infstate);
        // Manager를 통해 notification 디바이스로 전달
        mNotificationManager.notify(NOTIFICATION_ID,notifyBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestory");
        Toast.makeText(this, "onDestory!", Toast.LENGTH_SHORT).show();

        if (serviceIntent != null){
            setAlarmTimer();
        }

        if (mThread != null){
            mThread.interrupt();
            mThread = null;
        }

        if (sThread != null){
            sThread.interrupt();
            sThread = null;
        }

        if (socket != null){
            try {
                socket.close();
                networKWriter = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        lm.removeUpdates(gpsLocationListener);
        locationDB.close();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        setAlarmTimer();
    }

    protected void setAlarmTimer(){
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, 1);
        Intent intent = new Intent(this, AlarmRecever.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0,intent,0);

        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setSocket(String ip, int port) throws IOException{
        socket = new Socket(ip,port);
        networKWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        networkReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
}
