package com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections;

/**
 * Created by Alex on 07.11.2015.
 */

import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;
import com.alex.weatherapp.MapsFramework.EntityGeneral.DataPiece;
import com.alex.weatherapp.MapsFramework.EntityGeneral.Entity;
import com.alex.weatherapp.MapsFramework.EntityGeneral.MapEntity;
import com.google.android.gms.maps.GoogleMap;

/**
 * Projects data entity into map-specific entity. Implement, or subclass its implementation
 * for getting support of different map items (markers, areas, etc.)
 */
public interface IProjector {
    /**
     * performs projections and writes it to projection . Doesn't return any value, accepts projection
     * instead. This is because each piece of data is tied to its projection on creation stage.
     * @param dataSource data to project
     * @param projection container, into which projection is written to
     * @return
     */
    void project(IEntityContainer dataSource, IEntityContainer projection);

    void project(DataPiece data);

    /** use this method if you want to update just one item in some family, instead of
     * clearing projections and projecting entire family of objects.
     * goes throught all data items, and if item is marked for update, first clears projection
     * of that item and then reprojects it and clears update flag.
     */
    void updateRequestedItems();

    /**
     * Projector need to know about map instance, because it accesses it directly
     * during projection. If you need to chane map instance, yout must first pause event pump,
     * then set new map for each family' projector
     * You can get map from ProjectionsWarehouse, for this go by link
     * this->getDataContainer->getCommunity->getMap(). do it once
     * @param map
     */
    //void setMap(GoogleMap map);

    /**
     * Creates empty projection and binds it to according data. Use it during initialization.
     * Clone prototype for each item in dataSource and bind data and projection together.
     * We already have data and projectioin containers
     */
    void createEmptyProjections();

    /** Create and retrive an empty projection instance for a piece of data. This method makes
     * sense, because data item may contain additional information influencing resulting
     * projection.
     * @param dataPiece
     * @return
     */
    MapEntity createEmptyProjectionFor(DataPiece dataPiece);

    /**
     * Projector need to know where to stack initial results, or where to store results if
     * projection need to erase previous result. In later case, projection removes old result from
     * container, the creates new result, binds it to data and puts it into container
     * @param container
     */
    void setProjectionContainer(IEntityContainer container);

    /**
     * The same, as setProjectionContainer
     * @param container
     */
    void setDataContainer(IEntityContainer container);

    IEntityContainer getDataContainer();
    IEntityContainer getProjectionContainer();


    /** remove all projections, re-creation is neccessary */
    void clearProjections( boolean wipeAllMap);

    /** remove given individual projection from the map */
    void clearIndividualProjection(DataPiece projectionFor);
}
