package com.alex.weatherapp.MapsFramework.MapVisuals.Shapes;

import android.location.Location;


import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MapTapAction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.ProjectorBase;
import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;
import com.alex.weatherapp.MapsFramework.Containers.ProjectionsWarehouse;
import com.alex.weatherapp.MapsFramework.EntityGeneral.DataPiece;
import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;
import com.alex.weatherapp.MapsFramework.EntityGeneral.MapEntity;
import com.alex.weatherapp.Utils.Logger;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

/**
 * Created by Alex on 12.11.2015.
 */
public class ShapeProjector extends ProjectorBase
        implements IProjectingVisitor, ITapVisitor {
    public ShapeProjector(){
        super(null);
    }

    @Override
    public void removeProjectionsFromMap() {
        Logger.d("Removing shape projections from map");
        for (IEntity e : getDataContainer().getEntities()){
            this.clearIndividualProjection((DataPiece) e);
        }
    }

    @Override
    public void project(DataPiece data) {
        ShapeData shapeData = (ShapeData) data;
        shapeData.project(this);
    }

    @Override
    public void project(IEntityContainer dataSource, IEntityContainer projections) {
        Logger.d("Projecting shapes");
        for (IEntity e : dataSource.getEntities()){
            project((DataPiece)e);
            /** data and projection are already entangled and stored in containers
             * ( were done in createEmptyProjection method)
             */
        }
    }

    @Override
    public MapEntity createEmptyProjectionFor(DataPiece dataPiece) {
        ShapeData sd = (ShapeData)dataPiece;
        return sd.createEmptyProjection(this);
    }
    @Override
    public void clearIndividualProjection(DataPiece projectionFor) {
        ShapeData sd = (ShapeData) projectionFor;
        sd.removeFromMap(this);
    }

    /** Inherited from IProjectable shape - visitor pattern */
    @Override
    public MapEntity createEmpty(CircularRegionData cd) {
        return new CircularRegionProjection();
    }
    @Override
    public MapEntity createEmpty(RectRegionData rd) {
        return new RectRegionProjection();
    }
    @Override
    public void removeFromMap(CircularRegionData cd) {
        CircularRegionProjection cp = (CircularRegionProjection) cd.getEntangled();
        if (null == cp) return;
        Circle c = cp.getCircle();
        if (null != c){
            c.remove();
        }
    }
    @Override
    public void removeFromMap(RectRegionData rd) {
        RectRegionProjection rp = (RectRegionProjection) rd.getEntangled();
        if (null == rp) return;
        Polygon p = rp.getPolygon();
        if (null != p) {
            p.remove();
        }
    }

    @Override
    public MapEntity project(CircularRegionData data) {
        ProjectionsWarehouse wh = (ProjectionsWarehouse) getProjectionContainer().getCommunity();
        GoogleMap map = wh.getMap();

        CircleOptions options = new CircleOptions().center(data.getCenter())
                .radius(data.getRadius());
        if (!data.isSelected()) {
            options.strokeWidth(data.getStrokeNormal())
                    .fillColor(data.getFillColorNormal());
        } else {
            options.strokeWidth(data.getStrokeSelected())
                    .fillColor(data.getFillColotSelected());
        }
                options.strokeColor(data.getStrokeColor());
        Circle circle = map.addCircle(options);
        CircularRegionProjection proj = (CircularRegionProjection) data.getEntangled();
        proj.setCircle(circle);
        // Return for chaining calls
        return proj;
    }
    @Override
    public MapEntity project(RectRegionData data) {
        LatLng tl = data.getTopLeft();
        LatLng rb = data.getRightBottom();

        ProjectionsWarehouse wh = (ProjectionsWarehouse) getProjectionContainer().getCommunity();
        GoogleMap map = wh.getMap();

        PolygonOptions options = new PolygonOptions().add(
                new LatLng(tl.latitude, tl.longitude),
                new LatLng(rb.latitude, tl.longitude),
                new LatLng(rb.latitude, rb.longitude),
                new LatLng(tl.latitude, rb.longitude));
        if (!data.isSelected()) {
            options.strokeWidth(data.getStrokeNormal())
                    .fillColor(data.getFillColorNormal());
        } else {
            options.strokeWidth(data.getStrokeSelected())
                    .fillColor(data.getFillColotSelected());
        }
        options.strokeColor(data.getStrokeColor());
        Polygon poly = map.addPolygon(options);

        RectRegionProjection proj = (RectRegionProjection)data.getEntangled();
        proj.setRectBounds(new LatLngBounds(rb, tl));
        proj.setPolygon(poly);
        return proj;
    }

    /**
     * We perform checking on a worker thread, but can only access projection on main thread,
     * so we use values, stored in data, not projection
     * @param shape
     * @param tapAction
     * @return
     */
    @Override
    public boolean isTapped(CircularRegionProjection shape, MapTapAction tapAction) {
        CircularRegionData dc = (CircularRegionData) shape.getEntangled();
        LatLng center = dc.getCenter();
        double radius = dc.getRadius();
        LatLng tapPoint = tapAction.getTapPosition();
        float[] result = new float[1];
        Location.distanceBetween(center.latitude, center.longitude,
                tapPoint.latitude, tapPoint.longitude, result);
        return (result[0] < radius);
    }

    @Override
    public boolean isTapped(RectRegionProjection shape, MapTapAction tapAction) {
        boolean isIt =
            shape.getRectBounds().contains(tapAction.getTapPosition());
        return isIt;
    }
}
