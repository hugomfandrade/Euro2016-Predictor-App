package hugoandrade.euro2016.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import hugoandrade.euro2016.FragmentCommunication;
import hugoandrade.euro2016.R;
import hugoandrade.euro2016.GlobalData;
import hugoandrade.euro2016.common.CustomRecyclerScroll;
import hugoandrade.euro2016.common.GenericFragment;
import hugoandrade.euro2016.object.Match;
import hugoandrade.euro2016.object.Prediction;
import hugoandrade.euro2016.view.listadapter.PredictionListAdapter;

public class SetPredictionsFragment
        extends GenericFragment<FragmentCommunication.RequiredActivityOps>
        implements FragmentCommunication.ProvidedAllMatchesFragmentOps,
                   FragmentCommunication.ProvidedAllPredictionsFragmentOps {

    private RecyclerView lvAllPredictions;
    private SwipeRefreshLayout swipeRefreshLayout;

    private PredictionListAdapter mAdapter;
    private ArrayList<Match> mMatchList = new ArrayList<>();
    private ArrayList<Prediction> mPredictionList = new ArrayList<>();
    private int selection = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_set_results, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_fragment_set_results);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getParentActivity().refreshAllData();
            }
        });
        swipeRefreshLayout.setEnabled(false);
        lvAllPredictions = (RecyclerView) view.findViewById(R.id.rv_set_results);
        lvAllPredictions.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        lvAllPredictions.addOnScrollListener(new CustomRecyclerScroll() {
            @Override
            public void show() {
                getParentActivity().showFab();
            }

            @Override
            public void hide() {
                getParentActivity().hideFab();
            }
        });

        mAdapter = new PredictionListAdapter(mMatchList, mPredictionList, PredictionListAdapter.TASK_DISPLAY_AND_UPDATE);
        mAdapter.setOnButtonClickedListener(new PredictionListAdapter.OnButtonClickedListener() {
            @Override
            public void onClick(Prediction prediction) {
                getParentActivity().putPrediction(prediction);
                mAdapter.setChildViewDisabled(prediction.matchNo, true);
                mAdapter.setChildViewWaitingForResponse(prediction.matchNo, true);

            }
        });

        lvAllPredictions.setAdapter(mAdapter);
        lvAllPredictions.scrollToPosition(selection);
        ((SimpleItemAnimator) lvAllPredictions.getItemAnimator())
                .setSupportsChangeAnimations(false);
    }

    /**
     * Display the List of Matches in the appropriate View
     */
    @Override
    public void setAllMatches(ArrayList<Match> allMatchesList) {
        int prevSize = mMatchList.size();
        mMatchList.clear();
        mMatchList.addAll(allMatchesList);
        if (mAdapter != null)
            mAdapter.setAllMatches(mMatchList);
        if (lvAllPredictions != null && mMatchList.size() != prevSize)
            lvAllPredictions.scrollToPosition(getStartingItemPosition());
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Display the List of Predictions in the appropriate View
     */
    @Override
    public void setAllPredictions(ArrayList<Prediction> allPredictionsList) {
        mPredictionList.clear();
        mPredictionList.addAll(allPredictionsList);
        if (mAdapter != null)
            mAdapter.setAllPredictions(mPredictionList);
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Prediction updated in cloud. Update the old prediction.
     */
    @Override
    public void updatePrediction(Prediction prediction) {
        for (int i = 0 ; i < mPredictionList.size(); i++)
            if (mPredictionList.get(i).matchNo == prediction.matchNo) {
                mPredictionList.set(i, prediction);
                if (mAdapter != null) {
                    mAdapter.updatePrediction(prediction);
                    mAdapter.setChildViewDisabled(prediction.matchNo, false);
                    mAdapter.setChildViewWaitingForResponse(prediction.matchNo, false);
                }
                return;
            }

        mPredictionList.add(prediction);
        if (mAdapter != null) {
            mAdapter.updatePrediction(prediction);
            mAdapter.setChildViewDisabled(prediction.matchNo, false);
            mAdapter.setChildViewWaitingForResponse(prediction.matchNo, false);
        }
    }

    /**
     * Prediction updated in cloud failed. Enable the match again.
     */
    @Override
    public void updatePredictionFailure(int matchNo) {
        mAdapter.setChildViewDisabled(matchNo, false);
        mAdapter.setChildViewWaitingForResponse(matchNo, false);
    }

    /**
     * Re-set the adapter.
     */
    @Override
    public void reportNewServerTime() {
        mAdapter.reportNewServerTime(GlobalData.getServerTime());
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
                if (mMatchList.get(i).dateAndTime.after(GlobalData.getServerTime().getTime())) {
                    selection = i;
                    break;
                }
            }
            selection = (selection - 3) < 0? 0 : (selection - 3);
        }
        return selection;
    }
}
