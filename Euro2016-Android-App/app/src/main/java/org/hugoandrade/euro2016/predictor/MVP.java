package org.hugoandrade.euro2016.predictor;

import java.util.List;

import org.hugoandrade.euro2016.predictor.common.ContextView;
import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.LoginData;
import org.hugoandrade.euro2016.predictor.common.ModelOps;
import org.hugoandrade.euro2016.predictor.common.PresenterOps;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.model.IMobileClientService;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;

/**
 * Defines the interfaces for the Euro 2016 application that are
 * required and provided by the layers in the
 * Model-View-Presenter (MVP) pattern. This design ensures loose
 * coupling between the layers in the app's MVP-based architecture.
 */
public interface MVP {

    /**
     * Base View Ops that all views in the "View" layer which interact with the
     * Remote Web Service must implement
     */
    interface RequiredMobileClientViewBaseOps extends ContextView {
        /**
         * Disable UI by displaying over all layout a "Loading" progress bar
         */
        void disableUI();

        /**
         * Enable UI by dismissing the "Loading" progress bar
         */
        void enableUI();

        /**
         * Show a message, usually as a SnackBar.
         */
        void reportMessage(String message);
    }
    /**
     * Presenter Ops that the MobileClientPresenterBase in the "Presenter" layer,
     * which interacts with the Remote Web Service, implements
     */
    interface RequiredMobileClientPresenterOps extends RequiredServicePresenterBaseOps {

        /**
         * "Model" reports to the "Presenter" the data that results from the request to the Remote
         * Web Service.
         */
        void sendResults(MobileClientData data);
    }

    /**
     * Model Ops that the MobileClientModel in the "Model" layer, which interacts with the
     * Remote Web Service, implements
     */
    interface ProvidedMobileClientModelOps extends ProvidedServiceModelBaseOps<RequiredMobileClientPresenterOps> {
        IMobileClientService getService();
    }
    /**
     * Base Presenter Ops that all presenters in the "Presenter" layer which interact with the
     * Remote Web Service must implement
     */
    interface RequiredServicePresenterBaseOps extends ContextView {

        /**
         * "Model" notifies the "Presenter" that the Service is Bound
         */
        void notifyServiceIsBound();
    }
    /**
     * Base Model Ops that all models in the "Model" layer which interact with a
     * Service must implement
     */
    interface ProvidedServiceModelBaseOps<RequiredPresenterOps> extends ModelOps<RequiredPresenterOps> {

        /**
         * Tells "Model" to listen to callbacks from the Service
         */
        void registerCallback();
    }

    /** For LOGIN **/
    interface RequiredLoginViewOps extends RequiredMobileClientViewBaseOps {
        void successfulLogin();

        /**
         * Called after SystemData is fetched. Finish app when "AppState"
         * is false.
         */
        void finishApp();
    }
    interface ProvidedLoginPresenterOps extends PresenterOps<RequiredLoginViewOps> {
        void login(String username, String password);
        void notifyMovingToNextActivity();
    }

    /** For SIGN UP **/
    interface RequiredSignUpViewOps extends RequiredMobileClientViewBaseOps {
        void successfulRegister(LoginData loginData);
    }
    interface ProvidedSignUpPresenterOps extends PresenterOps<RequiredSignUpViewOps> {
        void registerUser(String username, String password, String confirmPassword);
    }

    /* ********************************************************************** */
    /* *************************** Main Activity **************************** */
    /* ********************************************************************** */
    /**
     * This interface defines the minimum API needed by the
     * MainPresenter class in the Presenter layer to interact with
     * MainActivity in the View layer.  It extends the
     * ContextView interface so the Model layer can access Context's
     * defined in the View layer.
     */
    interface RequiredMainViewOps extends RequiredMobileClientViewBaseOps {

        /**
         * Start UsersPredictionActivity after successfully fetching the list
         * of Predictions of the User selected in the UsersScoresFragment.
         *
         * @param selectedUser User selected.
         * @param matchList list of all Matches
         * @param predictionList list of Predictions of the User.
         */
        void moveToUsersPredictionActivity(User selectedUser,
                                           List<Match> matchList,
                                           List<Prediction> predictionList);

        void updatePrediction(Prediction prediction);

        void updateFailedPrediction(Prediction prediction);
    }

