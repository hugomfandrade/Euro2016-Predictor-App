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
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.FragComm;
import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.utils.StaticVariableUtils;
import org.hugoandrade.euro2016.predictor.view.CountryDetailsActivity;
import org.hugoandrade.euro2016.predictor.view.listadapter.PredictionListAdapter;

import java.util.ArrayList;
import java.util.List;

public class PredictionsFragment extends FragmentBase<FragComm.RequiredActivityOps>

        implements FragComm.ProvidedPredictionsFragmentOps {

    private List<String> mPredictionFilter;
    private int currentFilter = 0;

    private TextView tvFilterText;
    private RecyclerView rvPredictions;
    private PredictionListAdapter mPredictionsAdapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mPredictionFilter = buildPredictionFilter();

        GlobalData.getInstance().addOnMatchesChangedListener(mOnMatchesChangedListener);
        GlobalData.getInstance().addOnPredictionsChangedListener(mOnPredictionsChangedListener);

        return inflater.inflate(R.layout.fragment_predictions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        tvFilterText = view.findViewById(R.id.tv_filter_title);
        View ivFilterNext = view.findViewById(R.id.iv_filter_next);
        ivFilterNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterNext();
            }
        });
        View ivFilterPrevious = view.findViewById(R.id.iv_filter_previous);
        ivFilterPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterPrevious();
            }
        });


        rvPredictions = view.findViewById(R.id.rv_predictions);
        rvPredictions.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        mPredictionsAdapter = new PredictionListAdapter(GlobalData.getInstance().getMatchList(),
                                                        GlobalData.getInstance().getPredictionList(),
                                                        PredictionListAdapter.VIEW_TYPE_DISPLAY_AND_UPDATE);
        mPredictionsAdapter.setOnPredictionSetListener(new PredictionListAdapter.OnPredictionSetListener() {


            @Override
            public void onPredictionSet(Prediction prediction) {
                getParentActivity().putPrediction(prediction);

            }

            @Override
            public void onCountryClicked(Country country) {

                startActivity(CountryDetailsActivity.makeIntent(getActivity(),
                        GlobalData.getInstance().getCountry(country),
                        GlobalData.getInstance().getMatchList(country),
                        GlobalData.getInstance().getCountryList(country)));

            }
        });

        rvPredictions.setAdapter(mPredictionsAdapter);
        rvPredictions.scrollToPosition(getStartingItemPosition());
        ((SimpleItemAnimator) rvPredictions.getItemAnimator()).setSupportsChangeAnimations(false);


        setupFilter();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        GlobalData.getInstance().removeOnMatchesChangedListener(mOnMatchesChangedListener);
        GlobalData.getInstance().removeOnPredictionsChangedListener(mOnPredictionsChangedListener);
    }

    private List<String> buildPredictionFilter() {
        List<String> predictionFilter = new ArrayList<>();
        predictionFilter.add(getString(R.string.prediction_filter_all));
        predictionFilter.add(getString(R.string.prediction_matchday_1));
        predictionFilter.add(getString(R.string.prediction_matchday_2));
        predictionFilter.add(getString(R.string.prediction_matchday_3));
        predictionFilter.add(getString(R.string.prediction_round_of_16));
        predictionFilter.add(getString(R.string.prediction_quarter_finals));
        predictionFilter.add(getString(R.string.prediction_semi_finals));
        predictionFilter.add(getString(R.string.prediction_final));
        return predictionFilter;
    }

    private void setupFilter() {
        tvFilterText.setText(mPredictionFilter.get(currentFilter));
        List<Match> matchList = new ArrayList<>();
        int startingPosition = 0;
        switch (currentFilter) {
            case 0:
                matchList = GlobalData.getInstance().getMatchList();
                startingPosition = getStartingItemPosition();
                break;
            case 1:
                matchList = GlobalData.getInstance().getMatchList(StaticVariableUtils.SStage.groupStage, 1);
                break;
            case 2:
                matchList = GlobalData.getInstance().getMatchList(StaticVariableUtils.SStage.groupStage, 2);
                break;
            case 3:
                matchList = GlobalData.getInstance().getMatchList(StaticVariableUtils.SStage.groupStage, 3);
                break;
            case 4:
                matchList = GlobalData.getInstance().getMatchList(StaticVariableUtils.SStage.roundOf16);
                break;
            case 5:
                matchList = GlobalData.getInstance().getMatchList(StaticVariableUtils.SStage.quarterFinals);
                break;
            case 6:
                matchList = GlobalData.getInstance().getMatchList(StaticVariableUtils.SStage.semiFinals);
                break;
            case 7:
                matchList = GlobalData.getInstance().getMatchList(StaticVariableUtils.SStage.finals);
                break;
        }

        if (mPredictionsAdapter != null) {
            mPredictionsAdapter.setMatchList(matchList);
            mPredictionsAdapter.notifyDataSetChanged();
        }
        if (rvPredictions != null) {
            if (currentFilter == 0) {
                rvPredictions.setLayoutManager(
                        new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            }
            rvPredictions.scrollToPosition(startingPosition);
        }
    }

    private void filterNext() {
        if ((currentFilter + 1) < mPredictionFilter.size()) {
            currentFilter = currentFilter + 1;
            setupFilter();
        }
    }

    private void filterPrevious() {
        if (currentFilter != 0) {
            currentFilter = currentFilter - 1;
            setupFilter();
        }
    }

    private GlobalData.OnMatchesChangedListener mOnMatchesChangedListener
            = new GlobalData.OnMatchesChangedListener() {

        @Override
        public void onMatchesChanged() {

            setupFilter();
        }
    };

    private GlobalData.OnPredictionsChangedListener mOnPredictionsChangedListener
            = new GlobalData.OnPredictionsChangedListener() {

        @Override
        public void onPredictionsChanged() {

            setupFilter();
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
