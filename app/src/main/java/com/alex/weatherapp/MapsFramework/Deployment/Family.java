package com.alex.weatherapp.MapsFramework.Deployment;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionType;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.IReaction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.IProjector;
import com.alex.weatherapp.MapsFramework.Containers.EntityContainer;
import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;
import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by Alex on 10.11.2015.
 */

/**
 * Family carries all information related to a particular kind of map entities.
 * MapFacade already have all information needed, but keeping it here simplifies altering
 * family behaviour and initial creation. All items in here points to the same instances, as
 * MapFacade do. It takes no memory compared to map itself.
 * This class is build by FamilyBuilder the handed to deployer, which uses it for registering
 * family of objects, and stores it just in case.
 */
public class Family {
    public Family(){
        init();
    }
    private void init(){
        mDataContainer = new EntityContainer();
        mProjectionContainer = new EntityContainer();
        mProjector = null;
        mDataActions = new TreeSet<>();
        mProjectionActions = new TreeSet<>();
        mDataReactions = new ArrayList<>();
        mProjectionReactions = new ArrayList<>();
        mFamilyName = "";
    }

    /**
     * Accessors
     */
    public Set<ActionType> getDataActions(){ return mDataActions;}
    public void setDataActions(Set<ActionType> actions){ mDataActions = actions;}
    public Set<ActionType> getProjectionActions(){ return mProjectionActions;}
    public void setProjectionActions(Set<ActionType> actions){ mProjectionActions = actions;}
    public IEntityContainer getDataContainer(){ return mDataContainer;}
    public void setDataContainer(IEntityContainer container){ mDataContainer = container;}
    public IEntityContainer getProjectionContainer(){ return mProjectionContainer;}
    public void setProjectionContainer(IEntityContainer container){
        mProjectionContainer = container;}
    public IProjector getProjector(){ return mProjector;}
    public void setProjector(IProjector projector){ mProjector = projector;}
    public List<IReaction> getDataReactions(){ return mDataReactions;}
    public void setDataReactions(List<IReaction> reactions){
        mDataReactions = reactions;
    }
    public List<IReaction> getProjectionReactions(){ return mProjectionReactions;}
    public void setProjectionReactions(List<IReaction> reactions){
        mProjectionReactions = reactions;
    }
    public void setFamilyName(String familyName){
        mFamilyName = familyName;
        mDataContainer.setFamilyName(familyName);
        mProjectionContainer.setFamilyName(familyName);
    }
    public String getFamilyName(){ return mFamilyName;}

    private IEntityContainer mDataContainer;
    private IEntityContainer mProjectionContainer;
    private IProjector mProjector;
    Set<ActionType> mDataActions;
    Set<ActionType> mProjectionActions;
    List<IReaction> mDataReactions;
    List<IReaction> mProjectionReactions;
    private String mFamilyName;
}
