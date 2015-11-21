package com.alex.weatherapp.MapsFramework.MapVisuals.Shapes;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MapTapAction;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;

/**
 * Created by Alex on 12.11.2015.
 */

/**
 * Projection of rectangular on the map. It keeps Polygon, representing circle itself
 * alongside bounds, allowing easily define whether point is inside polygon or not.
 */

public class RectRegionProjection extends ShapeProjection {
    public RectRegionProjection(){
        /** map-specific objects */
        mPolygon = null;
        mRectBounds = null;
    }

    public Polygon getPolygon(){ return mPolygon;}
    public void setPolygon(Polygon polygon){mPolygon = polygon;}
    public LatLngBounds getRectBounds(){ return  mRectBounds;}
    public void setRectBounds(LatLngBounds bounds){ mRectBounds = bounds;}

    @Override
    public boolean isTapped(ITapVisitor visitor, MapTapAction action) {
        return visitor.isTapped(this, action);
    }

    private Polygon mPolygon;
    private LatLngBounds mRectBounds;
}
