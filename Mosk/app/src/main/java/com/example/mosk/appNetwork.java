package com.example.mosk;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class appNetwork extends BroadcastReceiver {

    private static final String TAG = "Log";
    private Activity activity;

    public appNetwork() {
        super();
    }
    public appNetwork(Activity activity) {
        this.activity = activity;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action= intent.getAction();

        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            try {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
                NetworkInfo _wifi_network = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if(_wifi_network != null) {
                    // wifi, 3g 둘 중 하나라도 있을 경우
                    Log.d(TAG, "network 1");
                    if(_wifi_network != null && activeNetInfo != null){
                        Log.d(TAG, "network 3");
                    } else{
                        // wifi, 3g 둘 다 없을 경우
                        Log.d(TAG, "network 4");
                    }
                } else{
                    Log.d(TAG, "network 2");
                }
            } catch (Exception e) {
                Log.i("ULNetworkReceiver", e.getMessage());
            }
        }
    }
}