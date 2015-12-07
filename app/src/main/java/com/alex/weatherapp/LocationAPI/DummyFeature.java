package com.alex.weatherapp.LocationAPI;




/**
 * Created by Alex on 05.12.2015.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.widget.Toast;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.Utils.Logger;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Dummy feature, may come handy for logging
 */
public class DummyFeature extends LibFeature {
    public static final String FEATURE_NAME = "'Dummy feature'";
    protected static final String DATA_FIELD_MESSAGE = FEATURE_NAME + " Message";
    protected static final String DATA_FIELD_COORDS = FEATURE_NAME + " Coords";
    protected static final String DATA_RESPONSE_NAME = FEATURE_NAME + " Response name";

    public static class DummyResultData extends LocationResultData {
        public DummyResultData(){}
        public void setResponseName(String responseName){ this.responseName = responseName;}
        public String getResponseName(){
            return responseName;
        }
        private String responseName;
    }

    public static class DummyFeatureRequestBuilder extends RequestBuilder {
        @Override
        public Intent createRequest() {
            Intent request = new Intent();
            GoogleLibFrame.setFeatureKind(request, FEATURE_NAME);
            request.putExtra(DATA_FIELD_MESSAGE, message);
            request.putExtra(DATA_FIELD_COORDS, placeCoords);
            return request;
        }

        public void setPlaceCoords(LocationData place){ placeCoords = place;}
        public void setMessage(String msg){ message = msg; }

        public static LocationData getPlaceCoords(Intent intent){
            return intent.getParcelableExtra(DATA_FIELD_COORDS);
        }
        public static String getMessage(Intent intent){
            return intent.getStringExtra(DATA_FIELD_MESSAGE);
        }

        private LocationData placeCoords;
        private String message;
    }

    public static class DummyFeatureServiceResultParser extends ServiceResultParser {
        public DummyFeatureServiceResultParser(){}
        @Override
        public void parseResult(final Intent intent, final Context context) {
            String featureName = GoogleLibFrame.getKindOfFeature(intent);
            ResultReceiver receiver = getResultReceiver();
            if (!featureName.equals(DummyFeature.FEATURE_NAME)){
                String msg = "Cant't parse result in intent because result type"+
                        " doesn't match ServiceResultParser type, silently aborting";
                Logger.e(msg);
                GoogleLibFrame.setErrorMessage(intent, msg);
                receiver.send(GoogleLibFrame.RESULT_ERROR, intent.getExtras());
                return;
            }
            final String message = DummyFeatureRequestBuilder.getMessage(intent);
            LocationData place = DummyFeatureRequestBuilder.getPlaceCoords(intent);
            Handler h = new Handler(Looper.getMainLooper());
            h.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Message: " + message, Toast.LENGTH_SHORT).show();
                }
            });

            String response = "I'm a response from Service";
            intent.putExtra(DATA_RESPONSE_NAME, response);

            receiver.send(GoogleLibFrame.RESULT_OK, intent.getExtras());
        }
    }

    public class DummyFeatureFinalResultProcessor extends FinalResultProcessor {
        @Override
        public void processResultInFrame(int resultCode, Intent resultData) {

            if (lookForErrors(resultCode, resultData)){
                return;
            }
            String responseFromService = resultData.getStringExtra(DATA_RESPONSE_NAME);
            Logger.d("LocationAPI: DummyFeature: processing final result");

            /** use callback for informing user, if there is any */
            IUserLocationCallback completionCallback = null;
            try {
                completionCallback = getCompletionCallback();
            } catch (IllegalStateException e) {
                return;
            }
            DummyResultData res = new DummyResultData();
            res.setResponseName(responseFromService);
            completionCallback.onTaskCompleted(res);
        }
    }


    public DummyFeature(GoogleLibFrame frame){
        super(frame);
    }

    @Override
    public String getFeatureDescriptionTag() {
        return FEATURE_NAME;
    }

    @Override
    public DummyFeatureRequestBuilder createRequestBuilder() {
        DummyFeatureRequestBuilder rb = new DummyFeatureRequestBuilder();
        return rb;
    }
    @Override
    public DummyFeatureServiceResultParser createResultParser() {
        DummyFeatureServiceResultParser parser = new DummyFeatureServiceResultParser();
        return parser;
    }
    @Override
    public DummyFeatureFinalResultProcessor createFinalResultProcessor() {
        DummyFeatureFinalResultProcessor frp = new DummyFeatureFinalResultProcessor();
        return frp;
    }

    @Override
    public void processRequest(Intent request) {
        Logger.d("LocationAPI: processing request: " + GoogleLibFrame.getKindOfFeature(request));
        getFrame().bringUpService(request);
    }

    @Override
    public boolean validateAPIFeatureReady(GoogleApiClient apiClient) {
        return true;
    }
}
