package com.alex.weatherapp.LoadingSystem.GeolookupRequest;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Alex on 22.09.2015.
 */
public class LocationData extends GeolookupData implements Comparable, Parcelable {

    public LocationData(double lat, double lon) {
        super(lat, lon);
        setmPlaceName("");
    }
    public LocationData(double lat, double lon, String mPlaceName) {
        super(lat, lon);
        setmPlaceName(mPlaceName);
    }
    public LocationData(LocationData src){
        setLat(src.getLat());
        setLon(src.getLon());
        setmPlaceName(src.getPlaceName());
    }

    @Override
    public int compareTo(Object another) {
        LocationData l2 = (LocationData) another;
        if (getLat() < l2.getLat()) return -1;
        if (getLat() > l2.getLat()) return 1;
        if (getLon() < l2.getLon()) return -1;
        if (getLon() > l2.getLon()) return 1;
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        LocationData l2 = (LocationData) o;
        return getLat() == l2.getLat() && getLon() == l2.getLon();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        Double t = getLat();
        hash = t.hashCode();
        t = getLon();
        hash += 17 * t.hashCode();
        return hash;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(getPlaceName());
        parcel.writeDouble(getLat());
        parcel.writeDouble(getLon());
    }

    public static final Parcelable.Creator<LocationData> CREATOR =
            new Parcelable.Creator<LocationData>(){
                @Override
                public LocationData createFromParcel(Parcel source) {
                    return new LocationData(source);
                }

                @Override
                public LocationData[] newArray(int size) {
                    return new LocationData[size];
                }
            };
    private LocationData(Parcel parcel){
        setmPlaceName(parcel.readString());
        setLat(parcel.readDouble());
        setLon(parcel.readDouble());
    }

    public String getPlaceName() { return mPlaceName;}
    public void setmPlaceName(String placeName){ mPlaceName = placeName;}

    private String mPlaceName;
}
