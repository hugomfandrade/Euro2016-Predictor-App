package org.hugoandrade.euro2016.predictor.utils;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Hugo Andrade on 26/02/2018.
 */

public final class StickyFooterUtils {

    private static final String TAG = StickyFooterUtils.class.getSimpleName();

    public static void initialize(final ViewGroup container, final View movableView, final View fixedView) {


        container.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (top != oldTop || bottom != oldBottom) {
                    checkIfScrollViewHasEnoughContentToScroll(container, movableView, fixedView);
                }
            }
        });
    }

    private static void checkIfScrollViewHasEnoughContentToScroll(ViewGroup container, View moveableView, View fixedView) {
        View child = container.getChildAt(0);
        if (child != null) {
            int innerHeight = child.getHeight()
                    + container.getPaddingBottom()
                    + container.getPaddingTop();

            if (container.getHeight() < innerHeight) {
                fixedView.setVisibility(View.GONE);
                moveableView.setVisibility(View.VISIBLE);
                return;
            }
        }
        fixedView.setVisibility(View.VISIBLE);
        moveableView.setVisibility(View.INVISIBLE);
    }
}
