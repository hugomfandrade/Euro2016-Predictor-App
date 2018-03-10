package org.hugoandrade.euro2016.predictor.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hugoandrade.euro2016.predictor.FragComm;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.Match;
import org.hugoandrade.euro2016.predictor.utils.StaticVariableUtils.SStage;
import org.hugoandrade.euro2016.predictor.view.listadapter.MatchListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MatchesFragment extends FragmentBase<FragComm.RequiredActivityOps>

        implements FragComm.ProvidedMatchesFragmentOps {

    // Views
    private RecyclerView rvMatches;
    private MatchListAdapter mAdapter;

    // Data
    private List<Match> mMatchList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_matches, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        // Store the RecyclerView that displays all Matches and set up a Vertical LinearLayoutManager.
        rvMatches = (RecyclerView) view.findViewById(R.id.rv_matches);
        rvMatches.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        // Set up adapter
        mAdapter = new MatchListAdapter(mMatchList);
        rvMatches.setAdapter(mAdapter);

        // Scroll to stored position.
        rvMatches.scrollToPosition(getStartingItemPosition());
    }

    /**
     * Display the List of Matches in the appropriate View
     */
    @Override
    public void setMatches(HashMap<SStage, List<Match>> matchMap) {
        // Store previous List size.
        int prevSize = mMatchList.size();

        // Set up stored List and Adapter
        mMatchList = getAsList(matchMap);

        if (mAdapter != null)
            mAdapter.set(mMatchList);

        // Scroll only if the List size has changed (ie. if it is the first initialization)
        if (rvMatches != null && mMatchList.size() != prevSize)
            rvMatches.scrollToPosition(getStartingItemPosition());
    }

    private List<Match> getAsList(HashMap<SStage, List<Match>> matchMap) {
        List<Match> matchList = new ArrayList<>();

        for (List<Match> matches : matchMap.values())
            matchList.addAll(matches);

        Collections.sort(matchList, new Comparator<Match>() {
            @Override
            public int compare(Match lhs, Match rhs) {
                return lhs.getMatchNumber() - rhs.getMatchNumber();
            }
        });
        return matchList;
    }

    /**
     * Get scrolling starting position, ie. the next match after
     * ServerTime. Offset by -3 so that the item is positioned
     * in the middle of View.
     *
     * @return The position of the item to scroll to.
     */
    public int getStartingItemPosition() {
        int selection = 0;
        if (mMatchList != null) {
            selection = 0;
            for (int i = 0; i < mMatchList.size(); i++) {
                if (mMatchList.get(i).getHomeTeamGoals() == -1 && mMatchList.get(i).getAwayTeamGoals() == -1) {
                    selection = i;
                    break;
                }
            }
            selection = (selection - 3) < 0? 0 : (selection - 3);
        }
        return selection;
    }
}
