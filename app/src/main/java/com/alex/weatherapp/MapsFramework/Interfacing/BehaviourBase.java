package com.alex.weatherapp.MapsFramework.Interfacing;

import com.alex.weatherapp.MapsFramework.Deployment.Deployer;
import com.alex.weatherapp.MapsFramework.MapFacade;

/**
 * Created by Alex on 14.11.2015.
 */

/** Knows how to setup all families of objects and their reactions specific for
 *  this map behaviour
 */
public abstract class BehaviourBase implements ISysInterface {
    public BehaviourBase(){
        mUserAdapter = null;
    }

    public void activate(MapFacade facade){
        Deployer deployer = new Deployer();
        deployer.setFacade(facade);
        /** create user side adapter and activate it */
        setUserAdapter(createUserAdapter(deployer));
        if (null != mUserAdapter){
            mUserAdapter.activate(deployer);
        }
        setupBehaviour(deployer);
        deployer.deploy();
    }

    public abstract ISysInterface getUserInterface();
    protected abstract void setupBehaviour(Deployer deployer);
    protected abstract UserAdapterBase createUserAdapter(Deployer deployer);
    /** Do something right after deployment is complete
     * @param adapter
     */
    protected abstract void onDeloymentCompletion(UserAdapterBase adapter);

    /** set user interface adapter during setup (method above)
     *
     * @param adapter instance to set
     */
    protected void setUserAdapter(UserAdapterBase adapter){
        mUserAdapter = adapter;
    }
    protected UserAdapterBase getUserAdapter(){ return mUserAdapter;}

    @Override
    public IFeedbackInterface getFeedbackInterface() {
        if (null == mUserAdapter){
            throw new IllegalStateException("Behaviour isn't active");
        }
        return mUserAdapter.getFeedbackInterface();
    }

    @Override
    public void setFeedbackInterface(IFeedbackInterface iface) {
        if (null == mUserAdapter){
            throw new IllegalStateException("Behaviour isn't active");
        }
        mUserAdapter.setFeedbackInterface(iface);
    }

    UserAdapterBase mUserAdapter;
}

