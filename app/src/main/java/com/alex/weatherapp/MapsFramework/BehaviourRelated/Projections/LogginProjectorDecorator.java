package com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections;

import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;
import com.alex.weatherapp.MapsFramework.EntityGeneral.DataPiece;
import com.alex.weatherapp.MapsFramework.EntityGeneral.MapEntity;
import com.alex.weatherapp.Utils.Logger;

/**
 * Created by Alex on 09.11.2015.
 */
public class LogginProjectorDecorator implements IProjector {
    public LogginProjectorDecorator(){
        mDecorated = null;
    }

    @Override
    public void project(IEntityContainer dataSource, IEntityContainer projection) {
        Logger.i("Projector log for '" + getProjectorName()+"':project method is called ");
        if (null == mDecorated) return;
        mDecorated.project(dataSource, projection);
    }

    @Override
    public void project(DataPiece data) {
    }

    @Override
    public void updateRequestedItems() {
        Logger.i("updateRequestedItems method is called ");
        if (null == mDecorated) return;
        mDecorated.updateRequestedItems();
    }

    @Override
    public MapEntity createEmptyProjectionFor(DataPiece dataPiece) {
        Logger.i("createEmptyProjectionFor method is called for " + dataPiece);
        if (null == mDecorated) return null;
        return mDecorated.createEmptyProjectionFor(dataPiece);
    }

    @Override
    public void createEmptyProjections() {
        Logger.i("Projector log for '" + getProjectorName()+
                "': createEmptyProjections method is called ");
        if (null == mDecorated)return;
        mDecorated.createEmptyProjections();
    }

    @Override
    public void setProjectionContainer(IEntityContainer container) {
        if (null == mDecorated) return;
        mDecorated.setProjectionContainer(container);
    }

    @Override
    public void setDataContainer(IEntityContainer container) {
        if (null == mDecorated) return;
        mDecorated.setDataContainer(container);
    }

    @Override
    public IEntityContainer getDataContainer() {
        if (null == mDecorated) return null;
        return mDecorated.getDataContainer();
    }

    @Override
    public IEntityContainer getProjectionContainer() {
        if (null == mDecorated) return null;
        return mDecorated.getProjectionContainer();
    }

    @Override
    public void clearProjections(boolean wipeAllMap) {
        String modeDetail = wipeAllMap ? "wiping entire map": "clearing family projections";
        Logger.i("Projector log for '" + getProjectorName() + "': clearProjections method is called "+
        modeDetail);
        if (null == mDecorated) return;
        mDecorated.clearProjections(wipeAllMap);
    }

    @Override
    public void clearIndividualProjection(DataPiece projectionFor) {
        Logger.i("Clear Individual projection method is called ");
        if (null == mDecorated) return;
        mDecorated.clearIndividualProjection(projectionFor);
    }

    public void setDecorated(IProjector projector){
        mDecorated = projector;
    }
    public IProjector getDecorated(){
        return mDecorated;
    }
    public void setProjectorName(String name){
        mProjectorName = name;
    }
    public String getProjectorName(){
        return mProjectorName;
    }

    private String mProjectorName;
    private IProjector mDecorated;
}
