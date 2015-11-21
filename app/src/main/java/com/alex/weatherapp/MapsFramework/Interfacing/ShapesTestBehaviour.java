package com.alex.weatherapp.MapsFramework.Interfacing;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionTunnel;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionType;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionTypes;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MarkerClickAction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.FocusAndZoomReaction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.IReaction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.LoggingReactionDecorator;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.IProjector;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.LogginProjectorDecorator;
import com.alex.weatherapp.MapsFramework.Containers.EntityContainer;
import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;
import com.alex.weatherapp.MapsFramework.Deployment.Deployer;
import com.alex.weatherapp.MapsFramework.Deployment.FamilyBuilder;
import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.ISysShapesDisplay;
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.ShapesDisplayAdapter;
import com.alex.weatherapp.MapsFramework.MapVisuals.Markers.MarkerProjector;
import com.alex.weatherapp.MapsFramework.MapVisuals.Markers.PlaceData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.CircularRegionData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.DragToTapConverter;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.RectRegionData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.SelectionReaction;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.ShapeProjector;
import com.alex.weatherapp.Utils.Logger;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Alex on 14.11.2015.
 */
public class ShapesTestBehaviour extends BehaviourBase {
    public ShapesTestBehaviour(){
        super();
    }

    @Override
    public void setupBehaviour(Deployer deployer) {

        Set<ActionType> defaultActions = ActionTypes.getDefaultActions();

        String familyName = "Dummy family";
        FamilyBuilder builder = new FamilyBuilder();
        builder.newInstance(familyName);
        //IEntityContainer dataFamily = new EntityContainer();
        ////dataFamily.setCommunity(dataStore);
        LocationData tmpLoc = new LocationData(37.4218, -122.0840, "Some place");
        PlaceData tmpPlace = new PlaceData(tmpLoc);
        //dataFamily.addEntity(tmpPlace);
        tmpPlace.setMarkerType(PlaceData.MarkerType.BitmapIcon);
        tmpPlace.setIsDraggable(true);
        builder.addDataPiece(tmpPlace);


        LocationData loc2 = new LocationData(37.4118, -122.0740, "Neighbor #1");
        PlaceData place2 = new PlaceData(loc2);
        place2.setIsDraggable(true);
        //dataFamily.addEntity(place2);
        builder.addDataPiece(place2);

        //IReactionComposite reacts = new ReactionComposite();
        LoggingReactionDecorator logReact = new LoggingReactionDecorator();
        logReact.setFamilyDetails("Family name: " + familyName);
        //reacts.add(logReact);
        //dataFamily.setReactions(new ReactionComposite());
        Set<ActionType> t2 =new HashSet<>(defaultActions);

        ActionType userType  = new ActionType();
        userType.setAction("User action");
        t2.add(userType);
        builder.addProjectionReaction(logReact, t2);

        //IEntityContainer projectionFamily = new EntityContainer();
        IReaction anonymousLogger = new IReaction() {
            @Override
            public boolean reactTo(Action action) {
                ActionType markerClick = ActionTypes.getActionType(ActionTypes.ACTION_MARKER_CLICK);
                if (action.getActionType().equals(markerClick)){
                    MarkerClickAction clickAction = (MarkerClickAction) action;
                    Logger.i("Action received by anonymous projection reaction: " +
                            "'marker click'");
                    return false;
                }
                return false;
            }

            @Override
            public boolean isSupportsAction(Action action) {
                return true;
            }

            @Override
            public void setTargetEntity(IEntity target) {

            }

            @Override
            public IEntity getTargetEntity() {
                return null;
            }
        };
        builder.addProjectionReaction(anonymousLogger, t2);

        //reacts.add(anonymousLogger);
        //projectionFamily.setReactions(reacts);
        //builder.addProjectionReaction(reacts, defaultActions);
        /** Enable logging */
        LogginProjectorDecorator projLogger = new LogginProjectorDecorator();
        projLogger.setProjectorName("Dummy projector");
        IProjector projector = new MarkerProjector();
        projLogger.setDecorated(projector);

        builder.setProjector(projLogger);
        deployer.addFamilyBuilder(builder);

        /*
        Family f = builder.build(mSocketRack);

        ProjectorConstructionData prBuildData = new ProjectorConstructionData();
        prBuildData.setDataContainer(f.getDataContainer());
        prBuildData.setProjectionContainer(f.getProjectionContainer());
        prBuildData.setFamilyName(f.getFamilyName());
        prBuildData.setProjector(f.getProjector());
        prBuildData.setSupportedEvents(ActionTypes.getProjectionActions());
        projectionsStore.setProjectorData(prBuildData);

        IEntityContainer dataFamily = f.getDataContainer();
        familyName = f.getFamilyName();


        dataStore.addFamily(dataFamily, familyName, f.getDataActions());
        //dataStore.assignNewObligations(familyName, defaultActions);

        projectionsStore.addFamily(f.getProjectionContainer(),
                f.getFamilyName(), f.getProjectionActions());
*/



        /////////////////////////////////////////////////////////
        String familyName2 = "Second family";
        builder = new FamilyBuilder();
        builder.newInstance(familyName2);
        //IEntityContainer dataFamily2 = new EntityContainer();
        LocationData loc3 = new LocationData(37.4218, -122.0740, "Some place 2");
        PlaceData place3 = new PlaceData(loc3);
        place3.setIsDraggable(true);
        builder.addDataPiece(place3);
        //dataFamily2.addEntity(place3);
        //dataFamily2.setReactions(new ReactionComposite());



        IEntityContainer projectionFamily2 = new EntityContainer();
        IReaction anonymousLogger2 = new IReaction() {
            @Override
            public boolean reactTo(Action action) {
                ActionType markerClick = ActionTypes.getActionType(ActionTypes.ACTION_MARKER_CLICK);
                if (action.getActionType().equals(markerClick)){
                    MarkerClickAction clickAction = (MarkerClickAction) action;
                    Logger.i("Family 2 logger: "+
                            "'marker click' " );// + clickAction.getClickedMarker().getTitle());
                    /*
                    if (clickAction.getClickedMarker().getTitle().equals("Some place 2")){
                        return true;
                    }else {
                        return false;
                    }
                    */
                    return true;
                }
                return false;
            }

            @Override
            public boolean isSupportsAction(Action action) {
                return true;
            }
            @Override
            public void setTargetEntity(IEntity target) {
            }

            @Override
            public IEntity getTargetEntity() {
                return null;
            }
        };
        //IReactionComposite r2c = new ReactionComposite();
        //r2c.add(anonymousLogger2);
        //projectionFamily2.setReactions(r2c);
        builder.addProjectionReaction(anonymousLogger2, t2);
        IProjector f2Proj = new MarkerProjector();
        builder.setProjector(f2Proj);

        deployer.addFamilyBuilder(builder);


        /////////////////////////////////////////////////////////
        String shapesFamily = "Shapes";
        builder = new FamilyBuilder();
        builder.newInstance(shapesFamily);
        builder.addProjectionReaction(ActionTypes.getActionType(ActionTypes.ACTION_MAP_TAP),
                new SelectionReaction());
        IReaction r = ActionTunnel.createTunnel(new DragToTapConverter(),
                ActionTypes.getActionType(ActionTypes.ACTION_DRAG), true);
        builder.addProjectionReaction(ActionTypes.getActionType(ActionTypes.ACTION_DRAG), r);

        builder.addProjectionReaction(ActionTypes.getActionType(FocusAndZoomReaction.ACTION),
                FocusAndZoomReaction.newIsntance());

        RectRegionData rect = new RectRegionData();
        rect.setRightBottom(new LatLng(37.4169, -122.0890));
        rect.setTopLeft(new LatLng(37.4269, -122.0790));
        builder.addDataPiece(rect);

        rect = new RectRegionData();

        //rect.setRightBottom(new LatLng(37.4067, -122.0890));
        //rect.setTopLeft(new LatLng(37.4167, -122.0990));
        //builder.addDataPiece(rect);

        rect = new RectRegionData();
        rect.setTopLeft(new LatLng(37.4269, -122.0890));
        rect.setRightBottom(new LatLng(37.4169, -122.10));

        builder.addDataPiece(rect);


        CircularRegionData circle = new CircularRegionData(new LatLng(37.4318, -122.0940), 400);
        builder.addDataPiece(circle);
        IProjector shapeProjector = new ShapeProjector();
        builder.setProjector(shapeProjector);
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
