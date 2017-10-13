package org.hugoandrade.euro2016.backend.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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
}

