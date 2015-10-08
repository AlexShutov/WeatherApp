package com.alex.weatherapp.MVP;

import com.alex.weatherapp.LoadingSystem.ILoadingFacade;
import com.alex.weatherapp.LoadingSystem.ServiceWrapper.ILoadingConnection;

/**
 * Created by Alex on 02.10.2015.
 */

/**
 * The presenter carries only view-model interaction logic, and is not supposed to be derived
 * from any other system class. Interaction logic interfaces is represented by
 * IPresenterContract and IViewContract, so this class wraps model-presenter-view binding lifecycle
 * methods, such as connect, disconnect and state accessors. By the way, compared to
 * IPresenter, IView must interact only with a presenter, so there is no need in
 * moving method responsible for attaching IView to IPresenter in a separate base class.
 * */

public abstract class PresenterBase implements IPresenter {

    /** methods of IPresenter lifecycle
     */

    private class ConnectionEstablishedCallback implements ILoadingConnection.IConnectedCallback {
        @Override
        public void onConnected(ILoadingFacade mConnectedSystem) {
            mIsModelReady = true;
            mModelContract = mConnectedSystem;
            onModelConnected();
            if (mReadyCallback != null){
                mReadyCallback.onPresenterReady(PresenterBase.this);
            }
            mIsPresenterReady = true;
        }

        @Override
        public void onDisconnected() {
            mIsModelReady = false;
            mModelContract = null;
            mModelConnection = null;
            onModelDisconnected();
            mIsPresenterReady = false;
        }
    }

    public PresenterBase() {
        init();
    }

    public void init(){
        mAttachedModel = null;
        mAttachedView = null;
        mModelConnection = null;
        mModelContract = null;
        mIsViewReady = false;
        mIsModelReady = false;
        mIsPresenterReady = false;
    }

    @Override
    public void tapIntoModel(IModel model) {
        if (model == null){
            mModelConnection.disconnect();
        }
        mAttachedModel = model;
        /** will be set in callback */
        mIsModelReady = false;
        mModelConnection = model.getServiceConnection();
        mModelConnection.setOnConnectedCallback(this.new ConnectionEstablishedCallback());
        mModelConnection.connect();
    }

    @Override
    public void setPresenterReadyCallback(IPresenterReady cb) {
        mReadyCallback = cb;
    }

    @Override
    public boolean isModelReady() {
        return mIsModelReady;
    }

    @Override
    public boolean isPresenterReady() {return mIsPresenterReady; }

    @Override
    public boolean disconnectFromModel() {
        if (!isModelReady()){
            return false;
        }
        if (mModelConnection == null || !mModelConnection.isConnected()){
            return  false;
        }
        mModelConnection.disconnect();
        return true;
    }

    @Override
    public void setView(IView view) {
        disconnectView(view);
        mAttachedView = view;
        mIsViewReady = true;
        view.connectToPresenter(this);
    }

    @Override
    public boolean isViewReady() {
        if (!mIsViewReady || mAttachedView == null ||
                !mAttachedView.isUIReady()){
            return false;
        }
        return true;
    }

    @Override
    public void disconnectView(IView view) {
        setPresenterReadyCallback(null);
        if (mIsViewReady && mAttachedView != null) {
            if (mAttachedView != view){
                return;
            }
            mAttachedView.connectToPresenter(null);
            mAttachedView = null;
            mIsViewReady = false;
        }
    }

    private IPresenterReady mReadyCallback;
    private boolean mIsPresenterReady;

    protected ILoadingConnection mModelConnection;
    protected ILoadingFacade mModelContract;

    protected IModel mAttachedModel;
    protected IView mAttachedView;

    protected boolean mIsModelReady;
    protected boolean mIsViewReady;
}
