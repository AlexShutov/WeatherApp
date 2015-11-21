package com.alex.weatherapp.MapsFramework.EntityGeneral;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;

/**
 * Created by Alex on 07.11.2015.
 */

/**
 * Entity has only one action reference. If you want more, use composite pattern, containers do
 * it in that way. Every entity is entangled with other counterpart- data with projection, data
 * container with other container, so connections is easy to follow, but it isn't necessary.
 * Entity know about container it belongs, it assures link between actions on entity-level and
 * container: entity-action -> container->container-actions
 * Also entity has an String ID mEntityGroupID. By default it is "" constant from VM's string pool,
 * so it will not draini memory. set it if you're using complex entities or data entity has more
 * than one projection
 */
public interface IEntity {
    void entangleWith(IEntity other);
    IEntity getEntangled();
    void setAction(Action action);
    void setContainer(IEntityContainer container);
    IEntityContainer getContainer();
    Action getAction();
    void setEntityGroupID(String groupID);
    String getEntityGroupID();
}
