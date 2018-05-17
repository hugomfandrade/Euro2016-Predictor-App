package org.hugoandrade.euro2016.predictor.view.helper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class FilterWrapper {

    private final Context mContext;
    private final int mDarkColor;
    private final int mWhiteColor;
    private List<String> mPredictionFilter;

    private int mMinFilter = -1;
    private int mMaxFilter = -1;
    private int currentFilter = 0;

    private TextView mFilterTextView;
    private ImageView mPreviousButton;
    private ImageView mNextButton;

    private OnFilterSelectedListener mOnFilterSelectedListener;
    private int mTheme;
    private boolean isHoldEnabled;

    FilterWrapper(Context context) {
        mContext = context;
        mPredictionFilter = buildFilter();

        mDarkColor = mContext.getResources().getColor(R.color.colorMain);
        mWhiteColor = Color.WHITE;
    }

    void setViews(TextView filterText, ImageView previousButton, ImageView nextButton) {
        mFilterTextView = filterText;
        mPreviousButton = previousButton;
        mNextButton = nextButton;

        if (mFilterTextView != null) {
            mFilterTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FilterPopup popup = onCreatePopup(v);
                    popup.setOnFilterItemClickedListener(new FilterPopup.OnFilterItemClickedListener() {
                        @Override
                        public void onFilterItemClicked(int position) {
                            onFilterSelected(position);
                        }
                    });
                    popup.build();
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
        setupFilterUI();
    }

    void setListener(OnFilterSelectedListener listener) {
        mOnFilterSelectedListener = listener;
    }

    void setTheme(int theme) {
        mTheme = theme;
        setupFilterUI();
    }

    void setMaxFilter(int maxFilter) {
        mMaxFilter = maxFilter;

        if (mMaxFilter != -1 && currentFilter > mMaxFilter) {

            int newFilter = mMaxFilter;
            if (mMinFilter != -1 && mMinFilter > mMaxFilter)
                newFilter = -1;

            setupFilter(newFilter);
        }
    }

    void setMinFilter(int minFilter) {
        mMinFilter = minFilter;

        if (mMinFilter != -1 && currentFilter < mMinFilter) {

            int newFilter = mMinFilter;
            if (mMaxFilter != -1 && mMinFilter > mMaxFilter)
                newFilter = -1;

            setupFilter(newFilter);
        }
    }

    void setHoldEnabled(boolean isHoldEnabled) {
        this.isHoldEnabled = isHoldEnabled;
    }

    FilterPopup onCreatePopup(View view) {
        List<String> pList = new ArrayList<>();
        for (int i = 0 ; i < mPredictionFilter.size() ; i++) {
            if (mMinFilter != -1 && i < mMinFilter)
                continue;

            if (mMaxFilter != -1 && i > mMaxFilter)
                continue;

            pList.add(mPredictionFilter.get(i));
        }
        FilterPopup filterPopup = new FilterPopup(view, mPredictionFilter, currentFilter, 0, mTheme);
        filterPopup.setMax(mMaxFilter);
        filterPopup.setMin(mMinFilter);
        return filterPopup;
    }

    private void filterNext() {
        int nextFilter = currentFilter + 1;

        if (nextFilter >= mPredictionFilter.size())
            return;

        if (mMaxFilter != -1 && nextFilter > mMaxFilter)
            return;

        onFilterSelected(nextFilter);
    }

    private void filterPrevious() {
        int nextFilter = currentFilter - 1;

        if (nextFilter < 0)
            return;

        if (mMinFilter != -1 && nextFilter < mMinFilter)
            return;

        onFilterSelected(nextFilter);
    }

    private void onFilterSelected(int position) {
        if (currentFilter != position) {
            if (!isHoldEnabled) {
                currentFilter = position;
                setupFilterUI();
            }

            if (mOnFilterSelectedListener != null)
                mOnFilterSelectedListener.onFilterSelected(position);

        }
    }

    private void setupFilter(int position) {
        if (currentFilter != position) {
            currentFilter = position;

            setupFilterUI();
        }
    }

    private void setupFilterUI() {
        if (mPreviousButton != null) {
            mPreviousButton.getDrawable().setColorFilter(mTheme == FilterTheme.LIGHT ? mDarkColor : mWhiteColor,
                    PorterDuff.Mode.SRC_ATOP);
        }
        if (mNextButton != null) {
            mNextButton.getDrawable().setColorFilter(mTheme == FilterTheme.LIGHT ? mDarkColor : mWhiteColor,
                    PorterDuff.Mode.SRC_ATOP);
        }
        if (mFilterTextView != null) {
            mFilterTextView.setText(currentFilter == -1 || mPredictionFilter == null || mPredictionFilter.size() <= currentFilter ?
                    "" :
                    mPredictionFilter.get(currentFilter));
            mFilterTextView.setTextColor(mTheme == FilterTheme.LIGHT ?
                    mDarkColor :
                    mWhiteColor);
            ((View) mFilterTextView.getParent())
                    .setBackgroundColor(mTheme == FilterTheme.DARK ?
                            ViewUtils.setAlpha(mDarkColor, 230):
                            ViewUtils.setAlpha(mWhiteColor, 170));
        }

        if (currentFilter == -1 || currentFilter == 0 || currentFilter == mMinFilter) {
            if (mPreviousButton != null) {
                mPreviousButton.getDrawable().setColorFilter(
                        ViewUtils.setAlpha(mTheme == FilterTheme.LIGHT ? mDarkColor : Color.GRAY /*mWhiteColor*/, 170),
                        PorterDuff.Mode.SRC_ATOP);
            }
        }
        if (currentFilter == -1 || currentFilter == (mPredictionFilter.size() - 1) || currentFilter == mMaxFilter) {
            if (mNextButton != null) {
                mNextButton.getDrawable().setColorFilter(
                        ViewUtils.setAlpha(mTheme == FilterTheme.LIGHT ? mDarkColor : Color.GRAY /*mWhiteColor*/, 170),
                        PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    protected Context getContext() {
        return mContext;
    }

    protected String getString(int res) {
        return mContext.getString(res);
    }

    protected abstract List<String> buildFilter();

    void rebuildFilter() {
        mPredictionFilter = buildFilter();

        setupFilterUI();
    }

    public void setSelectedFilter(int filter) {
        if (mMinFilter != -1 && filter < mMinFilter) {
            return;
        }
        if (mMaxFilter != -1 && filter > mMaxFilter) {
            return;
        }
        if (filter < 0) {
            return;
        }
        if (filter >= mPredictionFilter.size()) {
            return;
        }
        setupFilter(filter);
    }

    public int getSelectedFilter() {
        return currentFilter;
    }

    public interface OnFilterSelectedListener {
        void onFilterSelected(int stage);
    }

    public abstract static class AbstractBuilder<T extends FilterWrapper,
                                                 B extends AbstractBuilder<T, B>> {

        private final T mFilterWrapper;
        private final B thisObj;

        private int mTheme;
        private TextView mFilterText;
        private ImageView mPreviousButton;
        private ImageView mNextButton;
        private int mMinFilter = -1;
        private int mMaxFilter = -1;
        private int mInitialFilter = -1;
        private OnFilterSelectedListener mOnFilterSelectedListener;
        private boolean mIsHoldEnabled;

        AbstractBuilder(T filterWrapper) {
            mFilterWrapper = filterWrapper;
            thisObj = getThis();
        }

        public B setTheme(int theme) {
            mTheme = theme;
            return thisObj;
        }

        public B setFilterText(View filterText) {
            if (filterText != null && filterText instanceof TextView) {
                mFilterText = (TextView) filterText;
            }
            return thisObj;
        }

        public B setPreviousButton(View previousButton) {
            if (previousButton != null && previousButton instanceof ImageView) {
                mPreviousButton = (ImageView) previousButton;
            }
            return thisObj;
        }

        public B setNextButton(View nextButton) {
            if (nextButton != null && nextButton instanceof ImageView) {
                mNextButton = (ImageView) nextButton;
            }
            return thisObj;
        }

        public B setListener(OnFilterSelectedListener listener) {
            mOnFilterSelectedListener = listener;
            return thisObj;
        }

        public B setHoldEnabled(boolean isEnabled) {
            mIsHoldEnabled = isEnabled;
            return thisObj;
        }

        public B setMaxFilter(int max) {
            mMaxFilter = max;
            return thisObj;
        }

        public B setMinFilter(int min) {
            mMinFilter = min;
            return thisObj;
        }

        public B setInitialFilter(int initialFilter) {
            mInitialFilter = initialFilter;
            return thisObj;
        }

        public T build() {
            mFilterWrapper.setViews(mFilterText, mPreviousButton, mNextButton);
            mFilterWrapper.setTheme(mTheme);
            mFilterWrapper.setMaxFilter(mMaxFilter);
            mFilterWrapper.setMinFilter(mMinFilter);
            mFilterWrapper.setSelectedFilter(mInitialFilter);
            mFilterWrapper.setHoldEnabled(mIsHoldEnabled);
            mFilterWrapper.setListener(mOnFilterSelectedListener);
            return mFilterWrapper;
        }
        protected abstract B getThis();
    }
}
