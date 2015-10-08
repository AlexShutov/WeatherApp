package com.alex.weatherapp.MVP;

/**
 * Created by Alex on 30.09.2015.
 */
public interface IView extends IViewContract {

    /**
     * Will be called by presenter when view attached to it
     * @param presenter PresenterBase instance
     */
    void connectToPresenter(IPresenter presenter);

    /**
     * View keeps reference to IPresenter, if it is no connected to it, reference equals null
     * @return
     */
    boolean isPresenterConnected();
    boolean isUIReady();
    void finish();

}
