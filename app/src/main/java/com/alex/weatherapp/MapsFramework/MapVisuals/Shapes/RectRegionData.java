package com.alex.weatherapp.MapsFramework.MapVisuals.Shapes;


import com.alex.weatherapp.MapsFramework.EntityGeneral.MapEntity;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Alex on 12.11.2015.
 */
public class RectRegionData extends ShapeData {
    public RectRegionData(){
        super();
        init();
    }
    private void init(){
        setTopLeft(new LatLng(0, 0));
        setRightBottom(new LatLng(0, 0));
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

    public LatLng getTopLeft(){ return mTopLeft;}
    public void setTopLeft(LatLng topLeft){ mTopLeft = topLeft;}
    public LatLng getRightBottom(){ return mRightBottom;}
    public void setRightBottom(LatLng rightBottom){ mRightBottom = rightBottom;}

    private LatLng mTopLeft;
    private LatLng mRightBottom;
}
