package com.alex.weatherapp.MapsFramework.BehaviourRelated;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.IProjector;
import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;
import com.alex.weatherapp.Utils.Logger;

/**
 * Created by Alex on 09.11.2015.
 */
/** Adds logging to reactions */
public class LoggingReactionDecorator implements IReaction {
    public LoggingReactionDecorator(){
        mDecorated = null;
    }

    @Override
    public boolean reactTo(Action action) {
        Logger.i("Logging reaction for " + getDataFamilyDetails() + " Action: "+
                action.getActionType().getAction());
        if (null != mDecorated){
            return mDecorated.reactTo(action);
        }
        return false;
    }

    @Override
    public boolean isSupportsAction(Action action) {
        return true;
    }

    @Override
    public void setTargetEntity(IEntity target) {
        Logger.i("Logging reaction calling setTargetEntity ");
        if (null != mDecorated){
            mDecorated.setTargetEntity(target);
        }
    }
    @Override
    public IEntity getTargetEntity() {
        Logger.i("Logging reaction: calling getTargetEntity");
        if (null == mDecorated){
            return null;
        }
        return mDecorated.getTargetEntity();
    }

    public void setDecorated(IReaction reaction){
        mDecorated = reaction;
    }
    public IReaction getDecorated(){
         return mDecorated;
    }
    public void setFamilyDetails(String details){
        mDataFamilyDetails = details;
    }
    public String getDataFamilyDetails(){
        return mDataFamilyDetails;
    }

    String mDataFamilyDetails;
    IReaction mDecorated;
}
