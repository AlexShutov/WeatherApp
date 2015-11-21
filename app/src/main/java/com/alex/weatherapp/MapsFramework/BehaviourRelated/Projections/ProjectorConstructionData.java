package com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections;

/**
 * Created by Alex on 08.11.2015.
 */

import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionType;
import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;

import java.util.Set;

/** Struct, passed into ProjectionWarehouse on creation stage, carrying information
 * about projector, projection events this projector supports and also family name, this
 * projector is suited for.
 */
public class ProjectorConstructionData {
    public ProjectorConstructionData(){
        mOwningFamilyName  = "";
        mSupportedProjectionEvents = null;
        mProjector = null;
    }

    /** Accessors */
    public void setFamilyName(String familyName){
        mOwningFamilyName = familyName;
    }
    public String getFamilyName(){
        return mOwningFamilyName;
    }
    public void setSupportedEvents(Set<ActionType> actionTags){
        mSupportedProjectionEvents = actionTags;
    }
    public Set<ActionType> getSupportedEvents(){
        return mSupportedProjectionEvents;
    }
    public void setProjector(IProjector projector){
        mProjector = projector;
    }
    public IProjector getProjector(){
        return mProjector;
    }
    public void setDataContainer(IEntityContainer container){
        mDataContainer = container;
    }
    public IEntityContainer getDataContainer(){ return mDataContainer;}
    public void setProjectionContainer(IEntityContainer container){
        mProjectionContainer = container;
    }
    public IEntityContainer getProjectionContainer(){
        return mProjectionContainer;
    }


    private String mOwningFamilyName;
    private Set<ActionType> mSupportedProjectionEvents;
    private IProjector mProjector;
    private IEntityContainer mDataContainer;
    private IEntityContainer mProjectionContainer;
}
