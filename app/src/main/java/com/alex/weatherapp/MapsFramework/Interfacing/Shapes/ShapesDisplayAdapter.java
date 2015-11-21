package com.alex.weatherapp.MapsFramework.Interfacing.Shapes;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionTypes;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MapTapAction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MarkerClickAction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MarkerEndDragAction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.FocusAndZoomReaction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.IProjector;
import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;
import com.alex.weatherapp.MapsFramework.Containers.ProjectionsWarehouse;
import com.alex.weatherapp.MapsFramework.Deployment.Deployer;
import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;
import com.alex.weatherapp.MapsFramework.Interfacing.IFeedbackInterface;
import com.alex.weatherapp.MapsFramework.Interfacing.UserAdapterBase;
import com.alex.weatherapp.MapsFramework.MapFacade;
import com.alex.weatherapp.MapsFramework.MapVisuals.Markers.MarkerProjector;
import com.alex.weatherapp.MapsFramework.MapVisuals.Markers.PlaceData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Markers.PlaceMarker;
import com.alex.weatherapp.MapsFramework.MapVisuals.Markers.SingleMarkerReaction;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.CircularRegionData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.RectRegionData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.SelectionReaction;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.ShapeData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.ShapeProjection;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.ShapeProjector;
import com.alex.weatherapp.Utils.Logger;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

/**
 * Created by Alex on 14.11.2015.
 */
public class ShapesDisplayAdapter extends UserAdapterBase implements ISysShapesDisplay {
    private static final String infoMarkersFamily =
            ShapesAndMarkersBehaviour.FAMILIES_INFO_MARKERS_FAMILY_NAME;
    private static final String shapesFamily = ShapesAndMarkersBehaviour.FAMILIES_SHAPES_FAMILY_NAME;

    public ShapesDisplayAdapter(){
        super();
    }

    @Override
    public void activateAdapter(Deployer deployer) {
        //* subscribe to actions being broadcast when shape is selected */
        this.addFeedbackAction(ActionTypes.getActionType(SelectionReaction.ACTION_SHAPE_SELECTED));
        this.addFeedbackAction(ActionTypes.getActionType(SingleMarkerReaction.FEEDBACK_SINGLE_MARKER_CLICK));
        this.addFeedbackAction(ActionTypes.getActionType(ActionTypes.ACTION_DRAG_END));
        this.addFeedbackAction(ActionTypes.getActionType(ActionTypes.ACTION_MARKER_CLICK));
    }

    @Override
    public void handleAction(Action action) {
        IFeedbackShapes fdb = (IFeedbackShapes) getFeedbackInterface();
        switch (action.getActionType().getAction()){
            case SelectionReaction.ACTION_SHAPE_SELECTED:
                parseAndHandleShapeSelectionAction(action);
                break;
            case SingleMarkerReaction.FEEDBACK_SINGLE_MARKER_CLICK:
                if (action.isTunneled()) {
                    break;
                }
                LatLng point = (LatLng) action.getActionType().getExtra();
                LocationData ld = new LocationData(point.latitude, point.longitude);
                fdb.onNewPlacePinned(ld);
                break;
            case ActionTypes.ACTION_DRAG_END:
                MarkerEndDragAction da = (MarkerEndDragAction) action;
                LatLng pos = da.getMarker().getPosition();
                LocationData dragEndPos = new LocationData(pos.latitude, pos.longitude);
                fdb.onNewPlacePinned(dragEndPos);
                break;
            case ActionTypes.ACTION_MARKER_CLICK:
                checkWhetherInfoMarkerIsClicked(action);
                break;
            default:
                Logger.e("User adapter has received unknown action ");

        }
    }

    private void parseAndHandleShapeSelectionAction(Action action){
        IFeedbackShapes fdb = (IFeedbackShapes) getFeedbackInterface();
        String msg = "";
        SelectionReaction.SelectionUpdateData d = (SelectionReaction.SelectionUpdateData)
                action.getActionType().getExtra();
        if (d.isNoneSelected){
            msg = "None shape is selected";
            fdb.onNothingSelected();
            return;
        }
        if (!d.isDeselected){
            msg += "Shape is selected ";
            ShapeData selected = (ShapeData) d.singleSelected;
            if (selected instanceof CircularRegionData){
                fdb.onCircularRegionSelected((CircularRegionData) selected);
            } else
                if (selected instanceof RectRegionData){
                    fdb.onRectRegionSelected((RectRegionData) selected);
                }
        } else {
            msg += "Shape is deselected ";
        }
        Logger.i(msg);
    }

