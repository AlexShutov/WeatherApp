package com.alex.weatherapp.LoadingSystem.NetworkStateListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStateListener extends BroadcastReceiver implements
        INetStateListenerControl{
    private static final String sBroadcastAction = ConnectivityManager.CONNECTIVITY_ACTION;
    private enum ConnectionType {
        WiFi,
        Cellular,
        Unknown
    }
    private enum ConnectionState {
        Offline,
        Online
    }

    public NetworkStateListener(Context context) {
        mContext = context;
        mConnectionState = ConnectionState.Offline;
        mConnectionType = ConnectionType.Unknown;
    }

    @Override
    public void setFeedback(INetStateListenerFeedback feedback){
        mFeedback = feedback;
    }

    @Override
    public void startListening() {
        IntentFilter intentFilter = new IntentFilter(sBroadcastAction);
        mContext.registerReceiver(this, intentFilter);
    }

    @Override
    public void stopListening() {
        mContext.unregisterReceiver(this);
    }

    /**
     * Feedback may not be installed at the time constructo runs, so call this
     * method for initial lookup manualy first time (when feedback is ready)
     */
    @Override
    public void forceStateChecking() {
        checkNetworkState();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        checkNetworkState();
    }

    protected void checkNetworkState() {
        String service = Context.CONNECTIVITY_SERVICE;
        ConnectivityManager connectivity =
                (ConnectivityManager) mContext.getSystemService(service);
        NetworkInfo activeNetworkInfo = connectivity.getActiveNetworkInfo();
        /* if there are no active connection */
        if (activeNetworkInfo == null) {
            if (mConnectionState != ConnectionState.Offline) {
                /* phone goes offline, inform about it */
                mConnectionState = ConnectionState.Offline;
                mFeedback.onOffline();
            }
            return;
        }
        /* Check if device goes online (was offline before) */
        if(mConnectionState == ConnectionState.Offline) {
            mConnectionState = ConnectionState.Online;
            mFeedback.onOnline();
        }
        int networkType = activeNetworkInfo.getType();
        switch (networkType){
            case ConnectivityManager.TYPE_MOBILE:
                if (mConnectionType != ConnectionType.Cellular) {
                    mFeedback.onCellularAvailible();
                    mConnectionType = ConnectionType.Cellular;
                }
                break;
            case ConnectivityManager.TYPE_WIFI:
                if (mConnectionType != ConnectionType.WiFi){
                    mFeedback.onWiFiAvailible();
                    mConnectionType = ConnectionType.WiFi;
                }
                break;
        }
    }

    private ConnectionType mConnectionType;
    private ConnectionState mConnectionState;
    private Context mContext;
    /* just in case */
    private INetStateListenerFeedback mFeedback;
}
