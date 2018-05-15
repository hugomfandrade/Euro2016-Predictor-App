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
import org.hugoandrade.euro2016.predictor.utils.StaticVariableUtils;
import org.hugoandrade.euro2016.predictor.view.CountryDetailsActivity;
import org.hugoandrade.euro2016.predictor.view.helper.FilterWrapper;
import org.hugoandrade.euro2016.predictor.view.listadapter.PredictionListAdapter;

import java.util.ArrayList;
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

        mFilterWrapper = FilterWrapper.Builder.instance(getActivity())
                .setTheme(FilterWrapper.LIGHT)
                .setFilterText(view.findViewById(R.id.tv_filter_title))
                .setPreviousButton(view.findViewById(R.id.iv_filter_previous))
                .setNextButton(view.findViewById(R.id.iv_filter_next))
                .setListener(this)
                .create();

        rvPredictions = view.findViewById(R.id.rv_predictions);
        rvPredictions.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

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

        rvPredictions.setAdapter(mPredictionsAdapter);
        rvPredictions.scrollToPosition(getStartingItemPosition());
        ((SimpleItemAnimator) rvPredictions.getItemAnimator()).setSupportsChangeAnimations(false);
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
            onFilterSelected(mFilterWrapper.getSelectedFilter());
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

    @Override
    public void onFilterSelected(int stage) {

        List<Match> matchList = new ArrayList<>();
        int startingPosition = 0;

        switch (stage) {
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
            if (stage == 0) {
                rvPredictions.setLayoutManager(
                        new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            }
            rvPredictions.scrollToPosition(startingPosition);
        }
    }
}
