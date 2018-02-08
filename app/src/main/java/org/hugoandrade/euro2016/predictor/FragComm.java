package org.hugoandrade.euro2016.predictor;

import java.util.HashMap;
import java.util.List;

import org.hugoandrade.euro2016.predictor.common.ContextView;
import org.hugoandrade.euro2016.predictor.data.Group;
import org.hugoandrade.euro2016.predictor.data.Match;
import org.hugoandrade.euro2016.predictor.data.Prediction;
import org.hugoandrade.euro2016.predictor.data.User;
import org.hugoandrade.euro2016.predictor.utils.StaticVariableUtils.*;

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
     * Fragment, which desires to receive the list of Matches, to
     * interact with MainActivity.
     */
    interface ProvidedMatchesFragmentOps {
        /**
         * Display the List of Matches in the appropriate View
         *
         * @param matchMap The List of Matches grouped by stage to display.
         */
        void setMatches(HashMap<SStage, List<Match>> matchMap);
    }

    /**
     * This interface defines the minimum API provided by any child
     * Fragment, which desires to receive the list of Countries, to
     * interact with MainActivity.
     */
    interface ProvidedCountriesFragmentOps {

        /**
         * Display the HashMap of Countries in the appropriate View
         *
         * @param groupMap The HashMap of Countries to display.
         */
        void setGroups(HashMap<SGroup, Group> groupMap);
    }

    /**
     * This interface defines the minimum API provided by any child
     * Fragment, which desires to receive the list of Predictions and
     * the result of Prediction update operation in the cloud, to
     * interact with MainActivity.
     */
    interface ProvidedPredictionsFragmentOps {
        /**
         * Display the List of Predictions in the appropriate View
         *
         * @param predictionList The List of Predictions to display.
         */
        void setPredictions(List<Prediction> predictionList);

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

    /**
     * This interface defines the minimum API provided by any child
     * Fragment, which desires to receive the list of Users, to
     * interact with MainActivity.
     */
    interface ProvidedUsersFragmentOps {

        /**
         * Display the List of Users in the appropriate View
         *
         * @param allUsersList The List of Users to display.
         */
        void setUsers(List<User> allUsersList);
    }
}
