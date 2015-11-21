package com.alex.weatherapp.MapsFramework.Deployment;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionType;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionTypes;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.ProjectorConstructionData;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.SocketRack;
import com.alex.weatherapp.MapsFramework.Containers.Community;
import com.alex.weatherapp.MapsFramework.Containers.ProjectionsWarehouse;
import com.alex.weatherapp.MapsFramework.MapFacade;
import com.alex.weatherapp.Utils.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Alex on 10.11.2015.
 */

/**
 * Visitor, which sets up MapFacade by using builders FamilyBuilder for each family of items
 */
public class Deployer {
    public Deployer(){
        mBuilders = new ArrayList<>();
    }

    public void setFacade(MapFacade facade){
        mDeployingInstance = facade;
    }
    public MapFacade getFacade(){ return mDeployingInstance;}

    public void addFamilyBuilder(FamilyBuilder builder){
        mBuilders.add(builder);
    }

    public void deploy(){
        setupContainers();
        addFamilies();
        tieContainersAndDataWithProjectionInFamilies();
        activateMessagePassingAndMakeProjections();
    }

    protected void setupContainers(){
        /** setup data store */
        Community dataStore = new Community();
        dataStore.setSocketRAck(mDeployingInstance.getSocketRack());
        mDeployingInstance.setDataStore(dataStore);
        /** setup projectionWarehouse */
        ProjectionsWarehouse projectionsWarehouse = new ProjectionsWarehouse();
        projectionsWarehouse.setMap(mDeployingInstance.getAssignedMap());
        projectionsWarehouse.setSocketRAck(mDeployingInstance.getSocketRack());
        mDeployingInstance.setProjectionWarehouse(projectionsWarehouse);
    }
    protected void tieContainersAndDataWithProjectionInFamilies(){
        ProjectionsWarehouse wh = mDeployingInstance.getProjectionWarehouse();
        Community ds = mDeployingInstance.getDataStore();
        wh.entangleWith(ds);
    }
    protected void activateMessagePassingAndMakeProjections(){
        SocketRack sr = mDeployingInstance.getSocketRack();
        sr.activate();
        /** and broadcast initialization actions */
        sr.reactTo(ActionTypes.getAction(ActionTypes.ACTION_USER_MAKE_INITIAL_PROJECTION));
        sr.reactTo(ActionTypes.getAction(ActionTypes.ACTION_USER_PROJECT));
    }

    protected void addFamilies(){
        if (mBuilders.isEmpty()){
            Logger.i("This system doesn't have any family of objects, so it's useless for now");
            return;
        }
        ProjectionsWarehouse wh = mDeployingInstance.getProjectionWarehouse();
        Community ds = mDeployingInstance.getDataStore();
        for (FamilyBuilder familyBuilder : mBuilders){
            Logger.i("Building family: "+ familyBuilder.getFamilyName());
            Family f = familyBuilder.build(mDeployingInstance.getSocketRack());

            ProjectorConstructionData prBuildData = new ProjectorConstructionData();
            prBuildData.setDataContainer(f.getDataContainer());
            prBuildData.setProjectionContainer(f.getProjectionContainer());
            prBuildData.setFamilyName(f.getFamilyName());
            prBuildData.setProjector(f.getProjector());
            if (!familyBuilder.isProjectorDisabled()) {
                prBuildData.setSupportedEvents(ActionTypes.getProjectionActions());
            }else {
                prBuildData.setSupportedEvents(new HashSet<ActionType>());
            }
            wh.setProjectorData(prBuildData);

            ds.addFamily(f.getDataContainer(), f.getFamilyName(), f.getDataActions());
            wh.addFamily(f.getProjectionContainer(), f.getFamilyName(), f.getProjectionActions());
        }
    }

    private MapFacade mDeployingInstance;
    private List<FamilyBuilder> mBuilders;
}
