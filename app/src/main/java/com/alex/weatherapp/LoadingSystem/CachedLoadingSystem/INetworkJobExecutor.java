package com.alex.weatherapp.LoadingSystem.CachedLoadingSystem;

/**
 * Created by Alex on 22.09.2015.
 */

/**
 * Enqueue new network task from 'state' and call corresponding callback on completion
 */
public interface INetworkJobExecutor {
    void enqueueNetworkJob(StateOfExecution state);
}