    private void checkWhetherInfoMarkerIsClicked(Action action){
        MapFacade facade = getFacade();
        IEntityContainer dataCont = facade.getDataStore().getFamily(infoMarkersFamily);
        IEntityContainer projCont = (IEntityContainer) dataCont.getEntangled();
        MarkerClickAction mca = (MarkerClickAction) action;
        Marker clicked = mca.getClickedMarker();
        PlaceData clickedData = null;
        for (IEntity e : projCont.getEntities()){
            PlaceMarker marker = (PlaceMarker) e;
            if (marker.getMarker().equals(clicked)){
                clickedData = (PlaceData) e.getEntangled();
                break;
            }
        }
        if (null != clickedData){
            Logger.d("Info marker is clicked: " + clickedData.getLocation().getmPlaceName());
            getFeedbackInterface().onInfoMarkerClick(clickedData);
        }
    }

    @Override
    public void removeAllShapes() {
        MapFacade facade = getFacade();
        IEntityContainer dataCont = facade.getDataStore().getFamily(shapesFamily);
        IEntityContainer projCont = (IEntityContainer) dataCont.getEntangled();
        ShapeProjector projector = (ShapeProjector) getFacade().getProjectionWarehouse()
                .getFamilyProjector(shapesFamily);
        List<IEntity> shapesData = dataCont.getEntities();

        /** we need to stop accepting any messages, otherwise worker thread might make attempt of
         * removing some item from collections, causing ConcurrentModification exception
         */
        getFacade().getSocketRack().deactivate();

        projector.clearProjections(false);
        projCont.getEntities().clear();
        dataCont.getEntities().clear();
        getFacade().getSocketRack().activate();
    }

    @Override
    public boolean removeShape(String shapeName) {
        MapFacade facade = getFacade();
        IEntityContainer dataCont = facade.getDataStore().getFamily(shapesFamily);
        IEntityContainer projCont = (IEntityContainer) dataCont.getEntangled();
        ShapeData shape = getShape(shapeName);
        if (null == shape) {
            return false;
        }
        ShapeProjector projector = (ShapeProjector) getFacade().getProjectionWarehouse()
                .getFamilyProjector(shapesFamily);
        projector.clearIndividualProjection(shape);

        /** make map unresponsive for a very short period of time */
        //getFacade().getSocketRack().deactivate();
        projCont.getEntities().remove(shape.getEntangled());
        dataCont.getEntities().remove(shape);
        //getFacade().getSocketRack().activate();
        return true;
    }

    @Override
    public ShapeData getShape(String shapeName) {
        MapFacade facade = getFacade();
        IEntityContainer dataCont = facade.getDataStore().getFamily(shapesFamily);
        IEntityContainer projCont = (IEntityContainer) dataCont.getEntangled();
        ShapeData foundShape = null;
        for (IEntity e : dataCont.getEntities()){
            ShapeData s = (ShapeData) e;
            if (s.getShapeName().equals(shapeName)){
                foundShape = s;
                break;
            }
        }
        return foundShape;
    }

    @Override
    public void addCircularArea(CircularRegionData shapeData) {
        Logger.i("Adding an circular area");
        addShape(shapeData);
    }
    @Override
    public void addRectangularArea(RectRegionData shapeData) {
        Logger.i("Adding an rectangular area");
        addShape(shapeData);
    }

