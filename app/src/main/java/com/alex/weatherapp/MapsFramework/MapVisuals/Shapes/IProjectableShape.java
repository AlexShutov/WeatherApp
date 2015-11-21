package com.alex.weatherapp.MapsFramework.MapVisuals.Shapes;

import com.alex.weatherapp.MapsFramework.EntityGeneral.MapEntity;

/**
 * Created by Alex on 12.11.2015.
 */
public interface IProjectableShape {
    MapEntity project(IProjectingVisitor visitor);
    MapEntity createEmptyProjection(IProjectingVisitor visitor);
    void removeFromMap(IProjectingVisitor visitor);
}
