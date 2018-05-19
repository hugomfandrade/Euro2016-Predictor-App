package org.hugoandrade.euro2016.predictor;

import java.util.List;

import org.hugoandrade.euro2016.predictor.common.ContextView;
import org.hugoandrade.euro2016.predictor.common.ServiceManager;
import org.hugoandrade.euro2016.predictor.common.ServiceManagerOps;
import org.hugoandrade.euro2016.predictor.data.raw.LeagueUser;
import org.hugoandrade.euro2016.predictor.data.raw.LoginData;
import org.hugoandrade.euro2016.predictor.common.ModelOps;
import org.hugoandrade.euro2016.predictor.common.PresenterOps;
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
    interface RequiredServicePresenterBaseOps extends ContextView, ServiceManagerOps {

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

        boolean isServiceBound();
    }

    /** For LOGIN **/
    interface RequiredLoginViewOps extends RequiredMobileClientViewBaseOps {
        void successfulLogin();

        /**
         * Called after SystemData is fetched. Finish app when "AppState"
         * is false.
         */
        void showAppStateDisabledMessage();

        void showAppStateErrorGettingSystemDataMessage();

        void stopHoldingSplashScreenAnimation();
    }
    interface ProvidedLoginPresenterOps extends PresenterOps<RequiredLoginViewOps> {
        void login(String username, String password);
        void notifyMovingToNextActivity();

        void getSystemData();
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
    interface RequiredMainViewOps extends RequiredMobileClientViewBaseOps, ServiceManagerOps {

        void showGettingInfoErrorMessage();
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

        ServiceManager getServiceManager();

        void logout();

        void getInfo();
    }


    /** For MATCH PREDICTION **/
    interface RequiredMatchPredictionViewOps extends RequiredMobileClientViewBaseOps {
        List<LeagueUser> getUserList();

        void setMatchPredictionList(int matchNumber, List<User> userList);
    }
    interface ProvidedMatchPredictionPresenterOps extends PresenterOps<RequiredMatchPredictionViewOps> {
        void getPredictions(List<LeagueUser> userList, int matchNumber);

        void logout();
    }


    /** For MATCH PREDICTION **/
    interface RequiredLeagueDetailsViewOps extends RequiredMobileClientViewBaseOps {
        void leagueLeft();

        void updateListOfUsers(List<LeagueUser> userList);

        void updateListOfUsersByStage(int stage);

        void startUserPredictionsActivity(User user, List<Prediction> predictionList);
    }
    interface ProvidedLeagueDetailsPresenterOps extends PresenterOps<RequiredLeagueDetailsViewOps> {
        void deleteLeague(String userID, String leagueID);

        void leaveLeague(String userID, String leagueID);

        void fetchRemainingPredictions(User user);

        void logout();

        void fetchMoreUsers(String leagueID, int numberOfMembers);

        void fetchUsers(String leagueID, int stage, int minMatchNumber, int maxMatchNumber);

        void fetchMoreUsers(String leagueID, int numberOfMembers, int stage, int minMatchNumber, int maxMatchNumber);
    }
}
