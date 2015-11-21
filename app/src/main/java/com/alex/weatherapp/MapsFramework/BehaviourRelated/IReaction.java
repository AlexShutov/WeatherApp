package com.alex.weatherapp.MapsFramework.BehaviourRelated;

import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;

/**
 * Created by Alex on 06.11.2015.
 */
public interface IReaction {
    boolean reactTo(Action action);
    void setTargetEntity(IEntity target);
    IEntity getTargetEntity();
    boolean isSupportsAction(Action action);
}
