package org.hugoandrade.euro2016.backend.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import org.hugoandrade.euro2016.backend.FragmentCommunication;
import org.hugoandrade.euro2016.backend.R;
import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.view.listadapter.MatchListAdapter;

public class SetResultsFragment
        extends Fragment
        implements FragmentCommunication.ProvidedSetResultsChildFragmentOps {

    @SuppressWarnings("unused")
    private static final String TAG = SetResultsFragment.class.getSimpleName();

    private FragmentCommunication.ProvidedParentActivityOps mCommChListener;

    private RecyclerView lvAllMatches;
    private MatchListAdapter mAdapter;
    private List<Match> mMatchList = new ArrayList<>();
    private int selection = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCommChListener = (FragmentCommunication.ProvidedParentActivityOps) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_set_results, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        lvAllMatches = (RecyclerView) view.findViewById(R.id.rv_all_matches);
        lvAllMatches.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        mAdapter = new MatchListAdapter(mMatchList);
        mAdapter.setOnButtonClickedListener(new MatchListAdapter.OnButtonClickedListener() {
            @Override
            public void onClick(Match match) {
                mCommChListener.updateMatch(match);
            }
        });

        lvAllMatches.setAdapter(mAdapter);
        lvAllMatches.scrollToPosition(selection);
    }

    @Override
    public void setAllMatches(List<Match> allMatchesList) {
        mMatchList.clear();
        mMatchList.addAll(allMatchesList);
        if (mAdapter != null)
            mAdapter.setAll(mMatchList);
        if (lvAllMatches != null)
            lvAllMatches.scrollToPosition(getStartingItemPosition());
    }

    @Override
    public void setMatch(Match match) {
        for (int i = 0; i < mMatchList.size(); i++)
            if (mMatchList.get(i).getMatchNumber() == match.getMatchNumber()) {
                mMatchList.set(i, match);
                break;
            }

        if (mAdapter != null)
            mAdapter.updateMatch(match);
    }

    public int getStartingItemPosition() {
        selection = 0;
        if (mMatchList != null) {
            selection = 0;
            for (int i = 0; i < mMatchList.size(); i++) {
                if (mMatchList.get(i).getHomeTeamGoals() == -1 && mMatchList.get(i).getAwayTeamGoals() == -1) {
                    selection = i;
                    break;
                }
            }
            selection = (selection - 5) < 0? 0 : (selection - 5);
        }
        return selection;
    }
}
