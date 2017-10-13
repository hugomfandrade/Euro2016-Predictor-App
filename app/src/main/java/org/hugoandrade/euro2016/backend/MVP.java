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

        void onLoadingUpdate(boolean hasCompletelyLoaded);
        void reportMessage(String message);
        void setAllMatches(List<Match> allMatchesList);
        void setMatch(Match match);
        void setGroups(HashMap<String, List<Country>> allGroups);
    }

    interface ProvidedPresenterOps extends PresenterOps<RequiredViewOps> {
        void updateMatch(Match match);
        SystemData getSystemData();
        void setSystemData(SystemData systemData);
    }

    interface RequiredPresenterOps extends ContextView {
        void notifyServiceConnectionStatus(boolean isConnected);
        void reportGetAllCountriesRequestResult(boolean isRetrieved, String message, List<Country> allCountriesList);
        void reportGetAllMatchesRequestResult(boolean isRetrieved, String message, List<Match> allMatchesList);
        void reportUpdateMatchRequestResult(boolean isRetrieved, String message, Match match);
        void reportSystemDataRequestResult(boolean isRetrieved, String message, SystemData systemData);
    }
    interface ProvidedModelOps extends ModelOps<RequiredPresenterOps> {
        void getAllCountries();
        void getAllMatches();
        void getSystemData();
        void updateMatchUp(Match match);
        void updateMatch(Match match);
        void updateCountry(Country country);
        void setSystemData(SystemData systemData);
    }
}
