package org.hugoandrade.euro2016.predictor.customview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.R;

public class IconTabLayout extends TabLayout {

    private int selectedColor = Color.GRAY;
    private int nonSelectedColor = Color.GRAY;

    private ViewPager mViewPager;
    private AdapterChangeListener mAdapterChangeListener;
    private IconTabLayoutListener mPagerAdapter;
    private DataSetObserver mPagerAdapterObserver;

    public IconTabLayout(Context context) {
        super(context);

        init(context, null);
    }

    public IconTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public IconTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconTabLayout, 0, 0);

        selectedColor =
                a.getColor(R.styleable.IconTabLayout_selectedColor, Color.GRAY);
        nonSelectedColor =
                a.getColor(R.styleable.IconTabLayout_nonSelectedColor, Color.GRAY);

        a.recycle();

        addOnTabSelectedListener(mTabSelectedListener);
    }

    @NonNull
    @Override
    public Tab newTab() {
        Tab tab = super.newTab();

        View customTab = LayoutInflater.from(getContext()).inflate(R.layout.tab_icon, null);
        tab.setCustomView(customTab);

        return tab;
    }
    @Override
    public void setupWithViewPager(@Nullable ViewPager viewPager) {
        super.setupWithViewPager(viewPager);
    }

    @Override
    public void setupWithViewPager(@Nullable ViewPager viewPager, boolean autoRefresh) {
        super.setupWithViewPager(viewPager, autoRefresh);

        if (mViewPager != null) {
            // If we've already been setup with a ViewPager, remove us from it
            if (mAdapterChangeListener != null) {
                mViewPager.removeOnAdapterChangeListener(mAdapterChangeListener);
            }
        }

        if (viewPager != null) {
            mViewPager = viewPager;
            final PagerAdapter adapter = viewPager.getAdapter();
            if (adapter != null && adapter instanceof IconTabLayoutListener) {
                // Now we'll populate ourselves from the pager adapter, adding an observer if
                // autoRefresh is enabled
                setPagerAdapter(adapter, autoRefresh);
            }
            if (mAdapterChangeListener == null) {
                mAdapterChangeListener = new AdapterChangeListener();
            }
            mAdapterChangeListener.setAutoRefresh(autoRefresh);
            viewPager.addOnAdapterChangeListener(mAdapterChangeListener);
        } else {
            mViewPager = null;
            setPagerAdapter(null, false);
        }
    }

    void setPagerAdapter(@Nullable final PagerAdapter adapter, final boolean addObserver) {
        if (mPagerAdapter != null && mPagerAdapterObserver != null) {
            // If we already have a PagerAdapter, unregister our observer
            mPagerAdapter.unregisterDataSetObserver(mPagerAdapterObserver);
        }


        if (addObserver && adapter != null && adapter instanceof IconTabLayoutListener) {
            IconTabLayoutListener a = (IconTabLayoutListener) adapter;
            mPagerAdapter = a;
            // Register our observer on the new adapter
            if (mPagerAdapterObserver == null) {
                mPagerAdapterObserver = new PagerAdapterObserver();
            }
            a.registerDataSetObserver(mPagerAdapterObserver);
        }
        else {
            mPagerAdapter = null;
        }

        // Finally make sure we reflect the new adapter
        populateFromPagerAdapter();
    }

    void populateFromPagerAdapter() {

        if (mPagerAdapter != null && mPagerAdapter.getCount() == getTabCount()) {
            final int adapterCount = mPagerAdapter.getCount();

            for (int i = 0; i < getTabCount(); i++) {

                View tab = getTabAt(i).getCustomView();

                if (tab == null) continue;

                ((ImageView) tab.findViewById(R.id.iv_tab))
                        .setImageResource(mPagerAdapter.getPageIcon(i));
                ((TextView) tab.findViewById(R.id.tv_tab))
                        .setText(mPagerAdapter.getPageTitle(i));
                setupTab(getTabAt(i), nonSelectedColor);
            }

            // Make sure we reflect the currently set ViewPager item
            if (mViewPager != null && adapterCount > 0) {
                final int curItem = mViewPager.getCurrentItem();
                if (curItem != getSelectedTabPosition() && curItem < getTabCount()) {
                    getTabAt(curItem).select();
                }
                else {
                    setupTab(getTabAt(curItem), selectedColor);
                }
            }
        }
    }

    public interface IconTabLayoutListener {

        int getCount();

        int getPageIcon(int position);

        CharSequence getPageTitle(int position);

        void registerDataSetObserver(DataSetObserver pagerAdapterObserver);

        void unregisterDataSetObserver(DataSetObserver pagerAdapterObserver);
    }

    private class PagerAdapterObserver extends DataSetObserver {
        PagerAdapterObserver() {
        }

        @Override
        public void onChanged() {
            populateFromPagerAdapter();
        }

        @Override
        public void onInvalidated() {
            populateFromPagerAdapter();
        }
    }

    private class AdapterChangeListener implements ViewPager.OnAdapterChangeListener {
        private boolean mAutoRefresh;

        AdapterChangeListener() {
        }

        @Override
        public void onAdapterChanged(@NonNull ViewPager viewPager,
                                     @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
            if (mViewPager == viewPager) {
                setPagerAdapter(newAdapter, mAutoRefresh);
            }
        }

        void setAutoRefresh(boolean autoRefresh) {
            mAutoRefresh = autoRefresh;
        }
    }

    private final OnTabSelectedListener mTabSelectedListener = new OnTabSelectedListener() {

        @Override
        public void onTabSelected(Tab tab) {
            setupTab(tab, selectedColor);
        }

        @Override
        public void onTabUnselected(Tab tab) {
            setupTab(tab, nonSelectedColor);
        }

        @Override
        public void onTabReselected(Tab tab) {
            setupTab(tab, selectedColor);
        }
    };

    private void setupTab(Tab tab, int color) {
        View customTab = tab.getCustomView();

        if (customTab == null
                || customTab.findViewById(R.id.iv_tab) == null
                || customTab.findViewById(R.id.tv_tab) == null)
            return;

        ((ImageView) customTab.findViewById(R.id.iv_tab))
                .setImageTintList(ColorStateList.valueOf(color));
        ((TextView) customTab.findViewById(R.id.tv_tab))
                .setTextColor(color);
    }
}