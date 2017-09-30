package org.hugoandrade.euro2016.backend;

import java.util.ArrayList;
import java.util.HashMap;

import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;

public interface FragmentCommunication {
    interface ProvidedGroupsChildFragmentOps {
        void setGroups(HashMap<String, ArrayList<Country>> allGroups);
    }

    interface ProvidedSetResultsChildFragmentOps {
        void setAllMatches(ArrayList<Match> allMatchesList);
        void setMatch(Match match);
    }

    interface ProvidedParentActivityOps {
        void updateMatch(Match match);
    }
}
