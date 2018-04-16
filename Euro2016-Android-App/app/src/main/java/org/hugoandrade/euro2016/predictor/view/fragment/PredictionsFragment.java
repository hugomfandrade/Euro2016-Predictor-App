package org.hugoandrade.euro2016.predictor.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.view.listadapter.PredictionListAdapter;

public class PredictionsFragment extends FragmentBase<FragComm.RequiredActivityOps>

        implements FragComm.ProvidedPredictionsFragmentOps {

    private RecyclerView rvPredictions;
    private PredictionListAdapter mPredictionsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        GlobalData.getInstance().addOnMatchesChangedListener(mOnMatchesChangedListener);
        GlobalData.getInstance().addOnPredictionsChangedListener(mOnPredictionsChangedListener);

        return inflater.inflate(R.layout.fragment_predictions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        rvPredictions = view.findViewById(R.id.rv_predictions);
        rvPredictions.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        mPredictionsAdapter = new PredictionListAdapter(GlobalData.getInstance().getMatchList(),
                                                        GlobalData.getInstance().getPredictionList(),
                                                        PredictionListAdapter.VIEW_TYPE_DISPLAY_AND_UPDATE);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        GlobalData.getInstance().removeOnMatchesChangedListener(mOnMatchesChangedListener);
        GlobalData.getInstance().removeOnPredictionsChangedListener(mOnPredictionsChangedListener);
    }

    private GlobalData.OnMatchesChangedListener mOnMatchesChangedListener
            = new GlobalData.OnMatchesChangedListener() {

        @Override
        public void onMatchesChanged() {
            //int prevSize = mMatchList.size();

            //mMatchList = setupMatchList(matchMap);

            if (mPredictionsAdapter != null) {
                mPredictionsAdapter.setMatchList(GlobalData.getInstance().getMatchList());
                mPredictionsAdapter.notifyDataSetChanged();
            }
            if (rvPredictions != null) {// && mMatchList.size() != prevSize)
                rvPredictions.scrollToPosition(getStartingItemPosition());
            }
        }
    };

    private GlobalData.OnPredictionsChangedListener mOnPredictionsChangedListener
            = new GlobalData.OnPredictionsChangedListener() {

        @Override
        public void onPredictionsChanged() {
            if (mPredictionsAdapter != null) {
                mPredictionsAdapter.setPredictionList(GlobalData.getInstance().getPredictionList());
                mPredictionsAdapter.notifyDataSetChanged();
            }
        }
    };

    /**
     * Prediction updated in cloud. Update the old prediction.
     */
    @Override
    public void updatePrediction(Prediction prediction) {
        /*boolean isUpdated = false;
        for (int i = 0; i < mPredictionList.size(); i++)
            if (mPredictionList.get(i).getMatchNumber() == prediction.getMatchNumber()) {
                mPredictionList.set(i, prediction);
                isUpdated = true;
            }

        if (!isUpdated)
            mPredictionList.add(prediction);/**/

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
            mPredictionsAdapter.reportNewServerTime(GlobalData.getInstance().getServerTime());
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
        if (GlobalData.getInstance().getMatchList() != null) {
            selection = 0;
            for (int i = 0; i < GlobalData.getInstance().getMatchList().size(); i++) {
                if (GlobalData.getInstance().getMatchList().get(i).getDateAndTime().after(GlobalData.getInstance().getServerTime().getTime())) {
                    selection = i;
                    break;
                }
            }
            //selection = (selection - 3) < 0 ? 0 : (selection - 3);
        }
        return selection;
    }
}
