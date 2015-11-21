package com.alex.weatherapp.MapsFramework.MapVisuals.Shapes;

import com.alex.weatherapp.MapsFramework.EntityGeneral.MapEntity;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Alex on 12.11.2015.
 */
public class CircularRegionData extends ShapeData  {
    public CircularRegionData(){
        super();
    }
    public CircularRegionData(LatLng center, double radius){
        super();
        setCenter(center);
        setRadius(radius);
    }

    @Override
    public MapEntity project(IProjectingVisitor visitor) {
        return visitor.project(this);
    }
    @Override
    public MapEntity createEmptyProjection(IProjectingVisitor visitor) {
        return visitor.createEmpty(this);
    }
    @Override
    public void removeFromMap(IProjectingVisitor visitor) {
        visitor.removeFromMap(this);
    }

    public LatLng getCenter(){ return mCircleCenterCoords;}
    public void setCenter(LatLng centerCoords){ mCircleCenterCoords = centerCoords;}
    public double getRadius(){ return mRadius;}
    public void setRadius(double r){ mRadius = r;}

    private LatLng mCircleCenterCoords;
    private double mRadius;

}
