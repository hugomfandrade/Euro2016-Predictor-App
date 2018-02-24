package org.hugoandrade.euro2016.predictor.admin.view.main;

import java.util.HashMap;
import java.util.List;

import org.hugoandrade.euro2016.predictor.admin.common.ContextView;
import org.hugoandrade.euro2016.predictor.admin.data.Group;
import org.hugoandrade.euro2016.predictor.admin.data.Match;

public interface MainFragComm {

    interface ProvidedGroupsChildFragmentOps {
        void displayGroups(HashMap<String, Group> groupsMap);
    }

    interface ProvidedMatchesFragmentOps {
        void displayMatches(List<Match> matchList);
        void updateMatch(Match match);
        void updateFailedMatch(Match match);
    }

    interface ProvidedMainActivityOps extends ProvidedMainActivityBaseOps {
        void setMatch(Match match);
    }

    interface ProvidedMainActivityBaseOps extends ContextView {
        void reportMessage(String message);
    }
}
