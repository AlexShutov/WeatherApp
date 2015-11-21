package com.alex.weatherapp.MapsFramework;

/**
 * Created by Alex on 06.11.2015.
 */

import com.alex.weatherapp.MapsFramework.Interfacing.ShapesTestBehaviour;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionTypes;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.StandartSocketRackBuilder;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.SocketRack;
import com.alex.weatherapp.MapsFramework.Containers.Community;
import com.alex.weatherapp.MapsFramework.Containers.ProjectionsWarehouse;
import com.alex.weatherapp.MapsFramework.Deployment.Deployer;
import com.google.android.gms.maps.GoogleMap;

/** Wrapper around GoogleMap, that has everything is necessary for this to work */
public class MapFacade {
    public MapFacade(GoogleMap map){
        init();
        setMap(map);
        initSocketRack();
    }
    private void init(){
        mGMap = null;
        mIsMapReady = false;
        mDataStore = new Community();
        mProjectionWarehouse = new ProjectionsWarehouse();
        mProjectionWarehouse.entangleWith(mDataStore);
        mIsJustStarted = true;
    }
    /** initializes message pump for a standard set of actioins */
    private void initSocketRack(){
        StandartSocketRackBuilder rackBuilder = new StandartSocketRackBuilder();
        rackBuilder.createNewRack(mGMap);
        rackBuilder.enableMarkerClicks();
        rackBuilder.enableMarkerDragging();
        rackBuilder.enableMapTapping();
        mSocketRack = rackBuilder.build();
    }

    public SocketRack getSocketRack(){
        return mSocketRack;
    }

    /**
     * Deploys this map facade by using Deployer visitor. First, create FamilyBuilder
     * instances for each kind of items, set them in deployer, and after all call this method.
     * @param deployer
     */
    public void deploy(Deployer deployer){
        if (null == deployer){
            throw new IllegalStateException("Null reference");
        }
        deployer.setFacade(this);
        deployer.deploy();
    }

    /**
     * Stop message dispatching and break clear reference to the map
     */
    public void suspend(){
        mSocketRack.reactTo(ActionTypes.getAction(ActionTypes.ACTION_CLEAR_PROJECTION));
        mSocketRack.deactivate();
        if (null != mGMap){
            mGMap.clear();
        }
        mIsJustStarted = false;
        //setMap(null);
    }

    public void destroy(){
        mSocketRack.destroy();
    }

    /** For making sure that working thread is stopped, call
     * deactivate once more
     */
    public void resume(){
        if (null == mGMap){
            return;
        }
        mSocketRack.deactivate();
        mSocketRack.activate();
        if(!mIsJustStarted) {
            mSocketRack.broadcastReproject();
        }
    }

    public void setMap(GoogleMap map){
        mGMap = map;
        mIsMapReady = (map == null);
    }
    public GoogleMap getAssignedMap(){ return mGMap;}


    /** Accessors for parts of this facade. It partially breaks its meaning, by allowing changing
     * them. Another solution- make Deployer inner class for allowing it accessing those
     * parts, but it wil make this class too cumbersome.
     * @return
     */
    public Community getDataStore(){ return mDataStore;}
    public void setDataStore(Community dataStore){ mDataStore = dataStore;}
    public ProjectionsWarehouse getProjectionWarehouse(){ return mProjectionWarehouse;}
    public void setProjectionWarehouse(Community warehouse){
        mProjectionWarehouse = (ProjectionsWarehouse) warehouse;
    }

    /** During passing activity lifecycle (onStop-onStart) GoogleMap gets deleted,
     * while all projections beiing kept active, so in that case we need to broadcast
     * again project actions, for all data being displayed again. But, howewer, we don't
     * want to do that the first time, because all items would be drawn twice.
     * mIsJustStarted solves that issue.
     */
    private boolean mIsJustStarted;
    private boolean mIsMapReady;
    private GoogleMap mGMap;
    private SocketRack mSocketRack;

    Community mDataStore;
    ProjectionsWarehouse mProjectionWarehouse;
}
