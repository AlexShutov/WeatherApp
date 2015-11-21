package com.alex.weatherapp.MapsFramework.EntityGeneral;

/**
 * Created by Alex on 06.11.2015.
 */

/**
 * The same, as MapEntity, but this one is for data layer.
 */
public class DataPiece extends Entity {
    public DataPiece(){
        super();
        mRequiresInfividualUpdate = false;
    }
    public boolean isRequiresIndividualUpdate(){ return mRequiresInfividualUpdate;}
    public void setRequiresIndividualUpdate(boolean isIt){ mRequiresInfividualUpdate = isIt;}

    private boolean mRequiresInfividualUpdate;
}
