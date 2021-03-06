package com.fanniemae.pingonetest;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.messaging.FirebaseMessaging;
import com.fanniemae.pingonetest.viewmodels.NetworkViewModel;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getCanonicalName();
    /*
     * view models separated by underlying fragments' logic
     * each used to communicate with corresponding fragment,
     * when activity is necessary
     */
    private NetworkViewModel networkViewModel;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logFcmRegistrationIdToken();
        setUpNetworkListeners();

        networkViewModel = new ViewModelProvider(this).get(NetworkViewModel.class);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

    }

    ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback(){
        @Override
        public void onAvailable(@NonNull Network network) {
            runOnUiThread(() -> networkViewModel.updateNetwork(true));
        }

        @Override
        public void onLost(@NonNull Network network) {
            runOnUiThread(() -> networkViewModel.updateNetwork(false));
        }
    };

    private void setUpNetworkListeners(){
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager!=null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(networkCallback);
            } else {
                NetworkRequest request = new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
                connectivityManager.registerNetworkCallback(request, networkCallback);
            }
        }
    }

    private void logFcmRegistrationIdToken() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("AuthenticatorSharedPreferences", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("FCM_TOKEN", null);
        if (token==null){
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    Log.d(TAG, "FCM Token = " + task.getResult());
                }
            });
        }else {
            Log.d(TAG, "FCM Token = " + token);
        }
    }
}
