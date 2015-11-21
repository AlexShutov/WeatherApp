package com.alex.weatherapp.MapsFramework.MapVisuals.Shapes;

import android.graphics.Color;

import com.alex.weatherapp.MapsFramework.EntityGeneral.DataPiece;

/**
 * Created by Alex on 12.11.2015.
 */

/**
 * Add info common for all shapes here
 */
public abstract class ShapeData extends DataPiece implements IProjectableShape{
    public ShapeData(){
        super();
        setDefaultValues();
    }
    protected  void setDefaultValues(){
        setFillColorNormal(Color.argb(40, 0, 255, 0));
        setFillColorSelected(Color.argb(40, 0, 0, 255));
        setStrokeColor(Color.RED);
        setStrokeNormal(2.0f);
        setStrokeSelected(6.0f);
        mIsSelected = false;
    }


    public String getShapeName(){ return mShapeName;}
    public void setShapeName(String name){ mShapeName = name;}

    public int getFillColorNormal(){ return mFillColorNormal; }
    public void setFillColorNormal(int color){ mFillColorNormal = color;}

    public int getFillColotSelected(){ return mFillColorSelected;}
    public void setFillColorSelected(int colorSelected){ mFillColorSelected = colorSelected;}

    public int getStrokeColor(){ return mColorStroke; }
    public void setStrokeColor(int strokeColor){ mColorStroke = strokeColor;}
    public float getStrokeSelected(){ return  mStrokeSelected;}
    public void setStrokeSelected(float thick){ mStrokeSelected = thick; }
    public float getStrokeNormal(){ return mStrokeNormal;}
    public void setStrokeNormal(float thick){ mStrokeNormal = thick;}
    public boolean isSelected(){ return mIsSelected;}
    public void setSelected(boolean isSelected){ mIsSelected = isSelected;}

    private String mShapeName;
    private int mFillColorNormal;
    private int mFillColorSelected;
    private int mColorStroke;
    private float mStrokeSelected;
    private float mStrokeNormal;

    private boolean mIsSelected;
}
