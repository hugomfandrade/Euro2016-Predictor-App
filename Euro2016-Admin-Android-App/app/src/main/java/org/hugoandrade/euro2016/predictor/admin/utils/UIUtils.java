package org.hugoandrade.euro2016.predictor.admin.utils;

import android.app.Activity;
import android.support.annotation.UiThread;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Provides some general utility helper methods.
 */
public final class UIUtils {
    /**
     * Logging tag.
     */
    @SuppressWarnings("unused")
    private static final String TAG = UIUtils.class.getSimpleName();

    /**
     * Ensure this class is only used as a utility.
     */
    private UIUtils() {
        throw new AssertionError();
    }

    /**
     * Helper to show a SnackBar message.
     *
     * @param view     The view to find a parent from.
     * @param message  The string to display
     */
    @UiThread
    public static void showSnackBar(View view,
                                    String message) {
        if (view == null)
            return;

        Snackbar.make(view,
                message,
                Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Hides the soft keyboard for the provided view.
     *
     * @param view The target view for soft keyboard input.
     */
    public static void hideSoftKeyboard(View view) {
        InputMethodManager imm =
                (InputMethodManager) view.getContext().getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Hides the soft keyboard for the provided view and clear focus.
     *
     * @param view The target view for soft keyboard input.
     */
    public static void hideSoftKeyboardAndClearFocus(View view) {
        view.clearFocus();
        hideSoftKeyboard(view);
    }
}

