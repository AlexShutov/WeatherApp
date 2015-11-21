package com.alex.weatherapp.MapsFramework.BehaviourRelated;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.IProjector;
import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;
import com.alex.weatherapp.MapsFramework.EntityGeneral.DataPiece;
import com.alex.weatherapp.MapsFramework.EntityGeneral.MapEntity;

/**
 * Created by Alex on 14.11.2015.
 */

/**
 * It is used in tunneling events (for consistency)
 */
public class DummyProjector implements IProjector {
    @Override
    public void project(IEntityContainer dataSource, IEntityContainer projection) {

    }

    @Override
    public void project(DataPiece data) {

    }

    @Override
    public void updateRequestedItems() {

    }

    @Override
    public void createEmptyProjections() {

    }

    @Override
    public MapEntity createEmptyProjectionFor(DataPiece dataPiece) {
        return null;
    }

    @Override
    public void setProjectionContainer(IEntityContainer container) {

    }

    @Override
    public void setDataContainer(IEntityContainer container) {

    }

    @Override
    public IEntityContainer getDataContainer() {
        return null;
    }

    @Override
    public IEntityContainer getProjectionContainer() {
        return null;
    }

    @Override
    public void clearProjections(boolean wipeAllMap) {

    }

    @Override
    public void clearIndividualProjection(DataPiece projectionFor) {

    }
}
