package com.alex.weatherapp.MapsFramework.MapVisuals.Shapes;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionTypes;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MapTapAction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Reaction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.SocketRack;
import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;
import com.alex.weatherapp.MapsFramework.Containers.ProjectionsWarehouse;
import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;
import com.alex.weatherapp.Utils.Logger;

/**
 * Created by Alex on 13.11.2015.
 */
public class AlgorithmHoverMode extends AlgorithmOfSelection {
    public AlgorithmHoverMode(Reaction r){
        super(r);
        mShapeSelected = null;
    }

    @Override
    public boolean handleEvent(Action a) {
        MapTapAction action = (MapTapAction)a;
        Reaction r = getReaction();
        IEntityContainer cont = (IEntityContainer)(r.getTargetEntity());
        ProjectionsWarehouse wh = (ProjectionsWarehouse) cont.getCommunity();
        SocketRack sr = wh.getSocketRack();
        ShapeProjector projector =(ShapeProjector) wh.getFamilyProjector(cont.getFamilyName());

        boolean selectionFound = false;
        boolean isUpdateRequired = false;
        boolean wereDeselected = false;
        ShapeData oldSelection = mShapeSelected;
        for (IEntity e : cont.getEntities()){
            ShapeProjection shapeProjection  = (ShapeProjection) e;
            ShapeData shapeData = (ShapeData) shapeProjection.getEntangled();
            boolean isTapPointInside = shapeProjection.isTapped(projector, (MapTapAction)action);

            if (isTapPointInside){
                selectionFound = true;
                if (null == mShapeSelected){
                    mShapeSelected = shapeData;
                    isUpdateRequired = true;
                    selectionFound = true;
                } else
                if (!shapeData.isSelected()){
                    mShapeSelected.setSelected(false);
                    mShapeSelected.setRequiresIndividualUpdate(true);
                    mShapeSelected = shapeData;
                    shapeData.setSelected(true);
                    shapeData.setRequiresIndividualUpdate(true);
                    isUpdateRequired = true;
                }
            }else {
                if (mShapeSelected == shapeData){

                    mShapeSelected.setSelected(false);
                    mShapeSelected.setRequiresIndividualUpdate(true);
                    mShapeSelected = null;
                    wereDeselected = true;
                    isUpdateRequired = true;
                }
            }
        }
        if (isUpdateRequired){
            sr.broadcastUpdate();
        }
        SelectionReaction.SelectionUpdateData update = new SelectionReaction.SelectionUpdateData();
        update.isSingle = true;
        Action updateAction = ActionTypes.getAction(SelectionReaction.ACTION_SHAPE_SELECTED, update);
        if (!selectionFound && !wereDeselected){
            Logger.i("No selected shape found");
            update.isNoneSelected = true;
        }else
        if (!selectionFound && wereDeselected) {
            Logger.i("Shape were deselected: "+ oldSelection);
            update.singleSelected = oldSelection;
            update.isNoneSelected = true;
            update.isDeselected = true;
            sr.reactTo(updateAction);
        }else
        if (oldSelection != mShapeSelected){
            Logger.i("Shape selected");
            update.singleSelected = mShapeSelected;
            sr.reactTo(updateAction);
        }
        return selectionFound;
    }
    public ShapeData getSelected(){
        return mShapeSelected;
    }

    private ShapeData mShapeSelected;
}
