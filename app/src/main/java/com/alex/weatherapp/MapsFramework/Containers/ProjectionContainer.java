package com.alex.weatherapp.MapsFramework.Containers;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.IReactionComposite;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.IProjector;
import com.alex.weatherapp.MapsFramework.EntityGeneral.Entity;
import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;


/**
 * Created by Alex on 07.11.2015.
 */
public class ProjectionContainer extends EntityContainer {
    public ProjectionContainer(IProjector projector){
        super();
    }
    public void setProjector(IProjector projector){
        mProjector = projector;
        mProjector.setProjectionContainer(this);
    }
    public IProjector getProjector() {
        return mProjector;
    }

    /**
     * Result container is supposed to be bound with data container. But, result container has a
     * projector, which also need to know about that change when happened, so if other container
     * is EntityContainer, we force projector to bind with container
     * @param other
     */
    @Override
    public void entangleWith(IEntity other) {
        if (null != other && other instanceof EntityContainer){
            if (null != mProjector){
                mProjector.setDataContainer((IEntityContainer) other);
            }
        }
        super.entangleWith(other);
    }
    private IProjector mProjector;
}
