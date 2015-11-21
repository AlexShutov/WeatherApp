package com.alex.weatherapp.MapsFramework.MapVisuals.Shapes;


import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionTypes;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Reaction;
import com.alex.weatherapp.Utils.Logger;

import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * Created by Alex on 13.11.2015.
 */
public class SelectionReaction extends Reaction {
    /** selection mode for this reaction */
    enum SelectionMode {
        SingleShape,
        MultipleShapes,
        HoverMode,
        Disabled
    }
    public static final String ACTION_SHAPE_SELECTED = "action.user_shape_selected";

    /** is used as extra in Action of ACTION_SHAPE_SELECTED
     * hover mode is the same, as single selection, with only difference, that we don't
     * force screen updates every event
     */
    public static class SelectionUpdateData{
        SelectionUpdateData(){
            isSingle = false;
            singleSelected = null;
            multipleSelections = null;
            isDeselected = false;
            isNoneSelected = false;
        }
        public boolean isNoneSelected;
        public boolean isSingle;
        public ShapeData singleSelected;
        public boolean isDeselected;
        List<ShapeData> multipleSelections;
    }

    public SelectionReaction(){
        super(ActionTypes.getActionType(ActionTypes.ACTION_MAP_TAP));
        setSelectionMode(SelectionMode.SingleShape);
        mHoverAlgotythm = new AlgorithmHoverMode(this);
    }

    @Override
    public boolean isSupportsAction(Action action) {
        boolean isSupports =
                ActionTypes.getActionType(ActionTypes.ACTION_MAP_TAP).equals(action.getActionType());
        return isSupports;
    }

    /** When user drags marker, drag events come very often, shape blinks because reprojections
     * occurs very often and algorythm thinks that those event is caused by user touching screen.
     * For handling that case we have hover algorythm, watching only when marker goes in and out of
     * shape. This reaction accepts only tap events, but drag events gets tunneled and served as
     * tap actions. We can distinguish them by method isTunneled(), and is, use hover algorythm.
     * @param action
     * @return
     */
    @Override
    protected boolean react(Action action) {
        if (mSelectionMode.equals(SelectionMode.Disabled)){
            return false;
        }
        boolean isTapped = false;
        /** safeguard for the future
         * this algorithm may take a lot of time compared to other actions, especially when map has
         * a lot of shapes. at the same time something may try to change this collection on main
         * thread, leading to exception. I encountered that during testing removal of all shapes,
         * when procedure is repeated many times. This happens very seldom, but here we can
         * get over it, because data under removal have no meaning at all.
         */
        try {
            if (action.isTunneled()) {
                isTapped = mHoverAlgotythm.handleEvent(action);
            } else {
                AlgorithmHoverMode alg = (AlgorithmHoverMode) mHoverAlgotythm;
                ShapeData sel = alg.getSelected();
                if (null != sel) {
                    sel.setSelected(false);
                    sel.setRequiresIndividualUpdate(true);
                }
                isTapped = mSelectionAlgorythm.handleEvent(action);
            }
        } catch (ConcurrentModificationException e){
            Logger.e("ConcurrentModofication exception is throws, check your algrithm");
            return false;
        }
        return true;
    }

    public void setSelectionMode(SelectionMode mode){
        mSelectionMode = mode;
        switch (mode){
            case SingleShape:
                mSelectionAlgorythm = new AlgorithmSingleShape(this);
                break;
            case MultipleShapes:
                mSelectionAlgorythm = new AlgorithmMultipleShapes(this);
                break;
            case HoverMode:
                mSelectionAlgorythm = new AlgorithmHoverMode(this);
                break;
            case Disabled:
                mSelectionAlgorythm = null;
                break;
            default:
        }
    }
    public SelectionMode getSelectionMode(){ return mSelectionMode;}

    AlgorithmOfSelection mSelectionAlgorythm;
    AlgorithmOfSelection mHoverAlgotythm;

    private SelectionMode mSelectionMode;
}