    /**
     * This interface defines the minimum public API provided by the
     * MainPresenter class in the Presenter layer to the MainActivity
     * in the View layer.  It extends the  PresenterOps interface,
     * which is instantiated by the MVP.RequiredMainViewOps interface
     * used to define the parameter  that's passed to the
     * onConfigurationChange() method.
     */
    interface ProvidedMainPresenterOps extends PresenterOps<RequiredMainViewOps> {
        /**
         * Refresh all data by fetching the cloud database: SystemData, all Matches,
         * all Countries, all Predictions of the app user and all Users' scores and
         * username. Initiate by fetching SystemData when the user requests to
         * refresh data, either by "Swipe Refreshing" or by pressing the syncing
         * FloatingActionButton.
         */
        void refreshAllData();

        /**
         * Initiate the asynchronous update of the provided Predictions when the
         * user presses "Set Prediction" button in the SetPredictionsFragment.
         */
        void putPrediction(Prediction prediction);

        /**
         * Disable the View layer, initiate the asynchronous Predictions lookup
         * of the selected user and, once all Predictions are fetched, start new
         * activity displaying all Predictions of Matches prior to server time.
         */
        void getPredictionsOfSelectedUser(User user);
    }

    /**
     * This interface defines the minimum API needed by the MainModel
     * class in the Model layer to interact with MainPresenter class
     * in the Presenter layer.  It extends the ContextView interface
     * so the Model layer can access Context's defined in the View
     * layer.
     */
    interface RequiredMainPresenterOps extends RequiredServicePresenterBaseOps {

        /**
         * Report the result of the GET operation of countries, matches,
         * predictions and users.
         *
         * @param isOk Boolean reporting the result of the operation.
         *            It is true if successful, otherwise false.
         * @param message Reason that operation failed.
         * @param countryList The List of Countries to display.
         * @param matchList The List of Matches to display.
         * @param predictionList The List of Predictions to display.
         * @param userList The List of Users to display.
         */
        void onInfoFetched(boolean isOk,
                           String message,
                           List<Country> countryList,
                           List<Match> matchList,
                           List<Prediction> predictionList,
                           List<User> userList);

        /**
         * Report the result of the PUT operation of a given Prediction. The Prediction
         * was handled in the back-end (was either put, updated or not handled at all
         * because the window time to update that specific Prediction was closed) and
         * returns the Prediction saved.
         *
         * @param operationResult Boolean reporting the result of the operation.
         *                        It is true if successful, otherwise false.
         * @param message Either the reason that operation failed or the reason PUT operation
         *                responded with an not-updated error.
         * @param prediction The Prediction.
         */
        void onPredictionUpdated(boolean operationResult, String message, Prediction prediction);

        /**
         * Report the result of the GET operation of predictions
         *
         * @param operationResult Boolean reporting the result of the operation.
         *                        It is true if successful, otherwise false.
         * @param message Either the reason that operation failed or the reason PUT operation
         *                responded with an not-updated error.
         * @param user The selected user.
         * @param predictionList The List of Predictions to display.
         */
        void onPredictionsFetched(boolean operationResult, String message, User user, List<Prediction> predictionList);

        void onLatestPerformanceFetched(boolean operationResult, String message, List<Prediction> predictionList);
    }

    /**
     * This interface defines the minimum public API provided by the
     * MainModel class in the Model layer to the MainPresenter class
     * in the Presenter layer. It extends the ModelOps interface,
     * which is parameterized by the MVP.RequiredMainPresenterOps
     * interface used to define the argument passed to the
     * onConfigurationChange() method.
     */
    interface ProvidedMainModelOps extends ProvidedServiceModelBaseOps<RequiredMainPresenterOps> {

        /**
         * Initiate the asynchronous Countries, Matches, Predictions and Users
         * lookup.
         *
         * @param userID id of the User
         */
        void getInfo(String userID);

        /**
         * Initiate the asynchronous update of the provided Predictions when the
         * user presses "Set Prediction" button in the SetPredictionsFragment.
         *
         * @param prediction prediction to be put or updated in the cloud.
         */
        void putPrediction(Prediction prediction);

        /**
         * Initiate the asynchronous lookup of the Predictions of the provided user.
         *
         * @param user the User
         */
        void getPredictions(User user);

        void getLatestPerformanceOfUsers(List<User> userList, int firstMatchNumber, int lastMatchNumber);
    }
}
