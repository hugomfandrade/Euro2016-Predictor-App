package org.hugoandrade.euro2016.predictor.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hugoandrade.euro2016.predictor.FragComm;
import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.Match;
import org.hugoandrade.euro2016.predictor.data.Prediction;
import org.hugoandrade.euro2016.predictor.utils.StaticVariableUtils.SStage;
import org.hugoandrade.euro2016.predictor.view.listadapter.PredictionListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class PredictionsFragment extends FragmentBase<FragComm.RequiredActivityOps>

        implements FragComm.ProvidedMatchesFragmentOps,
                   FragComm.ProvidedPredictionsFragmentOps {

    private RecyclerView rvPredictions;
    private PredictionListAdapter mPredictionsAdapter;

    private List<Match> mMatchList = new ArrayList<>();
    private List<Prediction> mPredictionList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_predictions, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        rvPredictions = (RecyclerView) view.findViewById(R.id.rv_predictions);
        rvPredictions.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        mPredictionsAdapter = new PredictionListAdapter(mMatchList, mPredictionList, PredictionListAdapter.VIEW_TYPE_DISPLAY_AND_UPDATE);
        mPredictionsAdapter.setOnButtonClickedListener(new PredictionListAdapter.OnPredictionSetListener() {
            @Override
            public void onPredictionSet(Prediction prediction) {
                getParentActivity().putPrediction(prediction);

            }
        });

        rvPredictions.setAdapter(mPredictionsAdapter);
        rvPredictions.scrollToPosition(getStartingItemPosition());
        ((SimpleItemAnimator) rvPredictions.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    /**
     * Display the List of Matches in the appropriate View
     */
    @Override
    public void setMatches(HashMap<SStage, List<Match>> matchMap) {
        int prevSize = mMatchList.size();

        mMatchList = setupMatchList(matchMap);

        if (mPredictionsAdapter != null) {
            mPredictionsAdapter.setMatchList(mMatchList);
            mPredictionsAdapter.notifyDataSetChanged();
        }
        if (rvPredictions != null && mMatchList.size() != prevSize)
            rvPredictions.scrollToPosition(getStartingItemPosition());
    }

    /**
     * Display the List of Predictions in the appropriate View
     */
    @Override
    public void setPredictions(List<Prediction> predictionList) {
        mPredictionList = predictionList;
        if (mPredictionsAdapter != null) {
            mPredictionsAdapter.setPredictionList(mPredictionList);
            mPredictionsAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Prediction updated in cloud. Update the old prediction.
     */
    @Override
    public void updatePrediction(Prediction prediction) {
        boolean isUpdated = false;
        for (int i = 0 ; i < mPredictionList.size(); i++)
            if (mPredictionList.get(i).getMatchNumber() == prediction.getMatchNumber()) {
                mPredictionList.set(i, prediction);
                isUpdated = true;
            }

        if (!isUpdated)
            mPredictionList.add(prediction);

        if (mPredictionsAdapter != null)
            mPredictionsAdapter.updatePrediction(prediction);
    }

    /**
     * Failed to update prediction. Update the adapter accordingly.
     */
    @Override
    public void updateFailedPrediction(Prediction prediction) {
        if (mPredictionsAdapter != null)
            mPredictionsAdapter.updateFailedPrediction(prediction);
    }

    /**
     * Re-set the adapter.
     */
    @Override
    public void reportNewServerTime() {
        if (mPredictionsAdapter != null)
            mPredictionsAdapter.reportNewServerTime(GlobalData.getServerTime());
    }

    /**
     * Utility method that returns the list of matches
     *
     * @param matchMap HashMap of matches grouped by championship stage
     */
    private List<Match> setupMatchList(HashMap<SStage, List<Match>> matchMap) {
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
                if (mMatchList.get(i).getDateAndTime().after(GlobalData.getServerTime().getTime())) {
                    selection = i;
                    break;
                }
            }
            selection = (selection - 3) < 0? 0 : (selection - 3);
        }
        return selection;
    }
}
