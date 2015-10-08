package com.alex.weatherapp.LoadingSystem;

/**
 * Created by Alex on 05.09.2015.
 */
public abstract class BoundRequestExecutorAbstract implements  IRequestExecutor {

    /*
    After creation executor must be configured for request handling. Configuration includes
    referencing to components of the Loading system that executer is bound to. For example, Retrofit
    executor is a wrapper for interace, returned by Retrofit class. Binding includes saving system reference
    and setting ready init flag.
     */


    public abstract void bindToLoadingSystem(LoadingSystem sys) throws  IllegalArgumentException;


}
