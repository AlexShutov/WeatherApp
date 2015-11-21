package com.alex.weatherapp.MapsFramework.MapVisuals.Shapes;

/**
 * Created by Alex on 12.11.2015.
 */

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionSources.MapTapSource;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MapTapAction;

/**
 * Implement in reaction to tap, use projection info.
 */
public interface ITapVisitor {
    boolean isTapped(CircularRegionProjection shape, MapTapAction tapAction);
    boolean isTapped(RectRegionProjection shape, MapTapAction tapAction );
}
