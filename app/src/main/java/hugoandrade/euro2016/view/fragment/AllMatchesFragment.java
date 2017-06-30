package hugoandrade.euro2016.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import hugoandrade.euro2016.FragmentCommunication;
import hugoandrade.euro2016.R;
import hugoandrade.euro2016.common.CustomRecyclerScroll;
import hugoandrade.euro2016.common.GenericFragment;
import hugoandrade.euro2016.object.Match;
import hugoandrade.euro2016.view.listadapter.MatchListAdapter;

public class AllMatchesFragment
        extends GenericFragment<FragmentCommunication.RequiredActivityOps>
        implements FragmentCommunication.ProvidedAllMatchesFragmentOps {

    // Views
    private RecyclerView lvAllMatches;
    private MatchListAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Data
    private ArrayList<Match> mMatchList = new ArrayList<>();
    private int selection = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_all_matches, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the SwipeRefreshLayout, but set it to disabled.
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_fragment_all_matches);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getParentActivity().refreshAllData();
            }
        });

        // Store the RecyclerView that displays all Matches and set up a Vertical LinearLayoutManager.
        lvAllMatches = (RecyclerView) view.findViewById(R.id.rv_all_matches);
        lvAllMatches.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        // Set up ScrollListener in order to be notified when RecyclerView is being scrolling
        // upwards or downwards. Call either showFAB or hideFAB methods of the Parent Activity,
        lvAllMatches.addOnScrollListener(new CustomRecyclerScroll() {
            @Override
            public void show() {
                getParentActivity().showFab();
            }

            @Override
            public void hide() {
                getParentActivity().hideFab();
            }
        });

        // Set up adapter
        mAdapter = new MatchListAdapter(mMatchList);
        lvAllMatches.setAdapter(mAdapter);

        // Scroll to stored position.
        lvAllMatches.scrollToPosition(selection);
    }

    /**
     * Display the List of Matches in the appropriate View
     */
    @Override
    public void setAllMatches(ArrayList<Match> allMatchesList) {
        // Store previous List size.
        int prevSize = mMatchList.size();

        // Set up stored List and Adapter
        mMatchList.clear();
        mMatchList.addAll(allMatchesList);
        if (mAdapter != null)
            mAdapter.setAllMatches(mMatchList);

        // Scroll only if the List size has changed (ie. if it is the first initialization)
        if (lvAllMatches != null && mMatchList.size() != prevSize)
            lvAllMatches.scrollToPosition(getStartingItemPosition());

        // Disable Refreshing state of SwipeRefreshLayout
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Get scrolling starting position, ie. the next match after
     * ServerTime. Offset by -3 so that the item is positioned
     * in the middle of View.
     *
     * @return The position of the item to scroll to.
     */
    public int getStartingItemPosition() {
        selection = 0;
        if (mMatchList != null) {
            selection = 0;
            for (int i = 0; i < mMatchList.size(); i++) {
                if (mMatchList.get(i).homeTeamGoals == -1 && mMatchList.get(i).awayTeamGoals == -1) {
                    selection = i;
                    break;
                }
            }
            selection = (selection - 3) < 0? 0 : (selection - 3);
        }
        return selection;
    }
}
