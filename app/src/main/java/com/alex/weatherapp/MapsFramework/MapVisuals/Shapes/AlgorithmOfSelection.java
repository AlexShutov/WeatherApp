package com.alex.weatherapp.MapsFramework.MapVisuals.Shapes;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Reaction;

/**
 * Created by Alex on 13.11.2015.
 */
public abstract class AlgorithmOfSelection {
    public AlgorithmOfSelection(Reaction caller){
        mCaller = caller;
    }

    public abstract boolean handleEvent(Action action);
    protected Reaction getReaction(){ return mCaller;}

    private Reaction mCaller;
}
