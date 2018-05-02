package org.hugoandrade.euro2016.predictor.common;

import org.hugoandrade.euro2016.predictor.model.IMobileClientService;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;

import java.util.HashSet;
import java.util.Set;

public class ServiceManager {

    private final IMobileClientService mService;
    private Set<MobileServiceCallback> mCallbackSet = new HashSet<>();

    public ServiceManager(IMobileClientService service) {
        mService = service;
    }

    public IMobileClientService getService() {
        return mService;
    }

    public void subscribeServiceCallback(MobileServiceCallback callback) {
        mCallbackSet.add(callback);
    }

    public void unsubscribeServiceCallback(MobileServiceCallback callback) {
        mCallbackSet.remove(callback);
    }

    public void sendResults(MobileClientData data) {
        for (MobileServiceCallback callback : mCallbackSet) {
            callback.sendResults(data);
        }
    }

    public interface MobileServiceCallback {
        void sendResults(MobileClientData data);
    }
}
