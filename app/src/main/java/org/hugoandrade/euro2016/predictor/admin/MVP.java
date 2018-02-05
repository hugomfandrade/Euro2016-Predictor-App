package org.hugoandrade.euro2016.predictor.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import org.hugoandrade.euro2016.predictor.admin.common.ContextView;
import org.hugoandrade.euro2016.predictor.admin.common.ModelOps;
import org.hugoandrade.euro2016.predictor.admin.common.PresenterOps;
import org.hugoandrade.euro2016.predictor.admin.object.Group;
import org.hugoandrade.euro2016.predictor.admin.object.LoginData;
import org.hugoandrade.euro2016.predictor.admin.object.Country;
import org.hugoandrade.euro2016.predictor.admin.object.Match;
import org.hugoandrade.euro2016.predictor.admin.object.SystemData;

public interface MVP {

    interface RequiredViewBaseOps extends ContextView {

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
    interface RequiredPresenterBaseOps extends ContextView {
        void notifyServiceIsBound();
    }

    interface RequiredLoginViewOps extends RequiredViewBaseOps {
        void successfulLogin();
    }
    interface ProvidedLoginPresenterOps extends PresenterOps<RequiredLoginViewOps> {
        void login(String email, String password);
    }
    interface RequiredLoginPresenterOps extends RequiredPresenterBaseOps {
        void loginRequestResult(boolean isOk, String message, LoginData loginData);

        void getSystemDataRequestResult(boolean isOk, String message, SystemData systemData);
    }
    interface ProvidedLoginModelOps extends ModelOps<RequiredLoginPresenterOps> {
        boolean login(String email, String password);

        boolean getSystemData();
    }


    interface RequiredViewOps extends RequiredViewBaseOps {
        void setMatches(List<Match> matchList);
        void setGroups(HashMap<String, Group> allGroups);
        void updateMatch(Match match);
    }

    interface ProvidedPresenterOps extends PresenterOps<RequiredViewOps> {
        void setNewMatch(Match match);

        void updateSystemData(SystemData systemData);

        void reset();
    }

    interface RequiredPresenterOps extends RequiredPresenterBaseOps {

        void getAllInfoRequestResult(boolean isRetrieved,
                                     String message,
                                     ArrayList<Country> countryList,
                                     ArrayList<Match> matchList);

        void updateCountryRequestResult(boolean isRetrieved,
                                        String message,
                                        Country country);

        void updateMatchRequestResult(boolean isRetrieved,
                                      String message,
                                      Match match);

        void updateMatchUpRequestResult(boolean isRetrieved,
                                        String message,
                                        Match match);

        void updateSystemDataRequestResult(boolean isRetrieved,
                                           String message,
                                           SystemData systemData);
    }

    interface ProvidedModelOps extends ModelOps<RequiredPresenterOps> {
        boolean getInfo();

        boolean reset();

        boolean updateMatchUp(Match match);

        boolean updateMatch(Match match);

        boolean updateCountry(Country country);

        boolean updateSystemData(SystemData systemData);
    }
}
