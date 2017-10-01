package org.hugoandrade.euro2016.backend;

import java.util.List;
import java.util.HashMap;

import org.hugoandrade.euro2016.backend.common.ContextView;
import org.hugoandrade.euro2016.backend.common.ModelOps;
import org.hugoandrade.euro2016.backend.common.PresenterOps;
import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.object.SystemData;

public interface MVP {
    interface RequiredViewOps extends ContextView {
        void reportMessage(String message);
        void setAllMatches(List<Match> allMatchesList);
        void setMatch(Match match);
        void setGroups(HashMap<String, List<Country>> allGroups);
    }

    interface ProvidedPresenterOps extends PresenterOps<RequiredViewOps> {
        void updateMatch(Match match);
    }

    interface RequiredPresenterOps extends ContextView {
        void notifyServiceConnectionStatus(boolean isConnected);
        void reportGetAllCountriesRequestResult(String message, List<Country> allCountriesList);
        void reportGetAllMatchesRequestResult(String message, List<Match> allMatchesList);
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
    }
    interface ProvidedEditSystemDataPresenterOps extends PresenterOps<RequiredEditSystemDataViewOps> {
        void setSystemData(SystemData systemData);
    }
    interface RequiredEditSystemDataPresenterOps extends ContextView {
        void reportGetSystemDataOperationResult(String message, SystemData systemData);
        void reportSetSystemDataOperationResult(String message, SystemData systemData);
    }
    interface ProvidedEditSystemDataModelOps extends ModelOps<RequiredEditSystemDataPresenterOps> {
        void getSystemData();
        void setSystemData(SystemData systemData);
    }
}
