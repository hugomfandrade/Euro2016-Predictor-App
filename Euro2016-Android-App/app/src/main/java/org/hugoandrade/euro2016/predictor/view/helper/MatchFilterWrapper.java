package org.hugoandrade.euro2016.predictor.view.helper;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;

import java.util.ArrayList;
import java.util.List;

public class MatchFilterWrapper extends FilterWrapper {

    private List<Match> mMatchList;
    private int mMaxMatchNumber = -1;

    MatchFilterWrapper(Context context) {
        super(context);
    }

    @Override
    protected List<String> buildFilter() {
        if (mMatchList == null) {
            return new ArrayList<>();
        }

        List<String> predictionFilter = new ArrayList<>();
        for (Match match : mMatchList) {
            if (mMaxMatchNumber != -1 && match.getMatchNumber() > mMaxMatchNumber) {
                continue;
            }

            predictionFilter.add(TextUtils.concat(
                    String.valueOf(match.getMatchNumber()),
                    ": ",
                    MatchUtils.getShortMatchUp(getContext(), match)).toString());

        }
        return predictionFilter;
    }

    @Override
    FilterPopup onCreatePopup(View view) {
        FilterPopup filterPopup = super.onCreatePopup(view);
        filterPopup.setMaxRows(5);
        return filterPopup;
    }

    private void setMaxMatchNumber(int maxMatchNumber) {
        if (mMaxMatchNumber != maxMatchNumber) {
            mMaxMatchNumber = maxMatchNumber;

            rebuildFilter();
        }
    }

    private void setMatchList(List<Match> matchList) {
        if (mMatchList != matchList) {
            mMatchList = matchList;

            rebuildFilter();
        }
    }

    public static class Builder extends AbstractBuilder<MatchFilterWrapper, Builder> {

        private List<Match> matchList;
        private int maxMatchNumber = -1;

        public Builder(Context context) {
            super(new MatchFilterWrapper(context));
        }

        public static Builder instance(Context context) {
            return new Builder(context);
        }

        public Builder setMatchList(List<Match> matchList) {
            this.matchList = matchList;
            return this;
        }

        public Builder setMaxMatchNumber(int maxMatchNumber) {
            this.maxMatchNumber = maxMatchNumber;
            return this;
        }

        @Override
        public MatchFilterWrapper build() {
            MatchFilterWrapper filterWrapper = super.build();
            filterWrapper.setMatchList(matchList);

            if (maxMatchNumber != -1) {
                filterWrapper.setMaxMatchNumber(maxMatchNumber);
            }
            return filterWrapper;
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
