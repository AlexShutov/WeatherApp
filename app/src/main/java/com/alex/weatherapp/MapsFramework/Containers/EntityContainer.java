package com.alex.weatherapp.MapsFramework.Containers;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionPlug;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionType;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.IReactionComposite;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.IProjector;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ReactionComposite;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.SocketRack;
import com.alex.weatherapp.MapsFramework.EntityGeneral.Entity;
import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Alex on 06.11.2015.
 */
public class EntityContainer extends Entity implements IEntityContainer{
    /** Transits actions to this container (As Action Plug) */
    private class ActionTransit implements ActionPlug.IActionHandler{
        ActionTransit(){
            mContainer = EntityContainer.this;
        }
        @Override
        public void handleAction(Action action) {
            mContainer.reactTo(action);
        }
        private IEntityContainer mContainer;
    }

    public EntityContainer(){
        super();
        init();
    }

    protected void init(){
        mFamilyName = "";
        mReactions = new ReactionComposite();
        mEntities = new ArrayList<>();
        mActionPlugForThis = new ActionPlug(new ActionTransit());
        mSocketRack = null;
        mLivesIn = null;
        mItemLevelSupportedActionTypes = new HashSet<>();
    }

    /**
     * Entity container act as a plug for an action socket
     * @param action
     */
    @Override
    public void handleAction(Action action) {
        this.reactTo(action);
    }
    @Override
    public void plugInto(SocketRack rack, ActionType actionType){
        mActionPlugForThis.plugIntoRack(rack, actionType);
    }
    @Override
    public void unplugFrom(SocketRack rack){
        mActionPlugForThis.unplugAll(rack);
    }


    public IReactionComposite getReactions(){
        return mReactions;
    }
    @Override
    public void setReactions(IReactionComposite reactionComposite){
        if (null == reactionComposite){
            return;
        }
        mReactions = reactionComposite;
    }

    @Override
    public String getFamilyName() {
        return mFamilyName;
    }
    @Override
    public void setFamilyName(String familyName) {
        mFamilyName = familyName;
    }
    /** Inherited from IReaction */

    /**
     *For each entity contained execute all actions, applied to
     * this container (container-level actions), O(N^2), but usually there are few actions and
     * they occurs seldom
     * @param action
     */
    @Override
    public boolean reactTo(Action action) {
        /** React on container level */
        mReactions.setTargetEntity(this);
        mReactions.reactTo(action);
        /** react on entity-level */
        for (IEntity e : getEntities()){
            if (isSupportItemLevelAction(action.getActionType())) {
                mReactions.setTargetEntity(e);
                if (mReactions.reactTo(action)){
                    break;
                };
            }
        }
        return true;
    }

    @Override
    public boolean isSupportsAction(Action action) {
        return true;
    }

    @Override
    public void setSocketRack(SocketRack rack) {
        mSocketRack = rack;
    }
    @Override
    public SocketRack getSocketRack() {
        return mSocketRack;
    }
    @Override
    public ActionPlug getActionPlug() {
        return mActionPlugForThis;
    }

    @Override
    public void setCommunity(Community community) {
        mLivesIn = community;
    }

    @Override
    public Community getCommunity() {
        return mLivesIn;
    }

    /**
     * Use only in single-target mode
     * @return
     */
    @Override
    public IEntity getTargetEntity() {
        return mReactions.getTargetEntity();
    }
    @Override
    public void setTargetEntity(IEntity target) {
        mReactions.setTargetEntity(target);
    }

    public void addEntity(IEntity e){
        mEntities.add(e);
    }
    public List<IEntity> getEntities(){
        return mEntities;
    }
    public void clearEntities(){
        mEntities.clear();
    }

    @Override
    public void addItemLevelActionType(ActionType actionType) {
        mItemLevelSupportedActionTypes.add(actionType);
    }
    @Override
    public void removeItemLevelActionType(ActionType actionType) {
        mItemLevelSupportedActionTypes.remove(actionType);
    }
    @Override
    public boolean isSupportItemLevelAction(ActionType actionType) {
        return mItemLevelSupportedActionTypes.contains(actionType);
    }

    Set<ActionType> mItemLevelSupportedActionTypes;

    private List<IEntity> mEntities;
    private IReactionComposite mReactions;
    private ActionPlug mActionPlugForThis;
    private SocketRack mSocketRack;
    private String mFamilyName;
    Community mLivesIn;
}
