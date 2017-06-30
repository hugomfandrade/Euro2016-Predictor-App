package hugoandrade.euro2016;

import java.util.ArrayList;

import hugoandrade.euro2016.common.ContextView;
import hugoandrade.euro2016.object.Country;
import hugoandrade.euro2016.object.Match;
import hugoandrade.euro2016.object.Prediction;
import hugoandrade.euro2016.object.User;

public interface FragmentCommunication {

    /**
     * This interface defines the minimum API needed by any child
     * Fragment class to interact with MainActivity. It extends the
     * GenericRequiredActivityOps interface so that the Fragment can
     * report a message to the Parent activity to be displayed as
     * a SnackBar.
     */
    interface RequiredActivityOps extends GenericRequiredActivityOps {
        /**
         * Called to refresh all the app Data. In this app, it is
         * called when a refresh is triggered in the SwipeRefreshLayout
         * via the swipe gesture.
         */
        void refreshAllData();

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

        /**
         * Show FloatingActionButton when the the Scrolling View (Recycler View or
         * SwipeRefreshLayout) is scrolling upwards.
         */
        void showFab();

        /**
         * Hide FloatingActionButton when the the Scrolling View (Recycler View or
         * SwipeRefreshLayout) is scrolling downwards.
         */
        void hideFab();
    }

    /**
     * The base interface that an Activity class that has
     * child Fragments must implement.
     */
    interface GenericRequiredActivityOps extends ContextView {
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
    interface ProvidedAllMatchesFragmentOps {
        /**
         * Display the List of Matches in the appropriate View
         *
         * @param allMatchesList The List of Matches to display.
         */
        void setAllMatches(ArrayList<Match> allMatchesList);
    }

    /**
     * This interface defines the minimum API provided by any child
     * Fragment, which desires to receive the list of Countries, to
     * interact with MainActivity.
     */
    interface ProvidedAllCountriesFragmentOps {
        /**
         * Display the List of Countries in the appropriate View
         *
         * @param allCountriesList The List of Countries to display.
         */
        void setAllCountries(ArrayList<Country> allCountriesList);
    }

    /**
     * This interface defines the minimum API provided by any child
     * Fragment, which desires to receive the list of Predictions and
     * the result of Prediction update operation in the cloud, to
     * interact with MainActivity.
     */
    interface ProvidedAllPredictionsFragmentOps {
        /**
         * Display the List of Predictions in the appropriate View
         *
         * @param allPredictionsList The List of Predictions to display.
         */
        void setAllPredictions(ArrayList<Prediction> allPredictionsList);

        /**
         * Prediction updated in cloud. Update the old prediction.
         *
         * @param prediction The prediction to update.
         */
        void updatePrediction(Prediction prediction);

        /**
         * Prediction updated in cloud failed. Enable the match again.
         *
         * @param matchNo The match number to enable update.
         */
        void updatePredictionFailure(int matchNo);

        /**
         * Re-set the adapter.
         */
        void reportNewServerTime();
    }

    /**
     * This interface defines the minimum API provided by any child
     * Fragment, which desires to receive the list of Users, to
     * interact with MainActivity.
     */
    interface ProvidedAllUsersFragmentOps {

        /**
         * Display the List of Users in the appropriate View
         *
         * @param allUsersList The List of Users to display.
         */
        void setAllUsers(ArrayList<User> allUsersList);
    }
}
