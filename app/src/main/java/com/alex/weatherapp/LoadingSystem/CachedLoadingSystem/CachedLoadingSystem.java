package com.alex.weatherapp.LoadingSystem.CachedLoadingSystem;


import android.os.*;
import android.os.Process;

import com.alex.weatherapp.LoadingSystem.ICallback;
import com.alex.weatherapp.LoadingSystem.IRequestExecutor;
import com.alex.weatherapp.LoadingSystem.IResponse;
import com.alex.weatherapp.LoadingSystem.LoadingSystem;
import com.alex.weatherapp.LoadingSystem.LocalStorage.ILocalStorageRequests;
import com.alex.weatherapp.LoadingSystem.RequestAbstract;
import com.alex.weatherapp.LoadingSystem.RequestExecutorTypeMismatchExceptioin;


/**
 * Created by Alex on 17.09.2015.
 */

/**
 * This class has the same contract,as LoadingSystem, but here Executors doesn't execute
 * request, but filters and redirects them. CachedLoadingSystem is responsible for concurrency
 * and execution behaviour. It has LoadingSystem, responsible for work with network and implementation
 * of ILocalStorageRequests for local storage support, e.g SQLite. It is logical to have cache
 * functionality organized in form of LoadingSystem, but local interface is much wider, that's why it
 * leads to a lot of boilerplate executors. So I decided to adress db directly. Here I don't use
 * ContentProvider in SQLite implementation because cached data doesn't supposed to be shared
 * without validation, but it may be convenient to use AsuncQueryHandler, though. Sequential task
 * queuing and execution on a worker thread is used, so it's always synchronized, no need in content
 * provider.
 */

public class CachedLoadingSystem  extends LoadingSystem{

    /**
     * After supported request is intercepted (unsupported is handed off to network execution),
     * we need to give that request to something. CachedLoadingSystem already implements
     * IRequestExecutor, so this class instance is a 'void Main()' for executing appropriate
     * request
     */
    public class ExecutionEntryPoint implements IRequestExecutor {
        @Override
        public IResponse execute(RequestAbstract request) throws RequestExecutorTypeMismatchExceptioin, IllegalStateException {
            // Sorry, but this class supports only asynchronous execution
            return null;
        }

        @Override
        public void execute(RequestAbstract request, ICallback callback) throws RequestExecutorTypeMismatchExceptioin, IllegalStateException {
            mLoadingFacility.initialRequest(request, callback);
        }
    }


    /**
     * TODO: figure out how to use a Dagger for constructor injection
     * @param networkLoader
     * @param cacheStorage
     */
    public CachedLoadingSystem(LoadingSystem networkLoader,
                         ILocalStorageRequests cacheStorage,
                               Handler callingThreadHandler) {
        super();
        mCallingThreadHandler = callingThreadHandler;
        mNetworkLoadingSystem = networkLoader;
        mCacheStorage =  cacheStorage;
        /**
         * mDoNetworkUpdateWhenPossible : this flag is cleared by default. If set, it would
         * force loading system to do network updates all the time, even if it isn't
         * necessary(cache has actual data)
         */
        mDoNetworkUpdateWhenPossible = false;
        mEntryPoint = this.new ExecutionEntryPoint();
        initAndBindComponents();
        /*
        Interceptor executor uses entry point
         */
        addAllSupportedExecutors();
    }

    /**
     * Stops both network and control threads
     */
    public void shutdownNow() {
        mNetworkJobThread.getHandler().getLooper().quit();
        LoadingFacility conrolThread = (LoadingFacility) mLoadingFacility;
        conrolThread.getThreadHandler().getLooper().quit();
    }

    public void switchExecutionStrategy(ILoadingStrategy newStrategy) {
        if (newStrategy == null) return;
        LoadingFacility conrolThread = (LoadingFacility) mLoadingFacility;

        mLoadingStrategy = newStrategy;
        mLoadingStrategy.registerLoadingFacility(mLoadingFacility);
        mLoadingFacility.registerLoadingStrategy(newStrategy);
        mNetworkJobThread.setLoadingStrategy(newStrategy);
    }

    protected void initAndBindComponents() {
        /* create and register mock loading facility and handle strategy
         */
        LoadingFacility conrolThread = new LoadingFacility(this, mCallingThreadHandler);
        mLoadingFacility = conrolThread;
        mLoadingStrategy = new LoadingStrategy();
        mLoadingStrategy.registerLoadingFacility(mLoadingFacility);
        mLoadingFacility.registerLoadingStrategy(mLoadingStrategy);
        /**network thread updates control thread, so we need to pass its handler, but we don't know
         * it yet, so we will set it in accessor, but before that force launch control thread and
         * force network thread to sleep. when handler is set, wake up network thread to let it
         * accept the messages
         */
        mNetworkJobThread = new NetworkJobThread(mNetworkLoadingSystem, null);
        mNetworkJobThread.setLoadingStrategy(mLoadingStrategy);
        /* set background priority to not to disrupt the ui thread */
        mNetworkJobThread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        conrolThread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        /* start processes */
        conrolThread.start();
        mNetworkJobThread.start();
        Handler controlThreadHndlr = conrolThread.getThreadHandler();
        mNetworkJobThread.setmCallingThreadHandler(controlThreadHndlr);
    }



    @Override
    protected void addAllSupportedExecutors() {
        /*
        Request inteceptor must be ahead of dispatcher in the list, so it handles
        requests first
         */
        RequestInterceptor interceptor = new RequestInterceptor(mEntryPoint);
        addExecutor(interceptor);

        /*
        create and register request dispatcher
         */
        RequestDispatcher dispatcher = new RequestDispatcher(this);
        addExecutor(dispatcher);
    }

    public boolean getNetworkUpdateFlag(){
        return mDoNetworkUpdateWhenPossible;
    }
    public void setNetworkUpdateFlag(boolean flag) {
        mDoNetworkUpdateWhenPossible = flag;
    }

    /** Accessors,
     * Loader sound like Loader in android, that's why
     * loading system
     * @return
     */
    public LoadingSystem getNetworkLoadingSystem() {
        return  mNetworkLoadingSystem;
    }
    public ILocalStorageRequests getCurrentCache() {
        return  mCacheStorage;
    }

    public INetworkJobExecutor getNetworkThread(){ return  mNetworkJobThread;}

    private LoadingSystem mNetworkLoadingSystem;
    private ILocalStorageRequests mCacheStorage;


    private ILoadingFacility mLoadingFacility;
    private ILoadingStrategy mLoadingStrategy;
    private NetworkJobThread mNetworkJobThread;

    private boolean mDoNetworkUpdateWhenPossible;
    private ExecutionEntryPoint mEntryPoint;
    /** It is used to post callbacks */
    private Handler mCallingThreadHandler;
}
