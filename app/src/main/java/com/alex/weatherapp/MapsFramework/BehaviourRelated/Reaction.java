package com.alex.weatherapp.MapsFramework.BehaviourRelated;

/**
 * Created by Alex on 06.11.2015.
 */

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.IProjector;
import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;

/** Reaction for a certain action. Reactions is assigned to entities and resembles certain king of
 * behaviour. Protected inheritance is used for filtering unsupported actions.
 */
public abstract class Reaction implements IReaction {
    public Reaction(ActionType typeOfAction){
        mSupportedAction = typeOfAction;
        mTargetEntity = null;
    }
    @Override
    public boolean reactTo(Action action){
        if (mSupportedAction != null &&
                !action.getActionType().equals(mSupportedAction)){
            return false;
        }
        return react(action);
    }

    @Override
    public void setTargetEntity(IEntity target) {
        mTargetEntity = target;
    }

    @Override
    public IEntity getTargetEntity() {
        return mTargetEntity;
    }

    abstract protected boolean react(Action action);
    public ActionType getSupportedAction(){ return mSupportedAction;}

    private IEntity mTargetEntity;
    private ActionType mSupportedAction;
}
