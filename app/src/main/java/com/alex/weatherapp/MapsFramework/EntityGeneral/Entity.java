package com.alex.weatherapp.MapsFramework.EntityGeneral;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;

/**
 * Created by Alex on 06.11.2015.
 */

/**
 * For more details see IEntity interface comment
 */

public class Entity implements IEntity{
    public Entity(){
    }
    @Override
    public void entangleWith(IEntity other){
        mOther = other;
        if (null != other){
            ((Entity) other).mOther = this;
        }
        mEntityGroupID = "";
    }
    @Override
    public IEntity getEntangled(){ return mOther;}
    @Override
    public void setAction(Action action){
        mAction = action;
    }
    @Override
    public void setContainer(IEntityContainer container){
        mContainer = container;
    }
    @Override
    public IEntityContainer getContainer(){ return mContainer;}
    @Override
    public Action getAction(){ return mAction;}
    @Override
    public void setEntityGroupID(String groupID){
        mEntityGroupID = groupID;
    }
    @Override
    public String getEntityGroupID(){ return mEntityGroupID;}

    private String mEntityGroupID;
    private IEntity mOther;
    private Action mAction;
    private IEntityContainer mContainer;
}

