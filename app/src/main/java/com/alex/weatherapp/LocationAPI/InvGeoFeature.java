package com.alex.weatherapp.LocationAPI;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.ResultReceiver;
import android.text.TextUtils;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.Utils.Logger;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Alex on 06.12.2015.
 */
public class InvGeoFeature extends LibFeature {
    public static final String LOGGING_PREFIX = "LocationAPI: InvGeoFeature:";
    public static final String FEATURE_NAME = "'Inverse geocoding feature'";
    public static final String DATA_PLACE_COORDS = FEATURE_NAME + " place_coordinates";
    public static final String DATA_RESULTING_NAME_SUGGESTIONS = FEATURE_NAME + " name_suggestions";
    private static final int NUMBER_OF_PROBABLE_ADRESSES = 3;

    public InvGeoFeature(GoogleLibFrame frame){
        super(frame);
    }

    public interface IUserInvGeoCallback extends IUserLocationCallback{
        void onNoResultFound(LocationData place);
    }

    public static class InvGeoResultData extends LocationResultData {
        public InvGeoResultData(){
            mPlaceNameSuggestions = new ArrayList<>();
        }
        public void setSuggestions(List<String> names){
            if (null == names) return;
            mPlaceNameSuggestions.clear();
            mPlaceNameSuggestions.addAll(names);
        }
        public List<String> getNameSuggestions(){ return mPlaceNameSuggestions;}
        public LocationData getPlaceCoordinates()throws IllegalStateException {
            if (null == mPlaceLocationCoords){
                String msg = LOGGING_PREFIX +" result has no" + "place coordinates set";
                Logger.e(msg);
                throw new IllegalStateException(msg);
            }
            return mPlaceLocationCoords;
        }
        /** At a time LocationAPI returns place name calling side may already forget
         * which place it has requested name for. So we must store coordinates of a place. Instance
         * is created at a final stage of processing place coordinates should be extracted from
         * original Intent, that's why Intent must travel along with unmodified location extra.
         */
        public void setPlaceCoordinates(LocationData loc) { mPlaceLocationCoords = loc; }
        private LocationData mPlaceLocationCoords;
        private List<String> mPlaceNameSuggestions;
    }

    /**
     * For using inverse geolookup we need to know coordinates of place
     */
    public static class InvGeoRequestBuilder extends RequestBuilder {
        public static LocationData getSourcePlace(Intent intent) throws IllegalArgumentException {
            if (!intent.hasExtra(DATA_PLACE_COORDS)){
                String msg = LOGGING_PREFIX + " Intent has no place coordinates";
                Logger.e(msg);
                throw new IllegalArgumentException(msg);
            }
            return intent.getParcelableExtra(DATA_PLACE_COORDS);
        }
        public InvGeoRequestBuilder(){
            mPlaceForHandling = null;
        }
        @Override
        public Intent createRequest() {
            if (null == mPlaceForHandling){
                throw new IllegalStateException(LOGGING_PREFIX + " no source place found");
            }
            Intent intent = new Intent();
            GoogleLibFrame.setFeatureKind(intent, InvGeoFeature.FEATURE_NAME);
            intent.putExtra(DATA_PLACE_COORDS, mPlaceForHandling);
            return intent;
        }
        public void setPlaceForHandling(LocationData place){
            mPlaceForHandling = place;
        }
        private LocationData mPlaceForHandling;
    }

    public static class InvGeoServiceResultParser extends ServiceResultParser{

