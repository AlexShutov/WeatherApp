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
public class AlgorithmSingleShape extends AlgorithmOfSelection {
    public AlgorithmSingleShape(Reaction caller){
        super(caller);
        mShapeSelected = null;
    }

    @Override
    public boolean handleEvent(Action a) {
        /** I didn't moved this piece of code into base class, because it works as reaction on
         * projection-side. if you know, that reaction works on data side, getTargetEntity would
         * return Community, obtain ProjectionWarehouse as entangled community.
         */
        MapTapAction action = (MapTapAction)a;
        Reaction r = getReaction();
        IEntityContainer cont = (IEntityContainer)(r.getTargetEntity());
        ProjectionsWarehouse wh = (ProjectionsWarehouse) cont.getCommunity();
        SocketRack sr = wh.getSocketRack();
        ShapeProjector projector =(ShapeProjector) wh.getFamilyProjector(cont.getFamilyName());

        boolean selectionFound = false;
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
                } else
                /** tapped region, already being selected, deselect it */
                    if (mShapeSelected.equals(shapeData)){
                        mShapeSelected.setSelected(false);
                        mShapeSelected.setRequiresIndividualUpdate(true);
                        oldSelection = mShapeSelected;
                        mShapeSelected = null;
                        selectionFound = false;
                        wereDeselected = true;
                        break;
                    } else {
                        oldSelection = mShapeSelected;
                        wereDeselected = false;
                        mShapeSelected = shapeData;
                        /** selected different shape, deselect previously selected one */
                        oldSelection.setSelected(false);
                        oldSelection.setRequiresIndividualUpdate(true);
                    }
                mShapeSelected.setSelected(true);
                mShapeSelected.setRequiresIndividualUpdate(true);
            }else {
                /** were selected, but now tapped outside any region, so deselect */
                if (shapeData.isSelected()){
                    shapeData.setSelected(false);
                }
                if (mShapeSelected == shapeData){
                    mShapeSelected.setSelected(false);
                    mShapeSelected.setRequiresIndividualUpdate(true);
                    oldSelection = mShapeSelected;
                    wereDeselected = true;
                    mShapeSelected = null;
                }
            }
        }
        if (selectionFound || wereDeselected){
            sr.broadcastUpdate();
        }
        SelectionReaction.SelectionUpdateData update = new SelectionReaction.SelectionUpdateData();
        if (!selectionFound && wereDeselected) {
            Logger.i("Shape were deselected: "+ oldSelection);
            update.isDeselected = true;
            update.isNoneSelected = true;
        }else
        if (!selectionFound && !wereDeselected){
            Logger.i("No selected shape found");
            update.isNoneSelected = true;

        }else{
            Logger.i("Shape selected");
            update.isDeselected = false;
            update.singleSelected = mShapeSelected;
        }
        update.isSingle = true;
        Action updateAction = ActionTypes.getAction(SelectionReaction.ACTION_SHAPE_SELECTED, update);
        sr.reactTo(updateAction);
        return selectionFound;
    }

    private ShapeData mShapeSelected;
}
