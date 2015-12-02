package com.alex.weatherapp.MapsFramework.Interfacing.Shapes;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionTunnel;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionTypes;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MapTapAction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MarkerClickAction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.FocusAndZoomReaction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.IReaction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.IProjector;
import com.alex.weatherapp.MapsFramework.Deployment.Deployer;
import com.alex.weatherapp.MapsFramework.Deployment.FamilyBuilder;
import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;
import com.alex.weatherapp.MapsFramework.Interfacing.BehaviourBase;
import com.alex.weatherapp.MapsFramework.Interfacing.UserAdapterBase;
import com.alex.weatherapp.MapsFramework.MapVisuals.Markers.MarkerProjector;
import com.alex.weatherapp.MapsFramework.MapVisuals.Markers.PlaceData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Markers.SingleMarkerReaction;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.CircularRegionData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.DragToTapConverter;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.RectRegionData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.SelectionReaction;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.ShapeProjector;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Alex on 14.11.2015.
 */
public class ShapesAndMarkersBehaviour extends BehaviourBase {
    public static final String FAMILIES_SHAPES_FAMILY_NAME = "behaviour.shape_family";
    public static final String FAMILIES_INFO_MARKERS_FAMILY_NAME = "behaviour.info_markers_family";
    public static final String FAMILIES_LOCATION_MARKER = "behaviour.current_location_marker";

    public ShapesAndMarkersBehaviour(){
        super();
    }

    @Override
    public void setupBehaviour(Deployer deployer) {
        FamilyBuilder builder = new FamilyBuilder();

        /** Setup shapes family */
        builder.newInstance(FAMILIES_SHAPES_FAMILY_NAME);
        builder.addProjectionReaction(ActionTypes.getActionType(ActionTypes.ACTION_MAP_TAP),
                new SelectionReaction());
        IReaction drag2tapTunnel = ActionTunnel.createTunnel(new DragToTapConverter(),
                ActionTypes.getActionType(ActionTypes.ACTION_DRAG), true);
        builder.addProjectionReaction(ActionTypes.getActionType(ActionTypes.ACTION_DRAG),
                drag2tapTunnel);
        IProjector shapeProjector = new ShapeProjector();
        builder.setProjector(shapeProjector);
        deployer.addFamilyBuilder(builder);

        /** Setup single marker */
        builder = new FamilyBuilder();
        builder.newInstance(FAMILIES_LOCATION_MARKER);
        SingleMarkerReaction smr = new SingleMarkerReaction();
        builder.addProjectionReaction(ActionTypes.getActionType(ActionTypes.ACTION_MAP_TAP),
                smr);
        builder.addProjectionReaction(ActionTypes.getActionType(FocusAndZoomReaction.ACTION),
                FocusAndZoomReaction.newIsntance());
        IProjector singleMarkerProjector = new MarkerProjector();
        builder.setProjector(singleMarkerProjector);
        PlaceData locationMarkerData = new PlaceData(new LocationData(37.4218, -122.0840));
        locationMarkerData.setIsDraggable(true);
        builder.addDataPiece(locationMarkerData);
        deployer.addFamilyBuilder(builder);

        /** Add info marker family. Info marker can show some info, location of weather
         * towers, for example. It is an ordinary marker family without any reactions.
         * By default, this family is emty;
         */

        builder = new FamilyBuilder();
        builder.newInstance(FAMILIES_INFO_MARKERS_FAMILY_NAME);
        MarkerProjector infoMarkerProjector = new MarkerProjector();
        builder.setProjector(infoMarkerProjector);
        deployer.addFamilyBuilder(builder);
    }

    @Override
    protected void onDeloymentCompletion(UserAdapterBase adapter) {
    }

    @Override
    protected UserAdapterBase createUserAdapter(Deployer deployer) {
        return new ShapesDisplayAdapter();
    }

    @Override
    public ISysShapesDisplay getUserInterface() {
        return (ISysShapesDisplay) getUserAdapter();
    }
}
