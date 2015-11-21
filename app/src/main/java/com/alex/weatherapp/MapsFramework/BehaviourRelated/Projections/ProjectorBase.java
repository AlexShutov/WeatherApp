package com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections;

import android.graphics.Color;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.SocketRack;
import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;
import com.alex.weatherapp.MapsFramework.Containers.ProjectionsWarehouse;
import com.alex.weatherapp.MapsFramework.EntityGeneral.DataPiece;
import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;
import com.alex.weatherapp.MapsFramework.EntityGeneral.MapEntity;
import com.alex.weatherapp.Utils.Logger;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Alex on 09.11.2015.
 */

/**
 * Wraps side functionality as keeping references to the parts necessary for projector work
 * (data and projection containers), clearing projections with unbinding them from data.
 * implement createInitialProjection, project method and removeProjectionsFromMap for clearing
 * the map from projected views (use clear map for entirely cleaning map even from projections
 * from other families of objects).
 */
public abstract class ProjectorBase implements IProjector {
    public ProjectorBase(Void callMe){
        setDataContainer(null);
        setProjectionContainer(null);
    }

    @Override
    public void setProjectionContainer(IEntityContainer container) {
        mProjectionContainer = container;
    }

    @Override
    public void setDataContainer(IEntityContainer container) {
        mDataContainer = container;
    }

    @Override
    public IEntityContainer getDataContainer() {
        return mDataContainer;
    }

    @Override
    public IEntityContainer getProjectionContainer() {
        return mProjectionContainer;
    }
    public ProjectionsWarehouse getProjectionWarehouse(){
        return (ProjectionsWarehouse) mProjectionContainer.getCommunity();
    }

    /**
     * Untangles projections and data first, then, depending on mode, removes all items from the map
     */
    @Override
    public void clearProjections(boolean wipeAllMap) {
        removeProjectionsFromMap();
        if (mProjectionContainer.getEntities().isEmpty()){
            Logger.w("Trying to clear empty projection container");
        }
        /** Clone array for avoiding ConcurrentModificationException */
        List<IEntity> entities = new ArrayList<>(mProjectionContainer.getEntities());
        if (wipeAllMap){
            mProjectionContainer.getEntities().clear();
        }else {
            for(IEntity e : entities){
                DataPiece d = (DataPiece)e.getEntangled();
                if (null != d) {
                    IEntity projection = d.getEntangled();
                    this.clearIndividualProjection(d);
                    d.entangleWith(null);
                    mProjectionContainer.getEntities().remove(projection);
                }
            }
        }
        for (IEntity e : entities){
            IEntity other = e.getEntangled();
            if (null != other){
                other.entangleWith(null);
            }
        }
    }

    @Override
    public void updateRequestedItems() {
        List<IEntity> entities = mDataContainer.getEntities();
        for (IEntity e : entities){
            DataPiece dataPiece = (DataPiece) e;
            if (dataPiece.isRequiresIndividualUpdate()){
                MapEntity proj = (MapEntity) dataPiece.getEntangled();
                this.clearIndividualProjection(dataPiece);
                project(dataPiece);
                dataPiece.setRequiresIndividualUpdate(false);
            }
        }
    }

    @Override
    public void createEmptyProjections() {
        for (IEntity entity : getDataContainer().getEntities()){
            DataPiece dataPiece = (DataPiece) entity;
            MapEntity emptyProjectionFor = createEmptyProjectionFor(dataPiece);
            emptyProjectionFor.entangleWith(dataPiece);
            getProjectionContainer().addEntity(emptyProjectionFor);
        }
    }

    /**
     * Helper method
     * @return socket rack taken from projections community
     */
    public SocketRack getSocketRack(){
        ProjectionsWarehouse wh = (ProjectionsWarehouse) mProjectionContainer.getCommunity();
        return wh.getSocketRack();
    }

    /**
     * We need to remove all projections from the map first, in the simpliest case
     * just call GoogleMap.clear(), but it will also remove projections belonging to all
     * other families, so it is applicable only if all families projects simoultaneously
     */
    public abstract void removeProjectionsFromMap();
    /**
     * Clears map from all projections (see method above)
     */
    public void clearMap(){
        getProjectionWarehouse().getMap().clear();
    }

    private IEntityContainer mDataContainer;
    private IEntityContainer mProjectionContainer;


/*
    private void testCircleRegion(){

        addRectangularRegion("Google HQ",
                new LatLng(37.4168, -122.0890),
                new LatLng(37.4268, -122.0790));

    }

    private static final float STROKE_SELECTED = 6.0f;
    private static final float STROKE_NORMAL = 2.0f;

    private static final int COLOR_STROKE = Color.RED;
    private static final int COLOR_FILL =
            Color.argb(20, 0, 0, 255);

    public void addRectangularRegion(String name,
                                     LatLng southwest, LatLng northeast) {
        ProjectionsWarehouse wh = (ProjectionsWarehouse) mProjectionContainer.getCommunity();
        GoogleMap map = wh.getMap();


        PolygonOptions options = new PolygonOptions().add(
                new LatLng(southwest.latitude,
                        southwest.longitude),
                new LatLng(southwest.latitude,
                        northeast.longitude),
                new LatLng(northeast.latitude,
                        northeast.longitude),
                new LatLng(northeast.latitude,
                        southwest.longitude));
//Set display properties of the shape
        options
                .strokeWidth(STROKE_NORMAL)
                .strokeColor(COLOR_STROKE)
                .fillColor(COLOR_FILL);
        p = map.addPolygon(options);
    }
    Polygon p;
    */
}