        @Override
        public void parseResult(Intent intent, Context context) {
            String featureName = GoogleLibFrame.getKindOfFeature(intent);
            ResultReceiver receiver = getResultReceiver();
            if (!featureName.equals(InvGeoFeature.FEATURE_NAME)){
                String msg = "Cant't parse result in intent because result type"+
                        " doesn't match ServiceResultParser type, silently aborting";
                Logger.e(msg);
                GoogleLibFrame.setErrorMessage(intent, msg);
                receiver.send(GoogleLibFrame.RESULT_ERROR, intent.getExtras());
                return;
            }
            LocationData placeCoords = null;
            try {
                placeCoords = InvGeoRequestBuilder.getSourcePlace(intent);
            }catch (IllegalArgumentException e){
                String msg = "LocationAPI: Service: Intent has no place coordinates, aborting";
                Logger.e(msg);
                GoogleLibFrame.setErrorMessage(intent, msg);
                receiver.send(GoogleLibFrame.RESULT_ERROR, intent.getExtras());
                return;
            }
            /** Now we have place coordinates and can process them */
            ArrayList<String> nameSuggestions = new ArrayList<>();

            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = null;
            String errorMessage = null;
            try {
                addresses = geocoder.getFromLocation(placeCoords.getLat(), placeCoords.getLon(),
                        NUMBER_OF_PROBABLE_ADRESSES);
            } catch (IOException exception) {
                errorMessage = LOGGING_PREFIX + exception.getMessage();
            } catch (IllegalArgumentException illegaleArgumentException){
                errorMessage = "Invalid latitude or longitude is used for place: " +
                        placeCoords.getPlaceName()+ " ("+ placeCoords.getLat() + ", " +
                        placeCoords.getLon() + ")";
            }
            if (null != errorMessage){
                GoogleLibFrame.setErrorMessage(intent, errorMessage);
                receiver.send(GoogleLibFrame.RESULT_ERROR, intent.getExtras());
                return;
            }
            if (!addresses.isEmpty()){
                for (Address address : addresses){
                    ArrayList<String> addressFragments = new ArrayList<String>();
                    for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        addressFragments.add(address.getAddressLine(i));
                    }
                    String textAdress = TextUtils.join(System.getProperty("line.separator"),
                            addressFragments);
                    nameSuggestions.add(textAdress);
                }
            }

            intent.putStringArrayListExtra(DATA_RESULTING_NAME_SUGGESTIONS, nameSuggestions);
            receiver.send(GoogleLibFrame.RESULT_OK, intent.getExtras());
        }
    }

    public class InvGeoFinalResultProcessor extends FinalResultProcessor {
        @Override
        public void processResultInFrame(int resultCode, Intent resultData) {
            if (lookForErrors(resultCode, resultData)) return;
            IUserLocationCallback callback = null;
            try {
                callback = getCompletionCallback();
            }catch (IllegalStateException e){
                return;
            }
            if (!resultData.hasExtra(DATA_RESULTING_NAME_SUGGESTIONS)){
                String notFound = "Name suggestions not found";
                Logger.e("LocationAPI: " + notFound);
                callback.onError(notFound);
                return;
            }
            List<String> nameSuggestions = resultData
                    .getStringArrayListExtra(DATA_RESULTING_NAME_SUGGESTIONS);
            LocationData placeCoordinates = resultData.getParcelableExtra(DATA_PLACE_COORDS);
            if (null != placeCoordinates){
                Logger.i("LocationAPI: inverse geo is received for place: " +
                        placeCoordinates.getPlaceName());
            }else {
                Logger.e("LocationAPI: intent has no place info");
                return;
            }
            if (nameSuggestions.isEmpty()){
                if (!(callback instanceof IUserInvGeoCallback)){
                    /** we can't tell user that result has no suggestions, report it as
                     * an error */
                    String noResultError = "No suggestions were found";
                    callback.onError(noResultError);
                }else {
                    ((IUserInvGeoCallback)callback).onNoResultFound(placeCoordinates);
                }
            }else {
                if (GoogleLibFrame.RESULT_OK != resultCode){
                    String msg = "LocationAPI: InvGeo: Unknown result code: " + resultCode;
                    Logger.e(msg);
                    callback.onError(msg);
                }
                /** OK */
                InvGeoResultData result = new InvGeoResultData();
                result.setPlaceCoordinates(placeCoordinates);
                result.setSuggestions(nameSuggestions);
                callback.onTaskCompleted(result);
            }
        }
    }


    @Override
    public RequestBuilder createRequestBuilder() {
        return new InvGeoRequestBuilder();
    }

    @Override
    public FinalResultProcessor createFinalResultProcessor() {
        return new InvGeoFinalResultProcessor();
    }

    @Override
    public ServiceResultParser createResultParser() {
        return new InvGeoServiceResultParser();
    }

    @Override
    public String getFeatureDescriptionTag() {
        return InvGeoFeature.FEATURE_NAME;
    }

    /** Service asks Geocoder for place name, we don't have to schedule any operation from
     * API in here
     * @param request
     */
    @Override
    public void processRequest(Intent request) {
        Logger.d("LocationAPI: processing request: " + GoogleLibFrame.getKindOfFeature(request));
        getFrame().bringUpService(request);
    }

    @Override
    public boolean validateAPIFeatureReady(GoogleApiClient apiClient) {
        if (!Geocoder.isPresent()){
            Logger.e("LocationAPI: Inverse geolookup: Error, Geocoder is unavailable");
            return false;
        }
        return true;
    }
}
