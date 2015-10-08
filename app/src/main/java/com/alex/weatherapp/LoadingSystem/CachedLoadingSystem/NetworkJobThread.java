package com.alex.weatherapp.LoadingSystem.CachedLoadingSystem;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.alex.weatherapp.LoadingSystem.IResponse;
import com.alex.weatherapp.LoadingSystem.LoadingSystem;

/**
 * Created by Alex on 22.09.2015.
 */
public class NetworkJobThread extends Thread implements INetworkJobExecutor {
    public static final int PROCESS_NETWORK_JOB = 2;

    public NetworkJobThread(LoadingSystem loadingSystem,
                            Handler callingThreadHandler) {
        super("NetworkJobThread");
        mUnderlyngLoadingSystem = loadingSystem;
        mCallingThreadHandler = callingThreadHandler;
    }

    /**
     * Handler isn't static because it will be destroyed upon thread termination,
     * so we don't need to worry about memory leaks
     */
    private class NJHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            StateOfExecution state = null;
            IResponse response = null;
            boolean errorOccured = false;
            switch (msg.what){
                case PROCESS_NETWORK_JOB:
                    state = (StateOfExecution) msg.obj;
                    if (state.isDone || state.isHandledOnNetwork) return;
                    try {
                        response = mUnderlyngLoadingSystem.execute(state.request);
                    }catch (Exception e) {
                        errorOccured = true;
                    }
                    break;
            }
            if (errorOccured) {
                state.isHandledOnNetwork = false;
                state.networkResponse = null;
            } else {
                state.isHandledOnNetwork = true;
                state.networkResponse = response;
            }
            getLoadingStrategy().onNetworkResponse(state);
        }
    };

    @Override
    public void enqueueNetworkJob(StateOfExecution state) {
        if (state == null) return;
        Message msg = mHandler.obtainMessage(PROCESS_NETWORK_JOB, state);
        mHandler.sendMessage(msg);
    }

    @Override
    public void run() {
        super.run();
        Looper.prepare();
        mHandler = this.new NJHandler();
        Looper.loop();
    }

    public ILoadingStrategy getLoadingStrategy() { return mLoadingStrategy;}
    public void setLoadingStrategy(ILoadingStrategy strategy){ mLoadingStrategy = strategy;}

    public Handler getCallingThreadHandler() { return  mCallingThreadHandler;}
    public void setmCallingThreadHandler(Handler hndl){ mCallingThreadHandler = hndl;}

    /* for process termination only */
    public Handler getHandler() { return mHandler;}

    /* The loading system which is stored somewhere else and is used here for
     * consecutive task execution on net. I don't know whether retrofit is capable of concurrent
      * execution. At least, it uses method enqueue, so it's supposed to be consecutive, too */
    private LoadingSystem mUnderlyngLoadingSystem;
    /**
     * The network thread need to know about current strategy to inform it about
     * operation completion
     */
    private ILoadingStrategy mLoadingStrategy;

    private Handler mCallingThreadHandler;
    private NJHandler mHandler;
}
