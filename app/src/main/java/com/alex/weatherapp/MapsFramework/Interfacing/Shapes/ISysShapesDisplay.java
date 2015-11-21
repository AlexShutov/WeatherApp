package com.alex.weatherapp.MapsFramework.Interfacing.Shapes;

import com.alex.weatherapp.MapsFramework.Interfacing.ISysInterface;
import com.alex.weatherapp.MapsFramework.MapVisuals.Markers.PlaceData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.CircularRegionData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.RectRegionData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.ShapeData;

import java.util.List;

/**
 * Created by Alex on 14.11.2015.
 */

/**
 * Interface for weather map
 */
public interface ISysShapesDisplay extends ISysInterface {
    @Override
    IFeedbackShapes getFeedbackInterface();

    void removeAllShapes();
    /** false if not found */
    boolean removeShape(String shapeName);
    /** perhaps we want to change shape color */
    ShapeData getShape(String shapeName);

    /** Circular shape data is a map-specific, ShapeDsata is about colors */
    void addCircularArea(CircularRegionData shapeData);
    void addRectangularArea(RectRegionData shapeData);

    boolean selectShape(String shapeName, boolean moveCam);
    boolean deselectShape(String shapeName);

    void showInfoMarkers(List<PlaceData> placesDetails);
    void removeInfoMarkers();
    void addInfoMarker(PlaceData markerData);
    boolean removeInfoMarekr(PlaceData markerData);
}
