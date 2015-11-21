package com.alex.weatherapp.MapsFramework.Containers;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionType;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.SocketRack;
import com.alex.weatherapp.MapsFramework.EntityGeneral.Entity;
import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Created by Alex on 07.11.2015.
 */

/**
 * Top-level container, having all families of objects (Data or projection), SocketRack(Action pump).
 * Every object family has its tag (e.g. name, markerFamily, for example), so it allows to access
 * it with ease by using Map<String, EntityContainer>. Family keeper represents one aspect of
 * family- either data, or projection of those data. Data and projections are tied together, that is
 * why Community's entangleWith() method ties each family with its counterpart in other family,
 * found therein. If not found, that family is ignored. Ignoring family without counterpart not
 * critical, because these connections are used by Reacion's specific to family-level. Some data
 * or behaviour simply may not have representation on a map. For example, we need to move map from
 * one point because external program layer demands it(connection with it is done in subclass).
 * One solution to this is add family on map side, each family member of which resembles sertain
 * kind of behaviour(moving map, changing map scale, etc.)On the data side we simply add external
 * interface implementation, which translates query into Action object and gives it to the event
 * pump (ActionSocket). Event pump, in turns, tries to deliver this message to all subscribers
 * signed into it. Message receives only our target family, or may any another, expanding its
 * behaviour. Message pump not stores all subscribers in list, because there not a lot of families,
 * but we may change it in tree, where path along the tree represents levels in action
 * (action.behaviour.do_something_x, action.behaviour.dosomething_x.option_1,  etc.)
 *
 * Note about actions-reactions:
 * Each family has its wn set of reactions (Composite pattern). But all these reactions will
 * never fire, if family will not receive a corresponding message. It is up to this keeper to
 * sign families for actions. Actually, it is done during system's deployment and it does deployer.
 * Map<String, List<ActionType>> mPluggedActions all types of actions each family is signed for.
 *
 * Methods are named in terms of family-community for clearer understanding - family of entity
 * plays a certain role in commmunity.
 */

public class Community extends Entity {
    public Community(){
        init();
    }
    public void init(){
        mFamilyNames = new HashSet<>();
        mFamilies = new HashMap<>();
        mSocketRack = null;
    }

    /**
     * These Mesage-pump accessors are called during deployment only
     * @return
     */
    public SocketRack getSocketRack(){ return mSocketRack;}
    public void setSocketRAck(SocketRack rack){ mSocketRack = rack;}

    /**
     * Returns famiily known under given name, or throws of it isn't found
     * @param familyName
     * @return
     * @throws NoSuchElementException
     */
    public IEntityContainer getFamily(String familyName) throws NoSuchElementException{
        boolean isHaving = mFamilyNames.contains(familyName);
        if (!isHaving){
            throw  new NoSuchElementException("Family " + familyName + " not found in this storage");
        }
        IEntityContainer family = mFamilies.get(familyName);
        return family;
    }

    /**
     * Clones and retrives set with names of all families in this community
     * @return
     */
    public Set<String> getCommunityFamiliesNames(){
        return new HashSet<>(mFamilyNames);
    }

    /**
     * Socket rack must be installed before this method is called
     * @param family    Family to add
     * @param familyName    tag, family is known for in this container
     * @param eventsToSubscribe  events, family will be subscribed for
     * @param override what to do in case of collision. If true and famili with that name exist,
     *                 existing family will be unplugged from message pump and removed,
     */
    public void addFamily(IEntityContainer family,
                           String familyName,
                           Set<ActionType> eventsToSubscribe,
                           boolean override){
        if (mFamilyNames.contains(familyName)){
            if (override){
                IEntityContainer foundFamily = mFamilies.get(familyName);
                foundFamily.unplugFrom(getSocketRack());
                mFamilies.remove(familyName);
                mFamilyNames.remove(familyName);
            }else {
                return;
            }
        }
        mFamilyNames.add(familyName);
        mFamilies.put(familyName, family);
        family.setCommunity(this);
        SocketRack rack = getSocketRack();
        for (ActionType type: eventsToSubscribe){
            family.plugInto(rack, type);
        }
    }
    public void addFamily(IEntityContainer family,
                          String familyName,
                          Set<ActionType> eventsToSubscribe){
        addFamily(family, familyName, eventsToSubscribe, true);
    }

    /**
     * Unplugs family under given name from message pump, removes it from community and
     * returns back
     * @param familyName name of the family to be removed
     * @return removed family
     * @throws NoSuchElementException thrown if family not exist in community
     */
    public IEntityContainer removeFamily(String familyName) throws NoSuchElementException {
        IEntityContainer family = getFamily(familyName);
        family.unplugFrom(getSocketRack());
        mFamilies.remove(familyName);
        mFamilyNames.remove(familyName);
        return family;
    }

    /**
     * Searches for a family, retrives all its obligations in a community (subscribed for earlier),
     * @param familyName name of family
     * @param actions actions, family is responsible for
     * @throws if family isn't found
     */
    public void assignNewObligations(String familyName, Set<ActionType> actions)
            throws NoSuchElementException{
        IEntityContainer family = getFamily(familyName);
        Set<ActionType> existingObligations = family.getActionPlug().getActionsOfPluggedSockets();
        existingObligations.retainAll(actions);
        Set<ActionType> anew = new HashSet<>(actions);
        anew.removeAll(existingObligations);
        for (ActionType action: anew){
            family.plugInto(getSocketRack(), action);
        }
    }

    public void removeObligations(String familyName, Set<ActionType> actions)
        throws NoSuchElementException{
        IEntityContainer family = getFamily(familyName);
        Set<ActionType> existing = new HashSet<>(actions);
        existing.retainAll(family.getActionPlug().getActionsOfPluggedSockets());
        for (ActionType type : existing){
            family.getActionPlug().unplugFromRack(getSocketRack(), type);
        }
    }

    @Override
    public void entangleWith(IEntity other) {
        if (null != other && (other instanceof Community)){
            entangleWithOtherCommunity((Community) other);
        }
        if (null != other){
            super.entangleWith(other);
        }
    }

    public void entangleWithOtherCommunity(Community other){
        Set<String> commonFamilyNames = getCommunityFamiliesNames();
        commonFamilyNames.retainAll(other.getCommunityFamiliesNames());
        IEntityContainer familyFromHere;
        IEntityContainer straingerFamily;
        for (String familyName: commonFamilyNames){
            familyFromHere = getFamily(familyName);
            straingerFamily = other.getFamily(familyName);
            familyFromHere.entangleWith(straingerFamily);
        }
    }


    /** for easy access */
    private Set<String> mFamilyNames;
    private Map<String, IEntityContainer> mFamilies;
    private SocketRack mSocketRack;
}
