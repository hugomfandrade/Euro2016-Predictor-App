package org.hugoandrade.euro2016.predictor.view.fragment;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.common.ServiceManager;
import org.hugoandrade.euro2016.predictor.common.ServiceManagerOps;
import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.model.IMobileClientService;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;
import org.hugoandrade.euro2016.predictor.utils.ErrorMessageUtils;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;
import org.hugoandrade.euro2016.predictor.utils.StageUtils;
import org.hugoandrade.euro2016.predictor.utils.ViewUtils;
import org.hugoandrade.euro2016.predictor.view.CountryDetailsActivity;
import org.hugoandrade.euro2016.predictor.view.helper.FilterWrapper;
import org.hugoandrade.euro2016.predictor.view.helper.FilterTheme;
import org.hugoandrade.euro2016.predictor.view.helper.StageFilterWrapper;
import org.hugoandrade.euro2016.predictor.view.listadapter.PredictionListAdapter;

import java.util.List;

public class PredictionsFragment extends FragmentBase<FragComm.RequiredActivityOps>

        implements ServiceManagerOps, FilterWrapper.OnFilterSelectedListener {

    private RecyclerView rvPredictions;
    private PredictionListAdapter mPredictionsAdapter;

    private ServiceManager mServiceManager;
    private FilterWrapper mFilterWrapper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getParentActivity().getServiceManager() != null) {
            mServiceManager = getParentActivity().getServiceManager();
            mServiceManager.subscribeServiceCallback(mServiceCallback);
        }

        GlobalData.getInstance().addOnMatchesChangedListener(mOnMatchesChangedListener);
        GlobalData.getInstance().addOnPredictionsChangedListener(mOnPredictionsChangedListener);

        return inflater.inflate(R.layout.fragment_predictions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mFilterWrapper = StageFilterWrapper.Builder.instance(getActivity())
                .setTheme(FilterTheme.LIGHT)
                .setFilterText(view.findViewById(R.id.tv_filter_title))
                .setPreviousButton(view.findViewById(R.id.iv_filter_previous))
                .setNextButton(view.findViewById(R.id.iv_filter_next))
                .setListener(this)
                .build();

        mPredictionsAdapter = new PredictionListAdapter(GlobalData.getInstance().getMatchList(),
                                                        GlobalData.getInstance().getPredictionList(),
                                                        PredictionListAdapter.VIEW_TYPE_DISPLAY_AND_UPDATE);
        mPredictionsAdapter.setOnPredictionSetListener(new PredictionListAdapter.OnPredictionSetListener() {

            @Override
            public void onPredictionSet(Prediction prediction) {
                putPrediction(prediction);

            }

            @Override
            public void onCountryClicked(Country country) {
                startActivity(CountryDetailsActivity.makeIntent(getActivity(), country));
            }
        });


        rvPredictions = view.findViewById(R.id.rv_predictions);
        rvPredictions.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvPredictions.setAdapter(mPredictionsAdapter);
        ((SimpleItemAnimator) rvPredictions.getItemAnimator()).setSupportsChangeAnimations(false);

        int startingItemPosition =
                MatchUtils.getPositionOfFirstNotPlayedMatch(
                        GlobalData.getInstance().getMatchList(),
                        GlobalData.getInstance().getServerTime().getTime());
        rvPredictions.scrollToPosition(startingItemPosition);


        Match match =
                MatchUtils.getFirstMatchOfYesterday(
                        GlobalData.getInstance().getMatchList(),
                        GlobalData.getInstance().getServerTime().getTime());

        if (match == null) {
            onFilterSelected(mFilterWrapper.getSelectedFilter());
        }
        else {
            int stageNumber = StageUtils.getStageNumber(match);

            mFilterWrapper.setSelectedFilter(stageNumber);
            onFilterSelected(stageNumber);
        }
    }

    private void putPrediction(Prediction prediction) {

        if (mServiceManager == null || mServiceManager.getService() == null) {
            onPredictionUpdated(false, ErrorMessageUtils.genNotBoundMessage(), prediction);
            return;
        }

        IMobileClientService service = mServiceManager.getService();

        try {
            service.putPrediction(prediction);
        } catch (RemoteException e) {
            e.printStackTrace();
            onPredictionUpdated(false, ErrorMessageUtils.genErrorSendingMessage(), prediction);
        }
    }

    private void onPredictionUpdated(boolean operationResult, String message, Prediction prediction) {
        if (operationResult) {
            updatePrediction(prediction);
        } else {

            updateFailedPrediction(prediction);

            reportMessage(ErrorMessageUtils.handleErrorMessage(getActivity(), message));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mServiceManager != null) {
            mServiceManager.unsubscribeServiceCallback(mServiceCallback);
        }

        GlobalData.getInstance().removeOnMatchesChangedListener(mOnMatchesChangedListener);
        GlobalData.getInstance().removeOnPredictionsChangedListener(mOnPredictionsChangedListener);
    }

    private GlobalData.OnMatchesChangedListener mOnMatchesChangedListener
            = new GlobalData.OnMatchesChangedListener() {

        @Override
        public void onMatchesChanged() {

            Match match =
                    MatchUtils.getFirstMatchOfYesterday(
                            GlobalData.getInstance().getMatchList(),
                            GlobalData.getInstance().getServerTime().getTime());

            if (match == null) {
                onFilterSelected(mFilterWrapper.getSelectedFilter());
            }
            else {
                int stageNumber = StageUtils.getStageNumber(match);

                mFilterWrapper.setSelectedFilter(stageNumber);
                onFilterSelected(stageNumber);
            }
        }
    };

    private GlobalData.OnPredictionsChangedListener mOnPredictionsChangedListener
            = new GlobalData.OnPredictionsChangedListener() {

        @Override
        public void onPredictionsChanged() {

            onFilterSelected(mFilterWrapper.getSelectedFilter());

            if (mPredictionsAdapter != null) {
                mPredictionsAdapter.setPredictionList(GlobalData.getInstance().getPredictionList());
                mPredictionsAdapter.notifyDataSetChanged();
            }
        }
    };

    /**
     * Prediction updated in cloud. Update the old prediction.
     */
    public void updatePrediction(Prediction prediction) {

        GlobalData.getInstance().updatePrediction(prediction);

        if (mPredictionsAdapter != null)
            mPredictionsAdapter.updatePrediction(prediction);
    }

    /**
     * Failed to update prediction. Update the adapter accordingly.
     */
    public void updateFailedPrediction(Prediction prediction) {
        if (mPredictionsAdapter != null)
            mPredictionsAdapter.updateFailedPrediction(prediction);
    }

    @Override
    public void notifyServiceIsBound() {
        if (getParentActivity() != null) {
            mServiceManager = getParentActivity().getServiceManager();
            mServiceManager.subscribeServiceCallback(mServiceCallback);
        }
    }

    private ServiceManager.MobileServiceCallback mServiceCallback = new ServiceManager.MobileServiceCallback() {
        @Override
        public void sendResults(MobileClientData data) {
            int operationType = data.getOperationType();
            boolean isOperationSuccessful
                    = data.getOperationResult() == MobileClientData.REQUEST_RESULT_SUCCESS;

            if (operationType == MobileClientData.OperationType.PUT_PREDICTION.ordinal()) {
                onPredictionUpdated(
                        isOperationSuccessful,
                        data.getErrorMessage(),
                        data.getPrediction());
            }
        }
    };

    @Override
    public void onFilterSelected(int stage) {

        int minMatchNumber = StageUtils.getMinMatchNumber(stage);
        int maxMatchNumber = StageUtils.getMaxMatchNumber(stage);

        List<Match> matchList = GlobalData.getInstance().getMatchList(minMatchNumber, maxMatchNumber);

        int startingPosition = 0;
        //if (stage == 0) {
            startingPosition = MatchUtils.getPositionOfFirstNotPlayedMatch(
                    matchList,
                    GlobalData.getInstance().getServerTime().getTime());
            //}

        if (startingPosition != 0 && startingPosition == matchList.size()) {
            startingPosition--;
        }

        ViewUtils.showToast(getActivity(), "Starting::" + startingPosition);
        if (mPredictionsAdapter != null) {
            mPredictionsAdapter.setMatchList(matchList);
            mPredictionsAdapter.notifyDataSetChanged();
        }
        if (rvPredictions != null) {
            //if (stage == 0) {
                rvPredictions.setLayoutManager(
                        new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            //}
            rvPredictions.scrollToPosition(startingPosition);
        }
    }
}
