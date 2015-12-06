package com.alex.weatherapp.LocationAPI;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.widget.Toast;

import com.alex.weatherapp.Utils.Logger;

public class GoogleLibFrameIntentService extends IntentService {
    private static final String THREAD_NAME = "GoogleLibFrameIntentService";
    public GoogleLibFrameIntentService() {
        super(THREAD_NAME);
    }

    /**Steps:
     * The first step is in finding out what kind of feature we're dealing with.
     * The second- create concrete ServiceResultParser, capable of interpreting results specific to
     * that kind of feature.
     * The third - extract ResultReceiver, belonging to the frame.
     * The fourth- install that receiver into parser, for parsing data and sending results back to
     * the frame.
     * The fifth- Handle final result in the frame - executed by FrameResultReceiver.
     * @param intent
     */
    @Override
    protected void onHandleIntent(final Intent intent) {
        String featureTag = GoogleLibFrame.getKindOfFeature(intent);
        LibFeature.ServiceResultParser resultParser =
                GoogleLibFrame.createServiceResultParser(featureTag);
        ResultReceiver receiver = null;
        try {
            receiver = GoogleLibFrame.extractResultReceiver(intent);
        }catch (IllegalArgumentException e){
            Logger.e("GoogleLibFrameIntentService: can't extract Result receiver, aborting");
            return;
        }
        resultParser.setResultReceiver(receiver);
        resultParser.parseResult(intent, this);
    }
}
