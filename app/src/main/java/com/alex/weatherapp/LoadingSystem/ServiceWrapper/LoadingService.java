package com.alex.weatherapp.LoadingSystem.ServiceWrapper;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.util.Log;
import android.widget.Toast;

import com.alex.weatherapp.LoadingSystem.Facade4LoadingSystem;
import com.alex.weatherapp.LoadingSystem.ILoadingFacade;
import com.alex.weatherapp.LoadingSystem.LoadingSystem;
import com.alex.weatherapp.LoadingSystem.LocalStorage.ILocalStorageRequests;
import com.alex.weatherapp.LoadingSystem.LocalStorage.SQLiteStorage;
import com.alex.weatherapp.LoadingSystem.RetrofitLoadingSystem.RetrofitLoadingSystem;

public class LoadingService extends Service {

    class LoadingServiceBinder extends Binder {
        public ILoadingFacade getWrappedLoadingSystem(){
            return mLSF;
        }
        public void setConnectionCallback(ILoadingConnection.IConnectedCallback cb) {
            mStateChangedCallback = cb;
        }
    }

    public LoadingService() {
        super();
        mIsInitialized = false;
    }

    private void init(){
        mStateChangedCallback = null;
        Facade4LoadingSystem.Builder builder = new Facade4LoadingSystem.Builder();
        builder.setContext(getApplicationContext());
        ILocalStorageRequests cache = new SQLiteStorage.ForecastSchemaHelper(this);
        LoadingSystem retrofitLoaddingSystem = new RetrofitLoadingSystem();
        builder.setLocalCache(cache);
        builder.setNetworkLOadingSystem(retrofitLoaddingSystem);
        mLSF = builder.build();
    }


    @Override
    public void onDestroy() {
        if (!mIsInitialized) return;
        if (mLSF != null){
            mLSF.goOffline();
            mLSF.terminateThis();
        }
        Toast.makeText(getApplicationContext(), "OnDestroy()", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("LoadingService trace ", "LoadingService::onUnbind()");
        Toast.makeText(getApplicationContext(), "onUnbind()", Toast.LENGTH_SHORT).show();
        if (mStateChangedCallback != null) {
            mStateChangedCallback.onDisconnected();
            mStateChangedCallback = null;
        }
        return super.onUnbind(intent);
    }

    @Override
    public LoadingServiceBinder onBind(Intent intent) {
        if (!mIsInitialized) {
            mIsInitialized = true;
            init();
        }
        if (mStateChangedCallback != null){
            mStateChangedCallback.onConnected(mLSF);
        }
        return this.new LoadingServiceBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    private Facade4LoadingSystem mLSF;
    /* I wanted to perform intialization in constructor, but android threw an exception
    when attempting to register broadcast receiver. I think, it is because service wasn't
    ready. Workaround: introduce flag and initialize during the first binding request
     */
    private boolean mIsInitialized;

    private ILoadingConnection.IConnectedCallback mStateChangedCallback;
}
