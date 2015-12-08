package com.alex.weatherapp.UIDetailed;

/**
 * Created by Alex on 08.10.2015.
 */
public interface IHolderBuilder {
    /**
     * Holder doesn't make any calls on its viewers, so activity may not be in resumed
     * lifecycle
     * @param linkToActivity
     */
    void setLinkToHoldingActivity(ILinkToHolderActivity linkToActivity);

    void setDisplayingMode(DisplayingMode mode);

    /**
     * call in two-frame mode, in case of mode mismatch exception is thrown.
     * @param frameLayoutID
     */
    void setForecastViewerFrameID(int frameLayoutID);

    /**
     * call in two-frame mode, in case of mode mismatch exception is thrown.
     * @param frameLayoutID
     */
    void setPlacesViewerFrameID(int frameLayoutID);

    /**
     * call in single-frame mode, in case of mode mismatch exception is thrown.
     * @param frameLayoutID
     */
    void setFrameID(int frameLayoutID);

    /**
     * @return new view holder intance
     * @throws IllegalStateException is thrown when passed parameters doesn't match
     * selected mode or mode isn't selected
     */
    ViewerAndDataHolder build() throws IllegalStateException;
}
