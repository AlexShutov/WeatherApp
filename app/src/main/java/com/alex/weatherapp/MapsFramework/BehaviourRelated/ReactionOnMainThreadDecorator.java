package com.alex.weatherapp.MapsFramework.BehaviourRelated;

import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MarkerDragAction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.IProjector;
import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;

/**
 * Created by Alex on 11.11.2015.
 */
public class ReactionOnMainThreadDecorator implements IReaction {
    public static ReactionOnMainThreadDecorator decorate(IReaction reaction){
        ReactionOnMainThreadDecorator decorator = new ReactionOnMainThreadDecorator();
        decorator.setDecoratedInstance(reaction);
        return decorator;
    }

    private ReactionOnMainThreadDecorator(){
        mDecoratedInstance = null;
        mMainThreadHndlr = new Handler(Looper.getMainLooper());
    }

    /** Methods must return handling state, but here it posts task on a main thread. Those task
     * is supposed to be processed anyway, so we return false. Alternative behaviour- block worker
     * thread until reaction is processed on main thread, but it will slow down action handling.
     * This kind of reaction is supposed to be used to notify external app level about some
     * event, so it's ok to return false.
      * @param action
     * @return
     */
    @Override
    public boolean reactTo(final Action action) {
        checkInstance();
        mMainThreadHndlr.post(new Runnable() {
            @Override
            public void run() {
                mDecoratedInstance.reactTo(action);
            }
        });
        return false;
    }

    @Override
    public void setTargetEntity(IEntity target) {
        checkInstance();
        mDecoratedInstance.setTargetEntity(target);
    }

    /**
     * Accessors mrthods may be executed on a worker thread
     * @return
     */
    @Override
    public IEntity getTargetEntity() {
        checkInstance();
        return mDecoratedInstance.getTargetEntity();
    }

    @Override
    public boolean isSupportsAction(Action action) {
        checkInstance();
        return mDecoratedInstance.isSupportsAction(action);
    }

    public void setDecoratedInstance(IReaction instance){
        mDecoratedInstance = instance;
    }
    public IReaction getDecoratedInstance(){
        return mDecoratedInstance;
    }
    public boolean isHavingInstance(){
        return null != mDecoratedInstance;
    }
    protected void checkInstance() throws IllegalStateException{
        if (!isHavingInstance()){
            throw new IllegalStateException("Decorated instance isn't set");
        }
    }

    private IReaction mDecoratedInstance;
    private Handler mMainThreadHndlr;
}
