package com.alex.weatherapp.MVP;

import android.content.Context;

import com.alex.weatherapp.LoadingSystem.ServiceWrapper.ConnectionToLoadingSystem;
import com.alex.weatherapp.LoadingSystem.ServiceWrapper.ILoadingConnection;

/**
 * Created by Alex on 01.10.2015.
 */
public class Model implements IModel {

    public Model(Context context) {
        mAppContext = context;
    }

    /**
     * Initialization is done duribg first binding to the service by a PresenterBase, this method is
     * added for the consistency with a concept and for further scaling
     */
    @Override
    public void initModel() {
    }

    /**
     * The same, as initModel()
     */
    @Override
    public void terminateModel() {
    }

    @Override
    public ILoadingConnection getServiceConnection() {
        return new ConnectionToLoadingSystem(mAppContext);
    }

    private Context mAppContext;
}
