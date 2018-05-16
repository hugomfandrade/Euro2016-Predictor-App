package org.hugoandrade.euro2016.predictor.view.helper;

import android.content.Context;

import org.hugoandrade.euro2016.predictor.R;

import java.util.ArrayList;
import java.util.List;

public class StageFilterWrapper extends FilterWrapper {

    StageFilterWrapper(Context context) {
        super(context);
    }

    @Override
    protected List<String> buildFilter() {
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

    public static class Builder extends AbstractBuilder<StageFilterWrapper, Builder> {

        public Builder(Context context) {
            super(new StageFilterWrapper(context));
        }

        public static Builder instance(Context context) {
            return new Builder(context);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
