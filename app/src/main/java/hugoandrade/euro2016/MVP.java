package hugoandrade.euro2016;


import java.util.ArrayList;

import hugoandrade.euro2016.common.ContextView;
import hugoandrade.euro2016.object.Country;
import hugoandrade.euro2016.object.LoginData;
import hugoandrade.euro2016.common.ModelOps;
import hugoandrade.euro2016.common.PresenterOps;
import hugoandrade.euro2016.object.Match;
import hugoandrade.euro2016.object.Prediction;
import hugoandrade.euro2016.object.SystemData;
import hugoandrade.euro2016.object.User;

/**
 * Defines the interfaces for the Euro 2016 application that are
 * required and provided by the layers in the
 * Model-View-Presenter (MVP) pattern. This design ensures loose
 * coupling between the layers in the app's MVP-based architecture.
 */
public interface MVP {

    /** For LOGIN **/
    interface RequiredLoginViewOps extends ContextView {
        void reportMessage(String message);
        void updateLayoutEnableState(boolean state);
        void successfulLogin();
    }
    interface ProvidedLoginPresenterOps extends PresenterOps<RequiredLoginViewOps> {
        LoginData getLoginDataFromSharedPreferences();
        void login(String username, String password);
        void notifySuccessfulSignUp(User user);
        void notifyMovingToNextActivity();
    }
    interface RequiredLoginPresenterOps extends ContextView {
        void reportLoginOperationResult(String message, User user);
    }
    interface ProvidedLoginModelOps extends ModelOps<RequiredLoginPresenterOps> {
        void login(LoginData loginData);
        void stopService();
    }

    /** For SIGNUP **/
    interface RequiredSignUpViewOps extends ContextView {
        void reportMessage(String message);
        void updateLayoutEnableState(boolean state);
        void successfulRegister(User user);
    }
    interface ProvidedSignUpPresenterOps extends PresenterOps<RequiredSignUpViewOps> {
        void registerUser(String username, String password, String confirmPassword);
    }
    interface RequiredSignUpPresenterOps extends ContextView {
        void reportRegisterOperationResult(String message, User user);
    }
    interface ProvidedSignUpModelOps extends ModelOps<RequiredSignUpPresenterOps> {
        void registerUser(LoginData registerData);
    }

    /************************************************************************/
    /**************************** Main Activity *****************************/
    /************************************************************************/
    /**
     * This interface defines the minimum API needed by the
     * MainPresenter class in the Presenter layer to interact with
     * MainActivity in the View layer.  It extends the
     * ContextView interface so the Model layer can access Context's
     * defined in the View layer.
     */
    interface RequiredMainViewOps extends ContextView {
        /**
         * Called after SystemData is fetched. Finish app when "AppState"
         * is false.
         */
        void finishApp();

        /**
         * Set view availability by setting the visibility to either GONE or
         * VISIBLE of a view that consumes any touch event of the user, and
         * stop or start the animation of the Syncing AnimationDrawable. This
         * methods is used and set to false while fetching data from the cloud.
         *
         * @param viewAvailable Boolean indicating if view is enabled or not.
         */
        void updateViewAvailability(boolean viewAvailable);

        /**
         * Start UsersPredictionActivity after successfully fetching the list
         * of Predictions of the User selected in the UsersScoresFragment.
         *
         * @param selectedUser User selected.
         * @param matchList list of all Matches
         * @param predictionList list of Predictions of the User.
         */
        void moveToUsersPredictionActivity(User selectedUser, ArrayList<Match> matchList,
                                           ArrayList<Prediction> predictionList);

        /**
         * Get all Fragments associated with the ViewPager of MainActivity
         * that implement a given Interface.
         *
         * @param interfaceType The interface class type.
         * @return  List of Fragments that implement the specified Interface.
         */
        <T> ArrayList<T> getAllFragmentsByInterfaceType(Class<T> interfaceType);

        /**
         * Display message in the SnackBar.
         *
         * @param message Message to be shown in SnackBar.
         */
        void reportMessage(String message);
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
        void onUserSelected(User user);
    }
    /**
     * This interface defines the minimum API needed by the MainModel
     * class in the Model layer to interact with MainPresenter class
     * in the Presenter layer.  It extends the ContextView interface
     * so the Model layer can access Context's defined in the View
     * layer.
     */
    interface RequiredMainPresenterOps extends ContextView {
        /**
         * Notify the Presenter layer that AzureMobileService is connected
         * so that it can start fetching all the app data.
         *
         * @param isConnected Boolean indicating if the service is connected or not.
         */
        void notifyServiceConnectionStatus(boolean isConnected);

