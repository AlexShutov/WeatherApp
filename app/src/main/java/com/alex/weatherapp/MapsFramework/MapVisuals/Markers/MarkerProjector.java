package com.alex.weatherapp.MapsFramework.MapVisuals.Markers;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.ProjectorBase;
import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;
import com.alex.weatherapp.MapsFramework.EntityGeneral.DataPiece;
import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;
import com.alex.weatherapp.MapsFramework.EntityGeneral.MapEntity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * Created by Alex on 09.11.2015.
 */
public class MarkerProjector extends ProjectorBase {
    public MarkerProjector(){
        super(null);
    }

    @Override
    public void project(IEntityContainer dataSource, IEntityContainer projection) {
        for (IEntity entity : getDataContainer().getEntities()){
            PlaceMarker m =  projectMarkerData((PlaceData)entity);
            m.entangleWith(entity);
            projection.addEntity(m);
        }
    }

    @Override
    public void project(DataPiece data) {
        projectMarkerData((PlaceData) data);
    }

    /** Projection container has empty projections at this point, because
     * CreateEmptyProjection action precedes Project action. When re-projecting, all
     * old projections is removed by projector as response to RemoveAllProjections command
     * @param markerData
     */
    protected PlaceMarker projectMarkerData(PlaceData markerData){
        GoogleMap map = getProjectionWarehouse().getMap();
        LocationData loc = markerData.getLocation();
        PlaceMarker markerProjection = (PlaceMarker) markerData.getEntangled();
        Marker marker = null;
        MarkerOptions options = new MarkerOptions();
        switch (markerData.getMarkerType()){
            case Standard:
                options.position(new LatLng(loc.getLat(), loc.getLon()))
                        .title(markerData.getLocation().getPlaceName())
                        .icon(BitmapDescriptorFactory.defaultMarker());
                break;
            case BitmapIcon:
               options.position(new LatLng(loc.getLat(), loc.getLon()))
                        .title(loc.getPlaceName())
                        .icon(BitmapDescriptorFactory
                                .fromResource(markerData.getBitmapIconResourceID()))
                                .alpha(markerData.getAlpha());
                break;
            default:
        }
        marker = map.addMarker(options);
        if (markerData.getIsDraggable()){
            marker.setDraggable(true);
        }
        markerData.setRequiresIndividualUpdate(false);
        markerProjection.setMarker(marker);
        markerProjection.setMarkerTitle(marker.getTitle());
        return markerProjection;
    }

    /** Here, if user acquired removing just this marker, we simply remove it from the map
     * by calling according method from Marker instance inside its projection
     * @param projectionFor
     */
    @Override
    public void clearIndividualProjection(DataPiece projectionFor) {
        PlaceMarker m = (PlaceMarker)projectionFor.getEntangled();
        /* cautionary style */
        if (null != m) {
            Marker marker = m.getMarker();
            if (null != marker){
                marker.remove();
            }
        }
    }

    @Override
    public MapEntity createEmptyProjectionFor(DataPiece dataPiece) {
        PlaceData place = (PlaceData) dataPiece;
        PlaceMarker markerProjection = new PlaceMarker();
        return markerProjection;
    }

    /** remove from map every marker from this family */
    @Override
    public void removeProjectionsFromMap() {
        for (IEntity e : getDataContainer().getEntities()){
           this.clearIndividualProjection((DataPiece) e);
        }
    }
}
