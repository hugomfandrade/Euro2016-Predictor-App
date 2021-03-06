package org.hugoandrade.euro2016.predictor.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.IntDef;
import android.view.KeyEvent;
import android.view.View;

import org.hugoandrade.euro2016.predictor.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SimpleDialog {

    @SuppressWarnings("unused")
    private static final String TAG = SimpleDialog.class.getSimpleName();
    public static final int YES = 1;
    public static final int NO = 2;
    public static final int BACK = 3;
    public static final int CANCEL = 4;

    @Retention(RetentionPolicy.SOURCE) @IntDef({YES, NO, BACK, CANCEL})
    public @interface Result {}

    private OnDialogResult mOnDialogResult;
    private Context context;
    private String title;
    private String message;
    private AlertDialog alert;


    public SimpleDialog(Context context, String title, String message) {
        this.context = context;
        this.title = title;
        this.message = message;

        buildPlan();
    }

    public boolean isShowing() {
        return alert.isShowing();
    }

    private void buildPlan() {

        // Initialize and build the AlertBuilderDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mOnDialogResult != null)
                            mOnDialogResult.onResult(dialog, YES);

                    }
                })
                .setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mOnDialogResult != null)
                            mOnDialogResult.onResult(dialog, NO);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (mOnDialogResult != null)
                            mOnDialogResult.onResult(dialog, CANCEL);
                    }
                })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                            if (mOnDialogResult != null)
                                mOnDialogResult.onResult(dialog, BACK);
                        }
                        return false;
                    }
                });
        alert = builder.create();
    }

    public void show() {
        alert.show();
    }

    public void dismiss() {
        alert.dismiss();
    }

    public void setOnDialogResultListener(OnDialogResult onDialogResultListener) {
        mOnDialogResult = onDialogResultListener;
    }

    public interface OnDialogResult {
        void onResult(DialogInterface dialog, @SimpleDialog.Result int result);
    }
}
