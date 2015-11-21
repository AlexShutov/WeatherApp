package com.alex.weatherapp.MapsFramework.MapVisuals.Shapes;


import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MapTapAction;
import com.google.android.gms.maps.model.Circle;

/**
 * Created by Alex on 12.11.2015.
 */
public class CircularRegionProjection extends ShapeProjection {
    public CircularRegionProjection(){
        super();
    }

    public Circle getCircle(){ return mCircle;}
    public void setCircle(Circle circle){ mCircle = circle; }

    @Override
    public boolean isTapped(ITapVisitor visitor, MapTapAction action) {
        return visitor.isTapped(this, action);
    }


    /** Circle region in a map */
    private Circle mCircle;
}
