// IMobileClientService.aidl
package org.hugoandrade.euro2016.predictor.model;

// Declare any non-default types here with import statements
import org.hugoandrade.euro2016.predictor.model.IMobileClientServiceCallback;
import org.hugoandrade.euro2016.predictor.data.Prediction;
import org.hugoandrade.euro2016.predictor.data.LoginData;

interface IMobileClientService {

    void registerCallback(IMobileClientServiceCallback cb);

    void unregisterCallback(IMobileClientServiceCallback cb);

    boolean getSystemData();

    boolean login(in LoginData loginData);

    boolean signUp(in LoginData loginData);

    boolean getInfo(String userID);

    boolean putPrediction(in Prediction prediction);
}
