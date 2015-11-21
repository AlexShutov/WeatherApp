package com.alex.weatherapp.MapsFramework.Containers;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionPlug;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionType;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.IProjector;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.ProjectorActionPlug;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.ProjectorConstructionData;
import com.google.android.gms.maps.GoogleMap;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Alex on 08.11.2015.
 */
public class ProjectionsWarehouse extends Community {

    public ProjectionsWarehouse(){
        super();
        mFamilyProjectors = new TreeMap<>();
        mFamilyProjectorsPlugs = new TreeMap<>();
        mTempProjectorData = null;
    }


    public void setProjectorData(ProjectorConstructionData data){
        mTempProjectorData = data;
    }
    public ProjectorConstructionData getCurrFamilyProjector(){
        return mTempProjectorData;
    }

    /**
     * Add projector first, then allow superclass handle the rest.
     * @param family    Family to add
     * @param familyName    tag, family is known for in this container
     * @param eventsToSubscribe  events, family will be subscribed for
     * @param override what to do in case of collision. If true and famili with that name exist,
     */
    @Override
    public void addFamily(IEntityContainer family,
                          String familyName,
                          Set<ActionType> eventsToSubscribe,
                          boolean override) {
        if (null == mTempProjectorData){
            return;
        }
        boolean isHaving = mFamilyProjectors.containsKey(familyName);
        if (isHaving){
            if (override){
                mFamilyProjectors.remove(familyName);
                mFamilyProjectorsPlugs.get(familyName).unplugAll(getSocketRack());
                mFamilyProjectorsPlugs.remove(familyName);
            }else{
                return;
            }
        }
        addProjector(family, familyName);
        mTempProjectorData = null;
        super.addFamily(family, familyName, eventsToSubscribe, override);
    }

    private void addProjector(IEntityContainer family, String familyName){
        IProjector projector = mTempProjectorData.getProjector();
        projector.setDataContainer(mTempProjectorData.getDataContainer());
        projector.setProjectionContainer(mTempProjectorData.getProjectionContainer());
        ActionPlug projectorPlug = new ActionPlug(new ProjectorActionPlug(projector));
        Set<ActionType> actionTypes = mTempProjectorData.getSupportedEvents();
        for (ActionType type: actionTypes){
            projectorPlug.plugIntoRack(getSocketRack(), type);
        }
        mFamilyProjectorsPlugs.put(familyName, projectorPlug);
        mFamilyProjectors.put(familyName, projector);
    }

    /**
     * Redirect to method from this class. If not implement, superclass method would be used,
     * ignoring projector
     * @param family
     * @param familyName
     * @param eventsToSubscribe
     */
    @Override
    public void addFamily(IEntityContainer family, String familyName, Set<ActionType> eventsToSubscribe) {
        addFamily(family, familyName, eventsToSubscribe, false);
    }

    public void setMap(GoogleMap map){
        mGMap = map;
    }
    public GoogleMap getMap(){
        return mGMap;
    }

    public IProjector getFamilyProjector(String familyName){
        if (!mFamilyProjectors.containsKey(familyName)){
            throw new IllegalStateException("No projector for that family is found");
        }
        return mFamilyProjectors.get(familyName);
    }

    /**
     * Projector warehouse extends Community, which doesn't know about projectors.
     * During initialization set projector first, then add family to community,
     * projector will be saved along with family tag and pointer to  this projector cleared.
     * Family won't be added if there are no projector set
     */
    private ProjectorConstructionData mTempProjectorData;
    private Map<String, IProjector> mFamilyProjectors;
    /** every projector accepts some events (projection events), so it need a plug
     *  for connecting to source of that events
     */
    private Map<String, ActionPlug> mFamilyProjectorsPlugs;

    private GoogleMap mGMap;
}