    protected void addShape(ShapeData shapeData){
        MapFacade facade = getFacade();
        IEntityContainer dataCont = facade.getDataStore().getFamily(shapesFamily);
        IEntityContainer projCont = (IEntityContainer) dataCont.getEntangled();

        shapeData.setRequiresIndividualUpdate(true);
        int encounterCnt = 0;
        for (IEntity e : dataCont.getEntities()){
            ShapeData s = (ShapeData)e;
            if (s.getShapeName().equals(shapeData.getShapeName())){
                String newShapeName = shapeData.getShapeName() + "_" + String.valueOf(encounterCnt);
                shapeData.setShapeName(newShapeName);
            }
        }
        ProjectionsWarehouse wh = (ProjectionsWarehouse) projCont.getCommunity();
        ShapeProjector projector = (ShapeProjector) wh.getFamilyProjector(shapesFamily);
        ShapeProjection shapeProj = (ShapeProjection) projector.createEmptyProjectionFor(shapeData);
        shapeProj.entangleWith(shapeData);
        dataCont.addEntity(shapeData);
        projCont.addEntity(shapeProj);
        projector.updateRequestedItems();
    }

    /** algorithm for multiple selections tracks previously selected shapes, so we can't just
     * mark shape as selected and reproject it. We need to find out center of the shape
     * and broadcast action, marked as tunneled, so selection algorithm would accept it,
     * but current location ignores, as thought user tapped the center of requested shape
     * It assumes that all shapes is having a unique name and we know about it. It is true
     * for geo areas.
     */
    @Override
    public boolean selectShape(String shapeName, boolean moveCam) {
        MapFacade facade = getFacade();
        IEntityContainer dataCont = facade.getDataStore().getFamily(shapesFamily);
        IEntityContainer projCont = (IEntityContainer) dataCont.getEntangled();
        ShapeData ourShape = null;
        for (IEntity e : dataCont.getEntities()){
            ShapeData shape = (ShapeData) e;
            if (shape.getShapeName().equals(shapeName)){
                ourShape = shape;
                break;
            }else if (shape.isSelected()){
                if (mIsSingleSelection){
                    shape.setSelected(false);
                }
            }
        }
        if (null == ourShape){
            Logger.w("Couldn't find requested shape, aborting");
            return false;
        }
        LatLng centerPoint = null;
        if (ourShape instanceof CircularRegionData){
            LatLng t =((CircularRegionData) ourShape).getCenter();
            centerPoint = new LatLng(t.latitude, t.longitude);
        }
        else
        if (ourShape instanceof RectRegionData){
            RectRegionData r = (RectRegionData) ourShape;
            LatLng rb = r.getRightBottom();
            LatLng tl = r.getTopLeft();
            centerPoint = new LatLng( (tl.latitude + rb.latitude) / 2.0,
                    (tl.longitude + rb.longitude) / 2.0);
        }
        Action a = new MapTapAction();
        a.setIsTunneled(false);
        a.getActionType().setExtra(centerPoint);
        facade.getSocketRack().reactTo(a);

        if (moveCam) {
            Action focus = ActionTypes.getAction(FocusAndZoomReaction.ACTION);
            FocusAndZoomReaction.FocusAndZoomData focusData = new FocusAndZoomReaction.FocusAndZoomData();
            focusData.centerOfScreen = centerPoint;
            focus.getActionType().setExtra(focusData);
            facade.getSocketRack().reactTo(focus);
        }
        return true;
    }

    @Override
    public boolean deselectShape(String shapeName) {
        MapFacade facade = getFacade();
        IEntityContainer dataCont = facade.getDataStore().getFamily(shapesFamily);
        IEntityContainer projCont = (IEntityContainer) dataCont.getEntangled();
        boolean wereSelected = true;
        for (IEntity e : dataCont.getEntities()){
            ShapeData shape = (ShapeData) e;
            if (shape.getShapeName().equals(shapeName)){
                wereSelected = shape.isSelected();
                if (wereSelected) {
                    shape.setSelected(false);
                    shape.setRequiresIndividualUpdate(true);
                }
                break;
            }else if (shape.isSelected()){
                if (mIsSingleSelection){
                    shape.setSelected(false);
                }
            }
        }
        if (wereSelected) {
            ProjectionsWarehouse wh = (ProjectionsWarehouse) projCont.getCommunity();
            IProjector projector = wh.getFamilyProjector(shapesFamily);
            projector.updateRequestedItems();
        }
        return true;
    }

