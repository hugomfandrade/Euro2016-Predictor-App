package org.hugoandrade.euro2016.predictor.common;

import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.View;

public class SplashScreenAnimation {

    private final View mInitView;
    private final View mFinalView;

    private View[] mDisappearingViews;
    private View[] mAppearingViews;

    private long mSplashDuration = 1000L;
    private long mAnimationDuration = 500L;

    private OnSplashScreenAnimationEndListener mListener;

    private boolean hasSplashScreenDurationEnded = false;
    private boolean isHolding = false;

    private SplashScreenAnimation(View initView, View finalView) {
        mInitView = initView;
        mFinalView = finalView;
    }

    private void setDisappearingViews(View... disappearingViews) {
        mDisappearingViews = disappearingViews;
    }

    private void setAppearingViews(View... appearingViews) {
        mAppearingViews = appearingViews;
    }

    private void setSplashDuration(long splashDuration) {
        mSplashDuration = splashDuration;
    }

    private void setAnimationDuration(long animationDuration) {
        mAnimationDuration = animationDuration;
    }

    private void setOnSplashScreenAnimationEndListener(OnSplashScreenAnimationEndListener listener) {
        mListener = listener;
    }

    private void start(boolean hold) {
        isHolding = !hold;

        start();
    }

    private void start() {

        if (mAppearingViews != null)
            for (View v : mAppearingViews)
                v.setAlpha(0);

        if (mDisappearingViews != null)
            for (View v : mDisappearingViews)
                v.setAlpha(1);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hasSplashScreenDurationEnded = true;

                runAnimationEnded();
            }
        }, mSplashDuration);
    }

    public void stopHold() {
        isHolding = false;

        runAnimationEnded();
    }

    private void runAnimationEnded() {
        if (!hasSplashScreenDurationEnded || isHolding)
            return;

        float toScaleX = getToScaleY();
        float toScaleY = getToScaleX();

        int translationY = getTranslationY();
        int translationX = getTranslationX();

        int finalTranslationX =
                (int) (translationX - mInitView.getMeasuredWidth() * (1f - toScaleX) / 2f);
        int finalTranslationY =
                (int) (translationY - mInitView.getMeasuredHeight() * (1f - toScaleY) / 2f);
        // Translate Splash Layout to the same position as the Logo ImageView
        mInitView.animate()
                .scaleX(toScaleX)
                .scaleY(toScaleY)
                .translationY(finalTranslationY)
                .translationX(finalTranslationX)
                .setDuration(mAnimationDuration)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // Make splash layout invisible
                        if (mDisappearingViews != null)
                            for (View v : mDisappearingViews)
                                v.setVisibility(View.INVISIBLE);
                        mInitView.setVisibility(View.INVISIBLE);

                        // Show login logo
                        mFinalView.setVisibility(View.VISIBLE);

                        if (mAppearingViews != null)
                            for (View v : mAppearingViews)
                                v.animate().alpha(1)
                                        .setDuration(mAnimationDuration)
                                        .start();

                        if (mListener != null)
                            mListener.onAnimEnded();
                    }
                })
                .start();


        if (mDisappearingViews != null)
            for (View v : mDisappearingViews) {
                v.animate().scaleX(toScaleX)
                        .translationY(finalTranslationY)
                        .translationX(finalTranslationX)
                        .setDuration(mAnimationDuration)
                        .start();
                v.animate().alpha(0)
                        .setDuration(mAnimationDuration)
                        .start();
            }
    }

    private int getTranslationY() {
        Rect rectLogo = new Rect();
        Rect rectSplash = new Rect();
        mFinalView.getGlobalVisibleRect(rectLogo);
        mInitView.getGlobalVisibleRect(rectSplash);
        return rectLogo.top - rectSplash.top;
    }

    private int getTranslationX() {
        Rect rectLogo = new Rect();
        Rect rectSplash = new Rect();
        mFinalView.getGlobalVisibleRect(rectLogo);
        mInitView.getGlobalVisibleRect(rectSplash);
        return rectLogo.left - rectSplash.left;
    }

    private float getToScaleY() {
        return ((float) mFinalView.getMeasuredHeight()) / ((float) mInitView.getMeasuredHeight());
    }

    private float getToScaleX() {
        return ((float) mFinalView.getMeasuredWidth()) / ((float) mInitView.getMeasuredWidth());
    }

    public interface OnSplashScreenAnimationEndListener {
        void onAnimEnded();
    }

    public static class Builder {

        SplashScreenAnimationParams P;

        public Builder(View initView, View finalView) {
            P = new SplashScreenAnimationParams(initView, finalView);
        }

        public static Builder instance(View initView, View finalView) {
            return new Builder(initView, finalView);
        }

        public Builder setDisappearingViews(View... disappearingViews) {
            P.disappearingViews = disappearingViews;
            return this;
        }

        public Builder setAppearingViews(View... appearingViews) {
            P.appearingViews = appearingViews;
            return this;
        }

        public Builder setSplashDuration(long splashDuration) {
            P.splashDuration = splashDuration;
            return this;
        }

        public Builder setAnimationDuration(long animationDuration) {
            P.animationDuration = animationDuration;
            return this;
        }

        public Builder withEndAction(OnSplashScreenAnimationEndListener onAnimEnded) {
            P.onAnimEnded = onAnimEnded;
            return this;
        }

        public SplashScreenAnimation start(boolean hold) {
            SplashScreenAnimation anim
                    = new SplashScreenAnimation(P.initView, P.finalView);

            P.apply(anim);

            anim.start(hold);

            return anim;
        }
    }

    private static class SplashScreenAnimationParams {

        private final View initView;
        private final View finalView;
        private View[] disappearingViews;
        private View[] appearingViews;
        private long splashDuration = 1000L;
        private long animationDuration = 500L;
        private OnSplashScreenAnimationEndListener onAnimEnded;

        SplashScreenAnimationParams(View initV, View finalV) {
            initView = initV;
            finalView = finalV;
        }

        void apply(SplashScreenAnimation anim) {
            anim.setDisappearingViews(disappearingViews);
            anim.setAppearingViews(appearingViews);
            anim.setSplashDuration(splashDuration);
            anim.setAnimationDuration(animationDuration);
            anim.setOnSplashScreenAnimationEndListener(onAnimEnded);
        }
    }
}
