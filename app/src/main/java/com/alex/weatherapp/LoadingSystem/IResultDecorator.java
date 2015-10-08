package com.alex.weatherapp.LoadingSystem;

/**
 * Created by Alex on 07.09.2015.
 */

/**
 * Interface for result morphing, used in RequestExecutorDecorator
 */

public interface IResultDecorator {
    /**
     * @param response
     * @throws IllegalArgumentException is thrown when response is of wrong type,
     * checking that requires downcasts - bad design
     * @throws IllegalStateException is added from IRequestExecutor just in case
     */
    IResponse decorate(IResponse response) throws IllegalArgumentException, IllegalStateException;
};
