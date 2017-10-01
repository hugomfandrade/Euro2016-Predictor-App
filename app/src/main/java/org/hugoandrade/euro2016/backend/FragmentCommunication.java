package org.hugoandrade.euro2016.backend;

import java.util.HashMap;
import java.util.List;

import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;

public interface FragmentCommunication {
    interface ProvidedGroupsChildFragmentOps {
        void setGroups(HashMap<String, List<Country>> allGroups);
    }

    interface ProvidedSetResultsChildFragmentOps {
        void setAllMatches(List<Match> allMatchesList);
        void setMatch(Match match);
    }

    interface ProvidedParentActivityOps {
        void updateMatch(Match match);
    }
}
