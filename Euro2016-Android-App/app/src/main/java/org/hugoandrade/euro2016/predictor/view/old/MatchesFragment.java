package org.hugoandrade.euro2016.predictor.view.old;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hugoandrade.euro2016.predictor.FragComm;
import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.view.fragment.FragmentBase;
import org.hugoandrade.euro2016.predictor.view.listadapter.MatchListAdapter;

import java.util.List;

public class MatchesFragment extends FragmentBase<FragComm.RequiredActivityOps> {

    // Views
    private RecyclerView rvMatches;
    private MatchListAdapter mMatchesAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        GlobalData.getInstance().addOnMatchesChangedListener(mOnMatchesChangedListener);

        return inflater.inflate(R.layout.fragment_matches, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        // Store the RecyclerView that displays all Matches and set up a Vertical LinearLayoutManager.
        rvMatches = (RecyclerView) view.findViewById(R.id.rv_matches);
        rvMatches.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        // Set up adapter
        mMatchesAdapter = new MatchListAdapter(GlobalData.getInstance().getMatchList());
        rvMatches.setAdapter(mMatchesAdapter);

        // Scroll to stored position.
        rvMatches.scrollToPosition(getStartingItemPosition());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        GlobalData.getInstance().removeOnMatchesChangedListener(mOnMatchesChangedListener);
    }


    private GlobalData.OnMatchesChangedListener mOnMatchesChangedListener
            = new GlobalData.OnMatchesChangedListener() {

        @Override
        public void onMatchesChanged() {
            // Store previous List size.
            //int prevSize = mMatchList.size();

            // Set up stored List and Adapter
            //mMatchList = getAsList(matchMap);

            if (mMatchesAdapter != null) {
                mMatchesAdapter.set(GlobalData.getInstance().getMatchList());
            }

            // Scroll only if the List size has changed (ie. if it is the first initialization)
            if (rvMatches != null)// && mMatchList.size() != prevSize)
                rvMatches.scrollToPosition(getStartingItemPosition());
        }
    };

    /**
     * Get scrolling starting position, ie. the next match after
     * ServerTime. Offset by -3 so that the item is positioned
     * in the middle of View.
     *
     * @return The position of the item to scroll to.
     */
    public int getStartingItemPosition() {
        int selection = 0;
        List<Match> matchList = GlobalData.getInstance().getMatchList();
        if (matchList != null) {
            selection = 0;
            for (int i = 0; i < matchList.size(); i++) {
                if (matchList.get(i).getHomeTeamGoals() == -1 && matchList.get(i).getAwayTeamGoals() == -1) {
                    selection = i;
                    break;
                }
            }
            selection = (selection - 3) < 0? 0 : (selection - 3);
        }
        return selection;
    }
}