    /**
     * Wipes out all previous info markers and show the new ones
     * @param placesDetails
     */
    @Override
    public void showInfoMarkers(List<PlaceData> placesDetails) {

        if (null == placesDetails || placesDetails.isEmpty()){
            Logger.w("Trying to add empty collection of data markers. Removing instead");
            return;
        }
        removeInfoMarkers();
        MapFacade facade = getFacade();
        List<IEntity> markersData = facade.getDataStore().getFamily(infoMarkersFamily).getEntities();
        List<IEntity> markersProj = facade.getProjectionWarehouse().getFamily(infoMarkersFamily)
                .getEntities();
        IProjector projector = getFacade().getProjectionWarehouse()
                .getFamilyProjector(infoMarkersFamily);
        for (PlaceData d : placesDetails){
            d.setIsDraggable(false);
            d.setRequiresIndividualUpdate(true);
            markersData.add(d);
            d.setRequiresIndividualUpdate(true);
            PlaceMarker m = new PlaceMarker();
            m.entangleWith(d);
            markersProj.add(m);
        }
        projector.updateRequestedItems();
        /** now add a new ones */
    }

    @Override
    public void removeInfoMarkers() {
        IProjector projector = getFacade().getProjectionWarehouse()
                .getFamilyProjector(infoMarkersFamily);
        projector.clearProjections(false);
        List<IEntity> markersData = getFacade().getDataStore()
                .getFamily(infoMarkersFamily).getEntities();
        List<IEntity> markersProj = getFacade().getProjectionWarehouse().getFamily(infoMarkersFamily)
                .getEntities();
        markersData.clear();
        markersProj.clear();
    }

    @Override
    public void addInfoMarker(PlaceData markerData) {
        if (null == markerData){
            Logger.w("Trying to add an empty marker, aborting");
            return;
        }
        MapFacade facade = getFacade();
        List<IEntity> dataCont = facade.getDataStore().getFamily(infoMarkersFamily).getEntities();
        List<IEntity> projCont = facade.getProjectionWarehouse().getFamily(infoMarkersFamily)
                .getEntities();
        IProjector projector = getFacade().getProjectionWarehouse()
                .getFamilyProjector(infoMarkersFamily);

        PlaceMarker proj = new PlaceMarker();
        proj.entangleWith(markerData);
        markerData.setIsDraggable(false);
        markerData.setRequiresIndividualUpdate(true);
        dataCont.add(markerData);
        projCont.add(proj);
        projector.updateRequestedItems();
    }

    public void setSingleSelectionMode( boolean isIt){
        mIsSingleSelection = isIt;
    }
    public boolean isSingleSelection(){ return  mIsSingleSelection;}

    @Override
    public boolean removeInfoMarekr(PlaceData markerData) {
        MapFacade facade = getFacade();
        IEntityContainer dataCont = facade.getDataStore().getFamily(infoMarkersFamily);
        IEntityContainer projCont = (IEntityContainer) dataCont.getEntangled();
        MarkerProjector projector = (MarkerProjector) getFacade().getProjectionWarehouse()
                .getFamilyProjector(infoMarkersFamily);
        boolean isFound = false;
        for (int i = 0; i < dataCont.getEntities().size(); ++i){
            PlaceData currMarker = (PlaceData) dataCont.getEntities().get(i);
            boolean equals = true;
            equals &= currMarker.getLocation().equals(markerData.getLocation());
            if (equals){
                isFound = true;
                projector.clearIndividualProjection(currMarker);
                IEntity currProj = currMarker.getEntangled();
                projCont.getEntities().remove(currProj);
                dataCont.getEntities().remove(currMarker);
                Logger.i("Info marker " + markerData.getLocation().getmPlaceName() + " is removed");
                break;
            }
        }
        return isFound;
    }

    @Override
    public IFeedbackShapes getFeedbackInterface() {
        return mFeedback;
    }
    @Override
    public void setFeedbackInterface(IFeedbackInterface iface) {
        mFeedback = (IFeedbackShapes) iface;
    }

    IFeedbackShapes mFeedback;
    private boolean mIsSingleSelection;
}
