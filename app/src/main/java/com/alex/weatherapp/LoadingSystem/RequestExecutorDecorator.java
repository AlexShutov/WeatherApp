package com.alex.weatherapp.LoadingSystem;

/**
 * Created by Alex on 07.09.2015.
 */

/**
 *  It is used for request result data format conversion. Network updates returns result in
 *  WUnderground format, but it must be transformed to proram's inner format, having no
 *  redundant fields and convenient for storing in a local storage (db).
 *  For achieving that this decorator is employed.
 */
public class RequestExecutorDecorator implements IRequestExecutor {

    protected static class CallbackWrapper implements  ICallback {
        public CallbackWrapper(ICallback callback, IResultDecorator dec) {
            mWrappedCallback = callback;
            mDecorator = dec;
        }
        @Override
        public void onResult(IResponse response) {
            IResponse modifiedResponse = response;
            try {
                modifiedResponse = mDecorator.decorate(response);
            }catch (IllegalArgumentException e) {
                /* failure,decorator doesn't support this kind of data, this code is reached
                 because of a coding error */
                mWrappedCallback.onResult(response);
                return;
            }
            mWrappedCallback.onResult(modifiedResponse);
        }

        public IResultDecorator mDecorator;
        public ICallback mWrappedCallback;
    }
    public RequestExecutorDecorator() {
        mDecorator = null;
        mExecutor = null;
    }

    @Override
    public IResponse execute(RequestAbstract request) throws RequestExecutorTypeMismatchExceptioin,
            IllegalStateException
    {
        if (mExecutor == null) throw new  IllegalStateException("Decorator: Executor isn't set, aborting");
        IResponse result = mExecutor.execute(request);
        if (mDecorator != null) {
            try {
                result = mDecorator.decorate(result);
            }
            catch (IllegalArgumentException e) {
                /* decoration failed, silently ignore it */
            }
        }
        return  result;
    }

    @Override
    public void execute(RequestAbstract request, ICallback callback) throws RequestExecutorTypeMismatchExceptioin,
            IllegalStateException
    {
        if (mExecutor == null) throw new IllegalStateException("Decorator: exector isn't set, aborting");
        if (mDecorator != null) {
            CallbackWrapper wrappedCallback = new CallbackWrapper(callback, mDecorator);
            mExecutor.execute(request, wrappedCallback);
        } else {
            mExecutor.execute(request, callback);
        }
    }

    public void setExecutor(IRequestExecutor exec) {
        if (exec != null) {
            mExecutor = exec;
        }
    }

    public IRequestExecutor getmExecutor() { return  mExecutor;}

    public void setDecorator(IResultDecorator decorator) {
        if (decorator != null) mDecorator = decorator;
    }
    public IResultDecorator getmDecorator() { return  mDecorator;}

    private IRequestExecutor mExecutor;
    private IResultDecorator mDecorator;
};
