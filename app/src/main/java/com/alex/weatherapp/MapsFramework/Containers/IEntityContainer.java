package com.alex.weatherapp.MapsFramework.Containers;

/**
 * Created by Alex on 07.11.2015.
 */

import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionPlug;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionType;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.IReaction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.IReactionComposite;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.SocketRack;
import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;

import java.util.List;

/**
 * I've moved EntityContainer functionnality into separate interface, because some data may need
 * more than one projections, for example, temperature area paints region and show marker in the
 * middle so projector need to create both. one way to solve this is to employ composite pattern,
 * or just store all projection in one Map and access them by key
 */
public interface IEntityContainer extends IEntity,
        ActionPlug.IActionHandler, IReaction {

    void setReactions(IReactionComposite reactionComposite);
    IReactionComposite getReactions();

    void addEntity(IEntity e);
    List<IEntity> getEntities();
    void clearEntities();
    void setSocketRack(SocketRack rack);
    SocketRack getSocketRack();
    ActionPlug getActionPlug();
    void plugInto(SocketRack rack, ActionType actionType);
    void unplugFrom(SocketRack rack);

    void addItemLevelActionType(ActionType actionType);
    void removeItemLevelActionType(ActionType actionType);
    boolean isSupportItemLevelAction(ActionType actionType);
    public String getFamilyName();
    public void setFamilyName(String familyName);
    void setCommunity(Community community);
    Community getCommunity();
}
