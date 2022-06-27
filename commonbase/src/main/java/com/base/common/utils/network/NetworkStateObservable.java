package com.base.common.utils.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;

import static com.base.common.utils.network.InternetUtilKt.hasInternetConnection;

public class NetworkStateObservable extends MutableLiveData<Boolean> implements LifecycleObserver {

    private Context context;
    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                boolean isConnected = hasInternetConnection(context);
                if (getValue() == null || getValue() != isConnected) {
                    setValue(isConnected);
                }
            }
        }
    };

    public NetworkStateObservable(@NonNull Context context) {
        this.context = context;
        if (!hasInternetConnection(context)) {
            setValue(false);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void startObserveNetwork() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(networkStateReceiver, intentFilter);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void stopObserveNetwork() {
        context.unregisterReceiver(networkStateReceiver);
    }

}
