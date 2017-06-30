package hugoandrade.euro2016.presenter;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import hugoandrade.euro2016.FragmentCommunication;
import hugoandrade.euro2016.MVP;
import hugoandrade.euro2016.GlobalData;
import hugoandrade.euro2016.object.Country;
import hugoandrade.euro2016.common.GenericPresenter;
import hugoandrade.euro2016.object.Match;
import hugoandrade.euro2016.object.Prediction;
import hugoandrade.euro2016.object.SystemData;
import hugoandrade.euro2016.object.User;
import hugoandrade.euro2016.model.MainModel;
import hugoandrade.euro2016.utils.ISO8601;

public class MainPresenter extends GenericPresenter<MVP.RequiredMainViewOps,
                                                     MVP.RequiredMainPresenterOps,
                                                     MVP.ProvidedMainModelOps,
                                                     MainModel>
        implements MVP.ProvidedMainPresenterOps,
                   MVP.RequiredMainPresenterOps {

    /**
     * Array of all Matches of the European Championship retrieved from
     * the cloud database.
     */
    private final ArrayList<Match> allMatches = new ArrayList<>();

    /**
     * Boolean to indicate if the application data has been retrieved or not.
     */
    private boolean hasRetrievedAllSystemData  = false;
    private boolean hasRetrievedAllCountries   = false;
    private boolean hasRetrievedAllMatches     = false;
    private boolean hasRetrievedAllPredictions = false;
    private boolean hasRetrievedAllUsers       = false;

    /**
     * Stores the selected user while the his/her predictions are fetched.
     */
    private User selectedUser;

    /**
     * Hook method called when a new instance of MainPresenter is
     * created. One time initialization code goes here, e.g., storing
     * a WeakReference to the View layer and initializing the Model
     * layer.
     *
     * @param view
     *            A reference to the View layer.
     */
    @Override
    public void onCreate(MVP.RequiredMainViewOps view) {
        // Invoke the special onCreate() method in GenericPresenter,
        // passing in the MainModel class to instantiate/manage and
        // "this" to provide MainModel with this MVP.RequiredMainModelOps
        // instance.
        super.onCreate(MainModel.class, view, this);

        // Set view unavailable
        getView().updateViewAvailability(false);
    }

    /**
     * Hook method dispatched by the GenericActivity framework to
     * initialize the MainPresenter object after a runtime
     * configuration change.
     *
     * @param view         The currently active MainPresenter.View.
     */
    @Override
    public void onConfigurationChange(MVP.RequiredMainViewOps view) {
        super.onConfigurationChange(view);
    }

    /**
     * Hook method called to shutdown the Presenter layer.
     *
     * @param isChangingConfiguration
     *        True if a runtime configuration triggered the onDestroy() call.
     */
    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        getModel().onDestroy(isChangingConfiguration);
    }

    /**
     * Report the result of the GET operation of all Countries. Interact with
     * the View layer to display the List of Countries in the appropriate
     * child Fragments (Standings Fragment).
     */
    @Override
    public void onAllCountriesFetched(boolean operationResult, String message, ArrayList<Country> allCountryList) {
        if (operationResult) {
            hasRetrievedAllCountries = true;
            if (allCountryList != null) {
                ArrayList<FragmentCommunication.ProvidedAllCountriesFragmentOps> IAllCountriesFragList =
                        getView().getAllFragmentsByInterfaceType(
                                FragmentCommunication.ProvidedAllCountriesFragmentOps.class);

                for (FragmentCommunication.ProvidedAllCountriesFragmentOps IAllCountriesFrag : IAllCountriesFragList)
                    IAllCountriesFrag.setAllCountries(allCountryList);
            }

            // Enable User input if all flags are set to true (ie. all data has been retrieved)
            getView().updateViewAvailability(hasRetrievedAllCountries && hasRetrievedAllUsers &&
                    hasRetrievedAllSystemData && hasRetrievedAllMatches && hasRetrievedAllPredictions);
        }
        else {
            if (message != null)
                getView().reportMessage(message);
        }
    }

    /**
     * Report the result of the GET operation of all Users' username and score.
     * Interact with the View layer to display the List of Users in the appropriate
     * child Fragments (Users Fragment).
     */
    @Override
    public void onAllUsersFetched(boolean operationResult, String message, ArrayList<User> userList) {
        if (operationResult) {
            hasRetrievedAllUsers = true;
            if (userList != null) {
                // Sort by score
                Collections.sort(userList, new Comparator<User>() {
                    @Override
                    public int compare(User lhs, User rhs) {
                        return rhs.score - lhs.score;
                    }
                });

                // Get all fragments which implement "FragmentCommunication.ProvidedAllUsersFragmentOps"
                ArrayList<FragmentCommunication.ProvidedAllUsersFragmentOps> IAllUsersFragList =
                        getView().getAllFragmentsByInterfaceType(
                                FragmentCommunication.ProvidedAllUsersFragmentOps.class);

                // Send the list of Users to those Fragments
                for (FragmentCommunication.ProvidedAllUsersFragmentOps IAllUsersFrag : IAllUsersFragList)
                    IAllUsersFrag.setAllUsers(userList);

            }
            // Enable User input if all flags are set to true (ie. all data has been retrieved)
            getView().updateViewAvailability(hasRetrievedAllCountries && hasRetrievedAllUsers &&
                    hasRetrievedAllSystemData && hasRetrievedAllMatches && hasRetrievedAllPredictions);
        } else {
            if (message != null)
                getView().reportMessage(message);
        }
    }

    /**
     * Report the result of the GET operation of system data.
     */
    @Override
    public void onSystemDataFetched(boolean operationResult, String message, SystemData systemData) {
        if (operationResult) {
            hasRetrievedAllSystemData = true;
            if (systemData != null) {
                GlobalData.setSystemData(systemData);

                if (!systemData.appState) {
                    getView().finishApp();
                }
                else {
                    getModel().getAllMatches();
                    getModel().getAllCountries();
                    getModel().getAllUsersScores();
                }
            }

            // Enable User input if all flags are set to true (ie. all data has been retrieved)
            getView().updateViewAvailability(hasRetrievedAllCountries && hasRetrievedAllUsers &&
                    hasRetrievedAllSystemData && hasRetrievedAllMatches && hasRetrievedAllPredictions);
        } else {
            if (message != null)
                getView().reportMessage(message);
        }
    }

    /**
     * Report the result of the GET operation of all Matches. Interact with
     * the View layer to display the List of Matches in the appropriate
     * child Fragments.
     */
    @Override
    public void onAllMatchesFetched(boolean operationResult, String message, ArrayList<Match> allMatchList) {
        if (operationResult) {
            hasRetrievedAllMatches = true;

            if (allMatchList != null) {
                allMatches.clear();
                allMatches.addAll(allMatchList);
                Collections.sort(allMatches);

                ArrayList<FragmentCommunication.ProvidedAllMatchesFragmentOps> allIMatchesFragList =
                        getView().getAllFragmentsByInterfaceType(
                                FragmentCommunication.ProvidedAllMatchesFragmentOps.class);

                for (FragmentCommunication.ProvidedAllMatchesFragmentOps IAllMatchesFrag : allIMatchesFragList)
                    IAllMatchesFrag.setAllMatches(allMatchList);

                getModel().getAllPredictions(GlobalData.user.id, Match.TOTAL_MATCHES + 1);
            }
            // Enable User input if all flags are set to true (ie. all data has been retrieved)
            getView().updateViewAvailability(hasRetrievedAllCountries && hasRetrievedAllUsers &&
                    hasRetrievedAllSystemData && hasRetrievedAllMatches && hasRetrievedAllPredictions);
        } else {
            if (message != null)
                getView().reportMessage(message);
        }
    }

    /**
     * Report the result of the GET operation of all Predictions of a given user.
     * Interact with the View layer to display the List of Prediction in the appropriate
     * child Fragments.
     */
    @Override
    public void onAllPredictionsFetched(boolean operationResult, String message, String userID, ArrayList<Prediction> predictionList) {
        if (operationResult) {

            if (predictionList != null && userID  != null) {
                if (selectedUser != null && userID.equals(selectedUser.id)) {

                    getView().moveToUsersPredictionActivity(selectedUser, allMatches, predictionList);
                    selectedUser = null;
                }
                else if (userID.equals(GlobalData.user.id)) {
                    ArrayList<FragmentCommunication.ProvidedAllPredictionsFragmentOps> IAllPredictionsFragList =
                            getView().getAllFragmentsByInterfaceType(
                                    FragmentCommunication.ProvidedAllPredictionsFragmentOps.class);


                    for (FragmentCommunication.ProvidedAllPredictionsFragmentOps IAllPredictionsFrag : IAllPredictionsFragList)
                        IAllPredictionsFrag.setAllPredictions(predictionList);

                    hasRetrievedAllPredictions = true;

                    // Enable User input if all flags are set to true (ie. all data has been retrieved)
                    getView().updateViewAvailability(hasRetrievedAllCountries && hasRetrievedAllUsers &&
                            hasRetrievedAllSystemData && hasRetrievedAllMatches && hasRetrievedAllPredictions);
                }
            }
        } else {
            if (message != null)
                getView().reportMessage(message);
        }
    }

    /**
     * Report the result of the PUT operation of a given Prediction. The Prediction
     * was handled in the back-end (was either put, updated or not handled at all
     * because the window time to update that specific Prediction was closed) and
     * returns the Prediction saved.
     */
    @Override
    public void onPredictionUpdated(boolean operationResult, String message, Prediction prediction) {
        if (operationResult) {
            if (message != null && message.contains(Prediction.PastMatchDate)) {
                GlobalData.systemData.systemDate = ISO8601.toCalendar(message.split("\t")[1]);
                GlobalData.systemData.dateOfChange = Calendar.getInstance();

                ArrayList<FragmentCommunication.ProvidedAllPredictionsFragmentOps> IAllPredictionsFragList =
                        getView().getAllFragmentsByInterfaceType(
                                FragmentCommunication.ProvidedAllPredictionsFragmentOps.class);

                for (FragmentCommunication.ProvidedAllPredictionsFragmentOps IAllPredictionsFrag : IAllPredictionsFragList)
                    IAllPredictionsFrag.updatePredictionFailure(prediction.matchNo);
                for (FragmentCommunication.ProvidedAllPredictionsFragmentOps IAllPredictionsFrag : IAllPredictionsFragList)
                    IAllPredictionsFrag.reportNewServerTime();

                return;
            }
            if (prediction != null) {
                ArrayList<FragmentCommunication.ProvidedAllPredictionsFragmentOps> IAllPredictionsFragList =
                        getView().getAllFragmentsByInterfaceType(
                                FragmentCommunication.ProvidedAllPredictionsFragmentOps.class);


                for (FragmentCommunication.ProvidedAllPredictionsFragmentOps IAllPredictionsFrag : IAllPredictionsFragList)
                    IAllPredictionsFrag.updatePrediction(prediction);
            }

        } else {
            if (message != null)
                getView().reportMessage(message);
        }

    }

    /**
     * Notify the Presenter layer that AzureMobileService is connected
     * so that it can start fetching all the app data.
     */
    @Override
    public void notifyServiceConnectionStatus(boolean isConnected) {
        if (isConnected)
            getModel().getSystemData();
    }

    /**
     * Refresh all data by fetching the cloud database: SystemData, all Matches,
     * all Countries, all Predictions of the app user and all Users' scores and
     * username. Initiate by fetching SystemData when the user requests to
     * refresh data.
     */
    @Override
    public void refreshAllData() {
        // Set all flags to false
        hasRetrievedAllCountries = false;
        hasRetrievedAllUsers = false;
        hasRetrievedAllSystemData = false;
        hasRetrievedAllMatches = false;
        hasRetrievedAllPredictions = false;

        // Disable User input (ie. all flags were set to false)
        getView().updateViewAvailability(hasRetrievedAllCountries && hasRetrievedAllUsers &&
                hasRetrievedAllSystemData && hasRetrievedAllMatches && hasRetrievedAllPredictions);

        // Fetch system data
        getModel().getSystemData();
    }

    /**
     * Initiate the asynchronous update of the provided Predictions when the
     * user presses "Set Prediction" button in the SetPredictionsFragment.
     */
    @Override
    public void putPrediction(Prediction prediction){
        getModel().putPrediction(prediction);
    }

    /**
     * Disable the View layer, initiate the asynchronous Predictions lookup
     * of the selected user and, once all Predictions are fetched, start new
     * activity displaying all Predictions of Matches prior to server time.
     */
    @Override
    public void onUserSelected(User user) {
        // Store the selected user
        selectedUser = user;

        // Disable USer input
        getView().updateViewAvailability(false);

        // Fetch all his/her predictions that had been closed.
        getModel().getAllPredictions(user.id, getFirstMatchAfterSystemTime());
    }

    /**
     * @return the Match number of the first available Match whose
     * prediction can be updated.
     */
    private int getFirstMatchAfterSystemTime() {
        for (Match m : allMatches)
            if (m.dateAndTime.after(GlobalData.getServerTime().getTime()))
                return m.matchNo;
        return -1;
    }

    /**
     * Return the Activity context.
     */
    @Override
    public Context getActivityContext() {
        return getView().getActivityContext();
    }

    /**
     * Return the Application context.
     */
    @Override
    public Context getApplicationContext() {
        return getView().getApplicationContext();
    }
}
