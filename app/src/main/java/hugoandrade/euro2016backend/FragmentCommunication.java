package hugoandrade.euro2016backend;

import java.util.ArrayList;
import java.util.HashMap;

import hugoandrade.euro2016backend.object.Country;
import hugoandrade.euro2016backend.object.Match;

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