        /**
         * Report the result of the GET operation of system data.
         *
         * @param operationResult Boolean reporting the result of the operation.
         *                        It is true if successful, otherwise false.
         * @param message Reason that operation failed.
         * @param systemData The SystemData retrieved.
         */
        void onSystemDataFetched(boolean operationResult, String message, SystemData systemData);

        /**
         * Report the result of the GET operation of all Matches. Interact with
         * the View layer to display the List of Matches in the appropriate
         * child Fragments.
         *
         * @param operationResult Boolean reporting the result of the operation.
         *                        It is true if successful, otherwise false.
         * @param message Reason that operation failed.
         * @param allMatchList The List of Matches to display.
         */
        void onAllMatchesFetched(boolean operationResult, String message, ArrayList<Match> allMatchList);

        /**
         * Report the result of the GET operation of all Countries. Interact with
         * the View layer to display the List of Countries in the appropriate
         * child Fragments (Standings Fragment).
         *
         * @param operationResult Boolean reporting the result of the operation.
         *                        It is true if successful, otherwise false.
         * @param message Reason that operation failed.
         * @param allCountryList The List of Countries to display.
         */
        void onAllCountriesFetched(boolean operationResult, String message, ArrayList<Country> allCountryList);

        /**
         * Report the result of the GET operation of all Users' username and score.
         * Interact with the View layer to display the List of Users in the appropriate
         * child Fragments (Users Fragment).
         *
         * @param operationResult Boolean reporting the result of the operation.
         *                        It is true if successful, otherwise false.
         * @param message Reason that operation failed.
         * @param userList The List of Users to display.
         */
        void onAllUsersFetched(boolean operationResult, String message, ArrayList<User> userList);

        /**
         * Report the result of the GET operation of all Predictions of a given user.
         * Interact with the View layer to display the List of Prediction in the appropriate
         * child Fragments.
         *
         * @param operationResult Boolean reporting the result of the operation.
         *                        It is true if successful, otherwise false.
         * @param message Reason that operation failed.
         * @param userID id of the User.
         * @param predictionList The List of Predictions to display.
         */
        void onAllPredictionsFetched(boolean operationResult, String message, String userID, ArrayList<Prediction> predictionList);

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
         * @param prediction The List of Predictions to display.
         */
        void onPredictionUpdated(boolean operationResult, String message, Prediction prediction);
    }
    /**
     * This interface defines the minimum public API provided by the
     * MainModel class in the Model layer to the MainPresenter class
     * in the Presenter layer. It extends the ModelOps interface,
     * which is parameterized by the MVP.RequiredMainPresenterOps
     * interface used to define the argument passed to the
     * onConfigurationChange() method.
     */
    interface ProvidedMainModelOps extends ModelOps<RequiredMainPresenterOps> {
        /**
         * Initiate the asynchronous SystemData lookup when the AzureMobileService
         * is connected or when the user requests to refresh data, either by "Swipe
         * Refreshing" or by pressing the syncing FloatingActionButton.
         */
        void getSystemData();

        /**
         * Initiate the asynchronous Matches lookup when the SystemData is
         * successfully fetched.
         */
        void getAllMatches();

        /**
         * Initiate the asynchronous Countries lookup when the SystemData is
         * successfully fetched.
         */
        void getAllCountries();

        /**
         * Initiate the asynchronous Users' username and score lookup when
         * the SystemData is successfully fetched.
         */
        void getAllUsersScores();

        /**
         * Initiate the asynchronous Predictions lookup either when all Matches
         * are successfully fetched or when an User listed in the RecyclerView
         * of the UsersPredictionsActivity is pressed. It fetches all Predictions
         * of the selected User prior to the a given Match Number.
         *
         * @param userID id of the User
         * @param matchNo Match number. Predictions to be fetched are of Matches
         *                prior to this Match number.
         */
        void getAllPredictions(String userID, int matchNo);

        /**
         * Initiate the asynchronous update of the provided Predictions when the
         * user presses "Set Prediction" button in the SetPredictionsFragment.
         *
         * @param prediction prediction to be put or updated in the cloud.
         */
        void putPrediction(Prediction prediction);
    }
}
