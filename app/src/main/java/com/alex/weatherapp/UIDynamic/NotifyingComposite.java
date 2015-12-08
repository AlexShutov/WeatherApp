package com.alex.weatherapp.UIDynamic;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.PlaceForecast;
import com.alex.weatherapp.UIDetailed.PlaceForecastViewer.IForecastViewer;
import com.alex.weatherapp.UIDynamic.CityPicker.ICityPickedFeedback;
import com.alex.weatherapp.UIDynamic.CityPicker.ICityPicker;
import com.alex.weatherapp.Utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 26.11.2015.
 */

/**
 * Acts as compositor and decorator. Allows to decorate some 'master' controller by assigning to
 * it some other controller; inn result all composed controllers reacts to user feedback
 * simultaneously. Master controller's feedback is also fired when one of dependent controllers
 * triggers feedback (the rest of depended controllers reacts too). It acts as 'Subject' relay,
 * triggering feedback in all controllers.
 */
public class NotifyingComposite implements IViewingController {
    class BoundUnit implements ICityPickedFeedback {
        BoundUnit(){
            mController = null;
            mControllerFeedback = null;
        }

        /** At this stage controller's picker have already been decorated, so it intercepts feedback
         * and executes its own stuff. Original picker's feedback instance are being kept in
         * controller's private variable, so mControllerFeedback points to controller's
         * implementation. On the other hand, it is true only for ViewingController. Other
         * implementations of IViewingController might act differently. Consider using map. MapViewer
         * can be created as implementation of ICityPicker and IForecastViewer, but can other be
         * IViewingController. The former duplicates references to forecasts and places but allows
         * to show forecasts on a map (in the marker, perhaps), but the latter is more simple, but
         * only allows to show places.
         * So to speak, this composite also acts as second-level decorator.
         * @param controller
         */
        void attach(IViewingController controller){
            ICityPicker picker = controller.getAssignedPicker();
            ICityPickedFeedback controllerFeedback = null;
            if (null != picker) {
                controllerFeedback = picker.getFeedback();
                picker.setFeedback(this);
            }
            mController = controller;
            mControllerFeedback = controllerFeedback;
        }
        void detachController(){
            mController.getAssignedPicker().setFeedback(mControllerFeedback);
            mController = null;
            mControllerFeedback = null;
        }

        /**
         * Decorates controller's feedback by telling NotifyingComposite to notify
         * other controllers. Original feedback is executed first
         * @param pickedCity
         */
        @Override
        public void onCityPicked(LocationData pickedCity) {
            if (null != mControllerFeedback){
                mControllerFeedback.onCityPicked(pickedCity);
            }
            notifyOnCityPicked(pickedCity, this);
        }

        private IViewingController mController;
        private ICityPickedFeedback mControllerFeedback;
    }

    public NotifyingComposite(){
        mMasterController = null;
        mUnitTriggeringFeedback = null;
        mBoundUnits = new ArrayList<>();
    }

    public void addController(IViewingController controller){
        BoundUnit newUnit = new BoundUnit();
        newUnit.attach(controller);
        mBoundUnits.add(newUnit);
    }
    public void removeController(IViewingController controller){
        BoundUnit found = null;
        for (BoundUnit unit : mBoundUnits){
            if (unit.mController == controller){
                found = unit;
                break;
            }
        }
        if (null != found){
            mBoundUnits.remove(found);
            found.detachController();
        }
    }

    /** Inherited from IViewingController, hands call to master controller */
    @Override
    public void assignPlacePicker(ICityPicker placePicker) {
        checkMasterController();
        getMasterController().assignPlacePicker(placePicker);
    }
    @Override
    public void assignForecastViewer(IForecastViewer forecastViewer) {
        checkMasterController();
        getMasterController().assignForecastViewer(forecastViewer);
    }
    @Override
    public ICityPicker getAssignedPicker() throws IllegalStateException {
        checkMasterController();
        return getMasterController().getAssignedPicker();
    }
    @Override
    public IForecastViewer getAssignedForecastViewer() throws IllegalStateException {
        checkMasterController();
        return getMasterController().getAssignedForecastViewer();
    }
    @Override
    public void handleListOfPlaces(List<LocationData> placesToShow) {
        for (BoundUnit bu : mBoundUnits){
            bu.mController.handleListOfPlaces(placesToShow);
        }
    }
    @Override
    public void handleIncomingForecast(PlaceForecast forecast) {
        for (BoundUnit bu : mBoundUnits){
            bu.mController.handleIncomingForecast(forecast);
        }
    }
    @Override
    public void saveState() {
        for (BoundUnit u : mBoundUnits){
            u.mController.saveState();
        }
    }
    @Override
    public void restoreState() {
        for (BoundUnit u : mBoundUnits){
            u.mController.restoreState();
        }
    }

    @Override
    public void clear() {
        for (BoundUnit u : mBoundUnits){
            u.mController.clear();
        }
    }

    @Override
    public void addPlace(LocationData place) {
        for (BoundUnit u : mBoundUnits){
            u.mController.addPlace(place);
        }
    }

    /** Here it sets feedback only for master controller
     * @param cityPickedFeedback
     */
    @Override
    public void setOnCityPickedFeedback(ICityPickedFeedback cityPickedFeedback) {
        checkMasterController();
        getMasterController().setOnCityPickedFeedback(cityPickedFeedback);
    }

    @Override
    public PlaceForecast getForecast(LocationData place) throws IllegalStateException,
            IllegalArgumentException {
        checkMasterController();
        return getMasterController().getForecast(place);
    }

    @Override
    public List<LocationData> getKnownPlaces() {
        checkMasterController();
        return getMasterController().getKnownPlaces();
    }

    /** Its own methods */
    /**
     * We need to break recursion, which happens when other BoundUnits are being receiving
     * notifications and after executing theit own callback tries to notify notifier again.
     * We skip unit which have triggered this action, because it already executed own callback
     * before calling this method.
     * @param pickedCity
     * @param triggeringUnit
     */
    private void notifyOnCityPicked(LocationData pickedCity, BoundUnit triggeringUnit){
        if (null != mUnitTriggeringFeedback){
            return;
        }
        mUnitTriggeringFeedback = triggeringUnit;
        for (BoundUnit unit : mBoundUnits){
            if (unit == triggeringUnit) continue;
            unit.onCityPicked(pickedCity);
        }
        mUnitTriggeringFeedback = null;
    }
    public void setMasterController(IViewingController controller){
        if (null == controller) return;
        BoundUnit masterUnit = null;
        for (BoundUnit unit : mBoundUnits){
            if (unit.mController == controller){
                masterUnit = unit;
                break;
            }
        }
        if (null == masterUnit){
            addController(controller);
        }
        mMasterController = controller;
    }
    private void checkMasterController(){
        if (null == mMasterController){
            if (mBoundUnits.isEmpty()){
                String msg = "Notifying composite has no aggregated controllers and";
                Logger.e(msg);
                throw new IllegalStateException(msg);
            }else {
                setMasterController(mBoundUnits.get(0).mController);
            }
        }
    }

    public IViewingController getMasterController(){ return mMasterController;}

    private BoundUnit mUnitTriggeringFeedback;
    private List<BoundUnit> mBoundUnits;
    private IViewingController mMasterController;
}
