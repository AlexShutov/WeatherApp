package com.alex.weatherapp.MapsFramework.MapVisuals.Shapes;

/**
 * Created by Alex on 12.11.2015.
 */

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MapTapAction;

/**
 * Part of Visitor pattern, allows to find out whether tapped point is inside a given region or not.
 */
public interface ITappable {
    boolean isTapped(ITapVisitor visitor, MapTapAction tapAction);
}
