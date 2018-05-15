package org.hugoandrade.euro2016.predictor.view.helper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.utils.ViewUtils;
import org.hugoandrade.euro2016.predictor.view.dialog.FilterPopup;

import java.util.ArrayList;
import java.util.List;


public class FilterWrapper {

    public static final int DARK = 0;
    public static final int LIGHT = 1;

    private final Context mContext;
    private final int mDarkColor;
    private final int mWhiteColor;
    private final List<String> mPredictionFilter;
    private int currentFilter = 0;

    private TextView mFilterTextView;
    private ImageView mPreviousButton;
    private ImageView mNextButton;

    private OnFilterSelectedListener mOnFilterSelectedListener;
    private int mTheme;

    FilterWrapper(Context context) {
        mContext = context;
        mPredictionFilter = buildPredictionFilter();

        mDarkColor = mContext.getResources().getColor(R.color.colorMain);
        mWhiteColor = Color.WHITE;
    }

    private void setViews(TextView filterText, ImageView previousButton, ImageView nextButton) {
        mFilterTextView = filterText;
        mPreviousButton = previousButton;
        mNextButton = nextButton;

        if (mFilterTextView != null) {
            mFilterTextView.setText(mPredictionFilter.get(currentFilter));
            mFilterTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FilterPopup popup = new FilterPopup(v, mPredictionFilter, currentFilter);
                    popup.setOnFilterItemClickedListener(new FilterPopup.OnFilterItemClickedListener() {
                        @Override
                        public void onFilterItemClicked(int position) {
                            setupFilter(position);
                        }
                    });
                }
            });
        }
        if (mNextButton != null) {
            mNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filterNext();
                }
            });
        }

        if (mPreviousButton != null) {
            mPreviousButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filterPrevious();
                }
            });
        }
    }

    private void setListener(OnFilterSelectedListener listener) {
        mOnFilterSelectedListener = listener;
    }

    private void setTheme(int theme) {
        mTheme = theme;
        setupUI();
    }

    private void setupUI() {

        if (mFilterTextView != null) {
            mFilterTextView.setTextColor(mTheme == LIGHT ? mDarkColor : mWhiteColor);
            ((View) mFilterTextView.getParent())
                    .setBackgroundColor(mTheme == DARK ? mDarkColor : ViewUtils.setAlpha(mWhiteColor, 1709));
        }

        if (mPreviousButton != null) {
            mPreviousButton.getDrawable().setColorFilter(mTheme == LIGHT ? mDarkColor : mWhiteColor,
                    PorterDuff.Mode.SRC_ATOP);
        }

        if (mNextButton != null) {
            mNextButton.getDrawable().setColorFilter(mTheme == LIGHT ? mDarkColor : mWhiteColor,
                    PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void filterNext() {
        if ((currentFilter + 1) < mPredictionFilter.size()) {
            setupFilter(currentFilter + 1);
        }
    }

    private void filterPrevious() {
        if (currentFilter != 0) {
            setupFilter(currentFilter - 1);
        }
    }

    private void setupFilter(int position) {
        if (currentFilter != position) {
            currentFilter = position;

            setupFilterUI();

            if (mOnFilterSelectedListener != null)
                mOnFilterSelectedListener.onFilterSelected(currentFilter);
        }
    }

    private void setupFilterUI() {
        if (mPreviousButton != null) {
            mPreviousButton.getDrawable().setColorFilter(mTheme == LIGHT ? mDarkColor : mWhiteColor,
                    PorterDuff.Mode.SRC_ATOP);
        }
        if (mNextButton != null) {
            mNextButton.getDrawable().setColorFilter(mTheme == LIGHT ? mDarkColor : mWhiteColor,
                    PorterDuff.Mode.SRC_ATOP);
        }
        if (mFilterTextView != null) {
            mFilterTextView.setText(mPredictionFilter.get(currentFilter));
        }

        switch (currentFilter) {
            case 0:
                if (mPreviousButton != null) {
                    mPreviousButton.getDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
                }
                break;
            case 7:
                if (mNextButton != null) {
                    mNextButton.getDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
                }
                break;
        }
    }

    private List<String> buildPredictionFilter() {
        List<String> predictionFilter = new ArrayList<>();
        predictionFilter.add(mContext.getString(R.string.prediction_filter_all));
        predictionFilter.add(mContext.getString(R.string.prediction_matchday_1));
        predictionFilter.add(mContext.getString(R.string.prediction_matchday_2));
        predictionFilter.add(mContext.getString(R.string.prediction_matchday_3));
        predictionFilter.add(mContext.getString(R.string.prediction_round_of_16));
        predictionFilter.add(mContext.getString(R.string.prediction_quarter_finals));
        predictionFilter.add(mContext.getString(R.string.prediction_semi_finals));
        predictionFilter.add(mContext.getString(R.string.prediction_final));
        return predictionFilter;
    }

    public int getSelectedFilter() {
        return currentFilter;
    }

    public interface OnFilterSelectedListener {
        void onFilterSelected(int stage);
    }

    public static class Builder {

        private final Context mContext;
        private int mTheme;
        private TextView mFilterText;
        private ImageView mPreviousButton;
        private ImageView mNextButton;
        private OnFilterSelectedListener mOnFilterSelectedListener;

        public Builder(Context context) {
            mContext = context;
        }

        public static Builder instance(Context context) {
            return new Builder(context);
        }

        public Builder setTheme(int theme) {
            mTheme = theme;
            return this;
        }

        public Builder setFilterText(View filterText) {
            if (filterText != null && filterText instanceof TextView) {
                mFilterText = (TextView) filterText;
            }
            return this;
        }

        public Builder setPreviousButton(View previousButton) {
            if (previousButton != null && previousButton instanceof ImageView) {
                mPreviousButton = (ImageView) previousButton;
            }
            return this;
        }

        public Builder setNextButton(View nextButton) {
            if (nextButton != null && nextButton instanceof ImageView) {
                mNextButton = (ImageView) nextButton;
            }
            return this;
        }

        public Builder setListener(OnFilterSelectedListener listener) {
            mOnFilterSelectedListener = listener;
            return this;
        }

        public FilterWrapper create() {
            FilterWrapper filterWrapper = new FilterWrapper(mContext);
            filterWrapper.setViews(mFilterText, mPreviousButton, mNextButton);
            filterWrapper.setTheme(mTheme);
            filterWrapper.setListener(mOnFilterSelectedListener);
            return filterWrapper;
        }
    }
}
