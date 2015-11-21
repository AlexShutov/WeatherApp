package com.alex.weatherapp.MapsFramework.BehaviourRelated;

/**
 * Created by Alex on 06.11.2015.
 */

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.IProjector;
import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;

import java.util.ArrayList;
import java.util.List;

/** Compoosite pattern, null value as action type forces to accept all actions, they then are
 * given to every added reaction */
public class ReactionComposite implements IReactionComposite {
    public ReactionComposite(){
        mReactions = new ArrayList<>();
    }

    /** Inherited from IReaction composite*/
    @Override
    public void add(IReaction reaction){
        mReactions.add(reaction);
    }
    @Override
    public void remove(IReaction reaction){
        mReactions.remove(reaction);
    }
    @Override
    public void clear() {
        mReactions.clear();
    }
    @Override
    public IReactionComposite getCompositeReaction() {
        return this;
    }

    @Override
    public boolean isSupportsAction(Action action) {
        return true;
    }

    @Override
    public boolean reactTo(Action action) {
        boolean success = false;
        for (IReaction r : mReactions) {
            if (r.isSupportsAction(action) && r.reactTo(action)){
                success = true;
            }
        }
        return success;
    }
    @Override
    public void setTargetEntity(IEntity target) {
        for (IReaction r : mReactions) {
            r.setTargetEntity(target);
        }
    }
    @Override
    public IEntity getTargetEntity() {
        if (mReactions.isEmpty()){
            return null;
        }
        return mReactions.get(0).getTargetEntity();
    }

    private List<IReaction> mReactions;
}
