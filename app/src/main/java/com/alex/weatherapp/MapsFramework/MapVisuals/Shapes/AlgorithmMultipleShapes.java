package com.alex.weatherapp.MapsFramework.MapVisuals.Shapes;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionTypes;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MapTapAction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Reaction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.SocketRack;
import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;
import com.alex.weatherapp.MapsFramework.Containers.ProjectionsWarehouse;
import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 13.11.2015.
 */

/** we don't save all selected shapes, because we can get them from data container
 * by using isSelected() flag accessor
 * @return
 */
public class AlgorithmMultipleShapes extends AlgorithmOfSelection {
    public AlgorithmMultipleShapes(Reaction r){
        super(r);
        mPreviouslySelected = new ArrayList<>();
    }

    @Override
    public boolean handleEvent(Action a) {
        MapTapAction action = (MapTapAction)a;
        boolean atLeastOneSelected = false;
        IEntityContainer projCont = (IEntityContainer)(getReaction().getTargetEntity());
        ProjectionsWarehouse wh = (ProjectionsWarehouse) projCont.getCommunity();
        SocketRack sr = wh.getSocketRack();
        ShapeProjector projector =(ShapeProjector) wh.getFamilyProjector(projCont.getFamilyName());
        boolean selectionChanged = false;
        List<ShapeData> selectedShapes = new ArrayList<>();
        for (IEntity e : projCont.getEntities()){
            ShapeProjection shapeProjection  = (ShapeProjection) e;
            ShapeData shapeData = (ShapeData) shapeProjection.getEntangled();
            boolean isTapped = shapeProjection.isTapped(projector, action);
            if (isTapped){
                selectionChanged = true;
                if (shapeData.isSelected()){
                    shapeData.setSelected(false);
                    shapeData.setRequiresIndividualUpdate(true);
                }else {
                    shapeData.setSelected(true);
                    atLeastOneSelected = true;
                    selectedShapes.add(shapeData);
                    shapeData.setRequiresIndividualUpdate(true);
                }
            }else {
                if (shapeData.isSelected()){
                    selectedShapes.add(shapeData);
                }
            }
        }
        SelectionReaction.SelectionUpdateData update =
                new SelectionReaction.SelectionUpdateData();
        update.isSingle = false;
        if (selectionChanged ){
            update.multipleSelections = selectedShapes;
            /** shape were deselected */
            if (mPreviouslySelected.size() > selectedShapes.size()){
                update.isDeselected = true;
                mPreviouslySelected.removeAll(selectedShapes);
                update.singleSelected = mPreviouslySelected.get(0);
            } else {
                /** some shape were selected */
                update.isDeselected = false;
                List<ShapeData> tmp = new ArrayList<>(selectedShapes);
                tmp.removeAll(mPreviouslySelected);
                update.singleSelected = tmp.get(0);
            }
            update.multipleSelections = selectedShapes;
            mPreviouslySelected = selectedShapes;
            sr.broadcastUpdate();
            Action updateAction = ActionTypes.getAction(SelectionReaction.ACTION_SHAPE_SELECTED, update);
            sr.reactTo(updateAction);
        }

        return  atLeastOneSelected;
    }

    private List<ShapeData> mPreviouslySelected;
}
