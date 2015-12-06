package com.alex.weatherapp.LocationAPI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.R;

/**
 * Created by Alex on 05.12.2015.
 */
public class PlayTestScreen extends Activity {

    public PlayTestScreen() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_screen_test);
        Button initiate = (Button) findViewById(R.id.idc_ps_btn_initiate);
        initiate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationData place = null;
                try {
                    place = readEnteredPlace();
                } catch (Exception e) {
                    showMsg(e.getMessage());
                    showPopup("Illegal format of coordinates, enter again");
                    return;
                }
                doWork(place);
            }
        });
        mFrame = new GoogleLibFrame(this);



            DummyFeature.DummyFeatureRequestBuilder rb =
                    (DummyFeature.DummyFeatureRequestBuilder) mFrame.createRequestBuilder(DummyFeature.FEATURE_NAME);
            rb.setMessage("Hello, i'm a message " );
            rb.setPlaceCoords(new LocationData(47.213075, 38.929782, "Taganrog"));
            Intent request = rb.createRequest();
            mFrame.processRequest(request);


    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFrame.onStart();
    }

    @Override
    protected void onStop() {
        mFrame.onStop();
        super.onStop();
    }

    public void showMsg(String msg){
        TextView tv = (TextView)findViewById(R.id.idc_ps_text_msg);
        tv.setText(msg);
    }
    public void showPopup(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    LocationData readEnteredPlace() throws IllegalArgumentException{
        EditText latField = (EditText)findViewById(R.id.idc_ps_edit_lat);
        String latText = latField.getText().toString();
        double lat = Double.valueOf(latText);
        EditText lonField = (EditText) findViewById(R.id.idc_ps_edit_lon);
        String lonText = lonField.getText().toString();
        double lon = Double.valueOf(lonText);
        return new LocationData(lat, lon, "Entered Place");
    }

    void doWork(LocationData enteredPlace){
        showPopup("Entered place: " + enteredPlace.getmPlaceName() + " " + enteredPlace.getLat() + ", " +
        enteredPlace.getLon());
    }

    private GoogleLibFrame mFrame;
}
