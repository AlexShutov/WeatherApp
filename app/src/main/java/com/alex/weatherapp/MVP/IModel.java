package com.alex.weatherapp.MVP;

/**
 * Created by Alex on 30.09.2015.
 */


import com.alex.weatherapp.LoadingSystem.ServiceWrapper.ILoadingConnection;

/**
 * a particular presenter retrives ILoadingConnection instance and
 * binds to the service from it. So, ILoadingConnection doesn't require any
 * ties between IModel and IPresenter. If presenter would need those one day,
 * it is possible to implement connections it as an observer pattern to
 * support multiple presenters
 */

public interface IModel {


    void initModel();
    void terminateModel();
    ILoadingConnection getServiceConnection();

}
