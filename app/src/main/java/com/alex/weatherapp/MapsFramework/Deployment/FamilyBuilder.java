package com.alex.weatherapp.MapsFramework.Deployment;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionType;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.IReaction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.IReactionComposite;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections.IProjector;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ReactionComposite;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.SocketRack;
import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;
import com.alex.weatherapp.MapsFramework.EntityGeneral.DataPiece;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Alex on 10.11.2015.
 */

/**
 * Obvious implementation for builder of Family instance.
 * here it mostly repeats methods from Family class and then returns built instance
 * Alter it for building family of objects from your specific source of data.
 * Some reactions may need to broadcast user actions. To do that reaction need to know about
 * message pump (SocketRack). Override factory method customizeReaction(..) to tie reaction with
 * SocketRack
 *
 * Do not call build() explicitly, it must be done by Deployer
 */
public class FamilyBuilder {
    public FamilyBuilder(){
        mInstance = null;
        mDataPieces = null;
    }
    public boolean isBuilding(){
        return null != mInstance;
    }
    public void newInstance(String familyName){
        mInstance = new Family();
        mDataPieces = new ArrayList<>();
        mInstance.setFamilyName(familyName);
        mIsDisableProjector = false;
    }

    public void addDataPiece(DataPiece item)throws IllegalStateException{
        validate();
        mDataPieces.add(item);
    }

    public void addDataPieces(List<DataPiece> items)throws IllegalStateException {
        validate();
        mDataPieces.addAll(items);
    }
    public void setFamilyName(String familyName) throws IllegalStateException{
        validate();
        mInstance.setFamilyName(familyName);
    }
    public String getFamilyName(){
        return mInstance.getFamilyName();
    }
    /**
     * all 'addReaction' method has a very similar code, but I didn't move it to
     * standalone method.
     * @param actionType
     * @param reaction
     * @throws IllegalStateException
     */
    public void addDataReaction(ActionType actionType, IReaction reaction)
            throws IllegalStateException {
        validate();
        mInstance.getDataActions().add(actionType);
        mInstance.getDataReactions().add(reaction);
    }

    public void addDataReaction(IReaction reaction, Set<ActionType> actionTypes)
            throws IllegalStateException{
        validate();
        List<IReaction> reacts = mInstance.getDataReactions();
        Set<ActionType> actions = mInstance.getDataActions();
        reacts.add(reaction);
        actions.addAll(actionTypes);
    }

    public void addProjectionReaction(ActionType actionType, IReaction reaction)
        throws IllegalStateException {
        validate();
        mInstance.getProjectionActions().add(actionType);
        mInstance.getProjectionReactions().add(reaction);
    }

    public void addProjectionReaction(IReaction reaction, Set<ActionType> actionTypes)
            throws IllegalStateException{
        validate();
        List<IReaction> reacts = mInstance.getProjectionReactions();
        Set<ActionType> actions = mInstance.getProjectionActions();
        reacts.add(reaction);
        actions.addAll(actionTypes);
    }

    public void setProjector(IProjector projector){
        mInstance.setProjector(projector);
    }

    /** prevents projector from obtaining projectionevents, useful in dummy families
     * (action tunnel family and user adapter family )
     * @param isDisabled
     */
    public void setDisableProjector(boolean isDisabled){
        mIsDisableProjector = isDisabled;
    }
    public boolean isProjectorDisabled(){ return mIsDisableProjector;}

    public Family build(SocketRack rack){
        putAllTogether(rack);
        Family retValue = mInstance;
        mInstance = null;
        mDataPieces = null;
        return retValue;
    }

    private void validate()throws IllegalStateException {
        if (!isBuilding()){
            throw new IllegalStateException("You must call newInstance first to start building");
        }
    }

    /**
     * here we add assign reactions to data and projections, inserts data into data
     * container, save containers in projector,
     */
    protected void putAllTogether(SocketRack rack){
        IEntityContainer dataContainer = mInstance.getDataContainer();
        for (DataPiece item : mDataPieces){
            dataContainer.addEntity(item);
        }
        dataContainer.setSocketRack(rack);
        mInstance.getProjectionContainer().setSocketRack(rack);
        IProjector projector = mInstance.getProjector();
        projector.setDataContainer(dataContainer);
        projector.setProjectionContainer(mInstance.getProjectionContainer());

        IReactionComposite dataReactions = new ReactionComposite();
        List<IReaction> dataReacts = mInstance.getDataReactions();
        for (IReaction r : dataReacts){
            dataReactions.add(r);
        }
        mInstance.getDataContainer().setReactions(dataReactions);

        IReactionComposite projectReactions = new ReactionComposite();
        List<IReaction> projReacts = mInstance.getProjectionReactions();
        for (IReaction r : projReacts){
            projectReactions.add(r);
        }
        mInstance.getProjectionContainer().setReactions(projectReactions);

        for (IReaction r : dataReacts){
            customizeReaction(r, mInstance, true);
        }
        for (IReaction r : projReacts){
            customizeReaction(r, mInstance, false);
        }
    }

    /**
     * Factory method, override it for saving in your action reference to ScketRack
     * @param reaction
     * @param family
     */
    public void customizeReaction(IReaction reaction, Family family, boolean isData){

    }
    private boolean mIsDisableProjector;
    private Family mInstance;
    private List<DataPiece> mDataPieces;
}

