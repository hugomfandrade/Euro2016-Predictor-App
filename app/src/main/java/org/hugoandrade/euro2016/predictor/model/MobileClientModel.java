package org.hugoandrade.euro2016.predictor.model;


import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;

public class MobileClientModel extends MobileClientModelBase<MVP.RequiredMobileClientPresenterOps>

        implements MVP.ProvidedMobileClientModelOps {

    @Override
    public IMobileClientService getService() {
        return super.getService();
    }

    @Override
    public void sendResults(MobileClientData data) {
        getPresenter().sendResults(data);
    }
}
