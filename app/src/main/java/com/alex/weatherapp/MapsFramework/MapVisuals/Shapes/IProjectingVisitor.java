package com.alex.weatherapp.MapsFramework.MapVisuals.Shapes;

import com.alex.weatherapp.MapsFramework.EntityGeneral.MapEntity;

/**
 * Created by Alex on 12.11.2015.
 */
public interface IProjectingVisitor {
    MapEntity project(CircularRegionData cd);
    MapEntity project(RectRegionData rp);

    MapEntity createEmpty(CircularRegionData cd);
    MapEntity createEmpty(RectRegionData rd);

    void removeFromMap(CircularRegionData cd);
    void removeFromMap(RectRegionData rd);
}
