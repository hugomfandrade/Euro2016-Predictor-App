package org.hugoandrade.euro2016.predictor;

import org.hugoandrade.euro2016.predictor.common.ContextView;
import org.hugoandrade.euro2016.predictor.common.ServiceManager;
import org.hugoandrade.euro2016.predictor.common.ServiceManagerOps;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.model.IMobileClientService;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;

public interface FragComm {

    /**
     * This interface defines the minimum API needed by any child
     * Fragment class to interact with MainActivity. It extends the
     * GenericRequiredActivityOps interface so that the Fragment can
     * report a message to the Parent activity to be displayed as
     * a SnackBar.
     */
    interface RequiredActivityOps extends RequiredActivityBaseOps {

        ServiceManager getServiceManager();

        void disableUI();

        void enableUI();
    }

    /**
     * The base interface that an Activity class that has
     * child Fragments must implement.
     */
    interface RequiredActivityBaseOps extends ContextView {
        /**
         * The child fragment sends the message to the Parent activity, MainActivity,
         * to be displayed as a SnackBar.
         *
         * @param message
         *      Message to be sent to the Parent Activity.
         */
        void showSnackBar(String message);
    }
}
