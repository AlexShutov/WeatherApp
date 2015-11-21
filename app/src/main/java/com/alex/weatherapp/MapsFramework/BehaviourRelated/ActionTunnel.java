package com.alex.weatherapp.MapsFramework.BehaviourRelated;

/**
 * Created by Alex on 13.11.2015.
 */

import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;

/** Can be subscribed to one type of event and convert them into other type
 */
public class ActionTunnel extends Reaction {

    public static IReaction createTunnel(IActionConverter eventConverter,
                                             ActionType receivingActionType,
                                             boolean forceMainThreadExecution){
        ActionTunnel tunnel = new ActionTunnel(receivingActionType);
        tunnel.setActionConverter(eventConverter);
        IReaction result = tunnel;
        if (forceMainThreadExecution){
            result = ReactionOnMainThreadDecorator.decorate(tunnel);
        }
        return result;
    }

    public static interface IActionConverter {
        Action convert(Action action);
        ActionType getConvertedActionType();
    }

    /** use factory method instead */
    private ActionTunnel(ActionType receivingEventType){
        super(receivingEventType);
        mSupportedAction = ActionTypes.getActionType(receivingEventType.getAction());
    }


    public ActionType getReceivingActionType(){
        return mSupportedAction;
    }

    /**
     * Converter know about type of transformed action
     * @return
     */
    public ActionType getTransformingActionType(){
        return mActionConverter.getConvertedActionType();
    }

    /**
     * Action handling is done in a worker thread, here we just convert action type
     * @param orig
     * @return
     */
    @Override
    protected boolean react(Action orig) {
        Action res = mActionConverter.convert(orig);
        res.setIsTunneled(true);
        ((IEntityContainer)getTargetEntity()).getSocketRack().reactTo(res);
        return true;
    }


    @Override
    public boolean isSupportsAction(Action action) {
        return mSupportedAction.equals(action.getActionType());
    }
    protected void setActionConverter(IActionConverter c){
        mActionConverter = c;
    }

    private ActionType mSupportedAction;
    private IActionConverter mActionConverter;
}
