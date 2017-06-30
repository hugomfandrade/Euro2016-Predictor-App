package hugoandrade.euro2016backend;


import java.util.ArrayList;
import java.util.HashMap;

import hugoandrade.euro2016backend.common.ContextView;
import hugoandrade.euro2016backend.common.ModelOps;
import hugoandrade.euro2016backend.common.PresenterOps;
import hugoandrade.euro2016backend.object.Country;
import hugoandrade.euro2016backend.object.Match;
import hugoandrade.euro2016backend.object.SystemData;

public interface MVP {
    interface RequiredViewOps extends ContextView {
        void reportMessage(String message);
        void setAllMatches(ArrayList<Match> allMatchesList);
        void setMatch(Match match);
        void setGroups(HashMap<String, ArrayList<Country>> allGroups);
    }

    interface ProvidedPresenterOps extends PresenterOps<RequiredViewOps> {
        void updateMatch(Match match);
    }

    interface RequiredPresenterOps extends ContextView {
        void notifyServiceConnectionStatus(boolean isConnected);
        void reportGetAllCountriesRequestResult(String message, ArrayList<Country> allCountriesList);
        void reportGetAllMatchesRequestResult(String message, ArrayList<Match> allMatchesList);
        void reportUpdateMatchRequestResult(String message, Match match);

    }
    interface ProvidedModelOps extends ModelOps<RequiredPresenterOps> {
        void getAllCountries();
        void getAllMatches();
        void updateMatchUp(Match match);
        void updateMatch(Match match);
        void updateCountry(Country country);
    }

    /** For SIGNUP **/
    interface RequiredEditSystemDataViewOps extends ContextView {
        void reportMessage(String message);
        void reportSystemData(SystemData systemData);
        //void reportMessage(String message);
        //void updateLayoutEnableState(boolean state);
        //void successfulRegister(LoginData registerData);
    }
    interface ProvidedEditSystemDataPresenterOps extends PresenterOps<RequiredEditSystemDataViewOps> {
        void setSystemData(SystemData systemData);
        //void registerUser(String username, String password, String confirmPAssword);
    }
    interface RequiredEditSystemDataPresenterOps extends ContextView {
        void reportGetSystemDataOperationResult(String message, SystemData systemData);
        void reportSetSystemDataOperationResult(String message, SystemData systemData);
        //void reportRegisterOperationResult(String message, LoginData registerData);
    }
    interface ProvidedEditSystemDataModelOps extends ModelOps<RequiredEditSystemDataPresenterOps> {
        void getSystemData();
        void setSystemData(SystemData systemData);
        //void registerUser(LoginData registerData);
    }
}
