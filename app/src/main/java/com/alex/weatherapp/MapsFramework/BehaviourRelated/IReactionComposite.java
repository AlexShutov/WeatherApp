package com.alex.weatherapp.MapsFramework.BehaviourRelated;

/**
 * Created by Alex on 07.11.2015.
 */
public interface IReactionComposite extends IReaction {
    void add(IReaction reaction);
    void remove(IReaction reaction);
    void clear();

    /**
     * This method is intended for use in object, which need to act as composite reaction,
     * but is not only reaction. For example, entity container, having container-level
     * reactions
     * @return
     */
    IReactionComposite getCompositeReaction();
}
