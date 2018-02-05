package org.hugoandrade.euro2016.predictor.admin;

import java.util.HashMap;
import java.util.List;

import org.hugoandrade.euro2016.predictor.admin.common.ContextView;
import org.hugoandrade.euro2016.predictor.admin.object.Group;
import org.hugoandrade.euro2016.predictor.admin.object.Match;

public interface FragmentCommunication {
    interface ProvidedGroupsChildFragmentOps {
        void setGroups(HashMap<String, Group> allGroups);
    }

    interface ProvidedSetResultsChildFragmentOps {
        void setMatches(List<Match> matchList);
        void updateMatch(Match match);
    }

    interface ProvidedParentActivityOps extends ProvidedParentBaseActivityOps {
        void setNewMatch(Match match);
    }

    interface ProvidedParentBaseActivityOps extends ContextView {
        void showSnackBar(String message);
    }
}
