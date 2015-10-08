package com.alex.weatherapp.LoadingSystem.ServiceWrapper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.alex.weatherapp.LoadingSystem.ILoadingFacade;

/**
 * Created by Alex on 27.09.2015.
 */
public class ConnectionToLoadingSystem implements ILoadingConnection {

    class LoadingServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LoadingService.LoadingServiceBinder lsb = null;
            try {
                lsb = (LoadingService.LoadingServiceBinder) service;
                mBinder = lsb;
            }catch (ClassCastException e){
                Log.d("Exception", "Exception in LoadingServiceConnection: wrong binder type");
                return;
            }
            ILoadingFacade sys = lsb.getWrappedLoadingSystem();
            lsb.setConnectionCallback(ConnectionToLoadingSystem.this.mConnectedCallback);
            mConnectedSystem = sys;
            if (mConnectedCallback != null){
                mConnectedCallback.onConnected(sys);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mConnectedCallback.onDisconnected();
            mConnectedSystem = null;
            mBinder = null;
        }
    }
    public ConnectionToLoadingSystem(Context context) {
        mContext = context;
        mCurrentConnection = null;
        mBinder = null;
    }
    public ConnectionToLoadingSystem(Context context, IConnectedCallback callback) {
        mCurrentConnection = null;
        mContext = context;
        setOnConnectedCallback(callback);
    }

    @Override
    public boolean connect() {
        if (mCurrentConnection != null) {
            return false;
        }
        Intent intent = new Intent(mContext, LoadingService.class);
        mCurrentConnection = this.new LoadingServiceConnection();
        mContext.bindService(intent, mCurrentConnection, Context.BIND_AUTO_CREATE);
        return true;
    }

    @Override
    public boolean disconnect() {
        if (mCurrentConnection == null){
            return false;
        }
        mContext.unbindService(mCurrentConnection);
        mCurrentConnection = null;
        return true;
    }

    @Override
    public boolean isConnected() {
        return mConnectedSystem != null && mCurrentConnection != null;
    }

    @Override
    public void setOnConnectedCallback(IConnectedCallback callback) {
        mConnectedCallback = callback;
        if (mBinder != null) {
            mBinder.setConnectionCallback(callback);
        }
    }

    public ILoadingFacade getConnectedSystem(){
        return mConnectedSystem;
    }
    private Context mContext;
    LoadingServiceConnection mCurrentConnection;
    /* Is kept to be able to change state change listener */
    LoadingService.LoadingServiceBinder mBinder;
    private ILoadingConnection.IConnectedCallback mConnectedCallback;
    private ILoadingFacade mConnectedSystem;
}

