package org.hugoandrade.euro2016.backend;

import java.util.HashMap;
import java.util.List;

import org.hugoandrade.euro2016.backend.common.ContextView;
import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.object.SystemData;

public interface FragmentCommunication {
    interface ProvidedGroupsChildFragmentOps {
        void setGroups(HashMap<String, List<Country>> allGroups);
    }

    interface ProvidedSetResultsChildFragmentOps {
        void setAllMatches(List<Match> allMatchesList);
        void setMatch(Match match);
    }

    interface ProvidedParentActivityOps extends ProvidedParentBaseActivityOps {
        void updateMatch(Match match);
        void popupEditSystemDataDialog();
    }

    interface ProvidedParentBaseActivityOps extends ContextView {
        void showSnackBar(String message);
    }
}
