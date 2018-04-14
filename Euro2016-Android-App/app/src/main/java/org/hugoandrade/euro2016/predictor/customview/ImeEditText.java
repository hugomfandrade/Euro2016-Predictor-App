package org.hugoandrade.euro2016.predictor.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class ImeEditText extends android.support.v7.widget.AppCompatEditText {

    private OnKeyPreImeListener mOnKeyPreImeListener;

    public ImeEditText(Context context) {
        super(context);
    }

    public ImeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (mOnKeyPreImeListener != null) {
            return mOnKeyPreImeListener.onKeyPreIme(keyCode, event);
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setOnKeyPreImeListener(OnKeyPreImeListener listener) {
        mOnKeyPreImeListener = listener;
    }

    public interface OnKeyPreImeListener {
        boolean onKeyPreIme(int keyCode, KeyEvent event);
    }
}
