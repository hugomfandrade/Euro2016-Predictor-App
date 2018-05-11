package org.hugoandrade.euro2016.predictor.common;

import android.text.Editable;
import android.text.TextWatcher;

public class TextWatcherAdapter implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No-ops
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // No-ops
    }

    @Override
    public void afterTextChanged(Editable s) {
        // No-ops
    }
}
