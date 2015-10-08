package com.alex.weatherapp.LoadingSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 05.09.2015.
 */
public abstract class LoadingSystem implements  IRequestExecutor {

    public LoadingSystem() {
        init();
    }

    protected  void init() {
        mRegisteredExecutors = new ArrayList<>();
    }

    /* register all executors supported by derived system, call this in dericed class
     * when system is ready  */
    protected abstract void addAllSupportedExecutors();


    public void addExecutor(IRequestExecutor exec) {
        if (exec != null)
            mRegisteredExecutors.add(exec);
    }

    @Override
    public void execute(RequestAbstract request, ICallback callback) throws IllegalStateException
    {
        if (mRegisteredExecutors == null)
            throw new IllegalStateException("This loading system has no registered executors yet");
        boolean isHandled = false;
        IResponse result = null;
        for (IRequestExecutor exec : mRegisteredExecutors) {
            try {
                 exec.execute(request, callback);
                /* reachable only when there were no mismatch exception thrown */
                isHandled = true;
                break;
            }
            catch (IllegalStateException ise) {
                /* Something is wrong in an executor itself, pass it up */
                throw ise ;
            }
            catch (RequestExecutorTypeMismatchExceptioin wrongExecExceptioin) {
                /* This executor can't handle this type of request, try with the next one  */
                continue;
            }
        }
        if (!isHandled)
            throw new IllegalStateException("failure, request hasn't been processed");
    }

    @Override
    public IResponse execute(RequestAbstract request) throws IllegalStateException {
        if (mRegisteredExecutors == null)
            throw new IllegalStateException("This loading system has no registered executors yet");
        boolean isHandled = false;
        IResponse result = null;
        for (IRequestExecutor exec : mRegisteredExecutors) {
            try {
                result = exec.execute(request);
                /* reachable only when there were no mismatch exception thrown */
                isHandled = true;
                break;
            }
            catch (IllegalStateException ise) {
                /* Something is wrong in an executor itself, pass exception up */
                throw ise ;
            }
            catch (RequestExecutorTypeMismatchExceptioin wrongExecExceptioin) {
                /* This executor can't handle this type of request, try with the next one  */
                continue;
            }
        }
        if (!isHandled)
            throw new IllegalStateException("failure, request hasn't been processed");
        return  result;
    }

    /**
     * Represents the chain of responsibilities (executors)
     */
    protected List<IRequestExecutor> mRegisteredExecutors;
}
