package com.alex.weatherapp.MapsFramework.MapVisuals.Markers;

/**
 * Created by Alex on 06.11.2015.
 */

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.MapsFramework.EntityGeneral.DataPiece;
import com.alex.weatherapp.MapsFramework.EntityGeneral.Entity;

/**
 * Data entity, resembling point on the map. Has a coordinates, title for show and string ID
 * For consistency with the rest of the project, it uses PlaceData for coordinates and name, instead
 * of embedded type
 */

public class PlaceData extends DataPiece{
    public enum MarkerType{
        Standard,
        BitmapIcon
    }
    public PlaceData(LocationData loc, String id){
        mLocation = loc;
        mID = id;
        defaultValues();
    }
    public PlaceData(PlaceData src){
        mLocation = new LocationData(src.getLocation());
        mID = src.getID();
        setAlpha(src.getAlpha());
        setIsDraggable(src.getIsDraggable());
        setBitmapIconResource(src.getBitmapIconResourceID());
        setMarkerType(src.getMarkerType());

    }
    public PlaceData(LocationData loc){
        mLocation = loc;
        mID = "";
        defaultValues();
    }

    private void defaultValues(){
        setMarkerType(MarkerType.Standard);
        setAlpha(0.6f);
        setBitmapIconResource(com.google.android.gms.R.drawable
                .common_signin_btn_text_pressed_dark);
        setIsDraggable(false);
    }

    public void setLocation(LocationData loc){
        mLocation = loc;
    }
    public LocationData getLocation(){
        return mLocation;
    }
    public void setID(String id){
        mID = id;
    }
    public String getID(){ return mID;}

    public void setMarkerType(MarkerType type){ mMarkerType = type;}
    public MarkerType getMarkerType(){ return mMarkerType;}
    public void setBitmapIconResource(int resID){
        mMarkerBitmapResourceID = resID;
    }
    public int getBitmapIconResourceID(){ return mMarkerBitmapResourceID;}
    /**
     * Set opacity for bitmap icon
     * @param alpha
     */
    public void setAlpha(float alpha){ mAlpha = alpha;}
    public float getAlpha(){ return mAlpha;}
    public void setIsDraggable(boolean isDraggable){ mIsDraggable = isDraggable;}
    public boolean getIsDraggable(){ return mIsDraggable;}

    private String mID;
    private LocationData mLocation;
    private MarkerType mMarkerType;
    private int mMarkerBitmapResourceID;
    private float mAlpha;
    private boolean mIsDraggable;
}
