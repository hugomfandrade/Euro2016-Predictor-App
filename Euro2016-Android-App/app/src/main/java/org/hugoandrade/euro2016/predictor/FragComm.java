package org.hugoandrade.euro2016.predictor;

import org.hugoandrade.euro2016.predictor.common.ContextView;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.User;

public interface FragComm {

    /**
     * This interface defines the minimum API needed by any child
     * Fragment class to interact with MainActivity. It extends the
     * GenericRequiredActivityOps interface so that the Fragment can
     * report a message to the Parent activity to be displayed as
     * a SnackBar.
     */
    interface RequiredActivityOps extends RequiredActivityBaseOps {

        /**
         * Initiate the asynchronous update of the provided Predictions when the
         * user presses "Set Prediction" button in the SetPredictionsFragment.
         *
         * @param prediction to try put in cloud
         */
        void putPrediction(Prediction prediction);

        /**
         * Disable the View layer, initiate the asynchronous Predictions lookup
         * of the selected user and, once all Predictions are fetched, start new
         * activity displaying all Predictions of Matches prior to server time.
         *
         * @param user selected user.
         */
        void onUserSelected(User user);
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

    /**
     * This interface defines the minimum API provided by any child
     * Fragment, which desires to receive the list of Predictions and
     * the result of Prediction update operation in the cloud, to
     * interact with MainActivity.
     */
    interface ProvidedPredictionsFragmentOps {

        /**
         * Prediction updated in cloud. Update the old prediction.
         *
         * @param prediction The prediction to update.
         */
        void updatePrediction(Prediction prediction);

        /**
         * Re-set the adapter.
         */
        void reportNewServerTime();

        void updateFailedPrediction(Prediction prediction);
    }
}
