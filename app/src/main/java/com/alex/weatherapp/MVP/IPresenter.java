package com.alex.weatherapp.MVP;

/**
 * Created by Alex on 30.09.2015.
 */

/**
 * The methods in this interface are responsible for connecting model and a
 * presenter. The presenter-specific functionality are described in
 * IPresenterContract interface
 */
public interface IPresenter extends IPresenterContract {
    /**
     * During connection presenter to IModel it will initiate binding to the
     * underlying service (any other kind of async initialization). When it is done,
     * presenter will receive completion callback call, which gonna be redirected to that iface,
     * if set
     */
    interface IModelReady {
        void onModelReady();
    }

    /** In this case the same as IModelReady, because service binding implements
     * model's Observer. Set it in in view to know when presenter is ready.
     * IPresenter keeps only one reference of ready callback. You need set it manually
     * for entry point execution in IView. Suppose, we have an activity, which is created before
     * model is initialized. So we can't send request to model from it, or we can but they will
     * be ignored. Setting this callback in 'setPresenterReadyCallback(IPresenterReady cb)'
     * forces presenter to inform Activity when it is ready. */
    interface IPresenterReady {
        void onPresenterReady(IPresenter presenter);
    }
     void setPresenterReadyCallback(IPresenterReady cb);

    void tapIntoModel(IModel model);
    /**
     * IModel instance may be not null, but still  isn't ready to be used
     * (For example- we create IModel and IPresenter instances in Application
     * (MVPManager) and tie them, then presenter schedules model initializations.
     * Model reference isn't null, but can't be used. It is up presenter to decide,
     * what to do in that case- whether ignore calls, or schedule them for later calls.
     */
    boolean isModelReady();
    boolean isPresenterReady();
    boolean disconnectFromModel();
    void onModelConnected();

    void onModelDisconnected();
    /**
     * The view is also has to bo initialized in order to be used (checked implicitly during
     * calls to the view in view itself, here is no isViewReady() method
     * @param view
     */
    void setView(IView view);

    /**
     * Dispatch request to the view if it is set
     * @return
     */
    boolean isViewReady();

    /**
     * We need to declare the view explicitly to be sure. Consider the case, when one activity
     * gets obstructed by another. The first activity isn't destroyed for some time, while
     * the second activity is already working. So, because presenter is bound to the only view,
     * presenter is already using the second activity and we have no need in disconnecting from
     * the first one, and that method will do nothing because of reference mismatch.
     * @param view
     */
    void disconnectView(IView view);

}
