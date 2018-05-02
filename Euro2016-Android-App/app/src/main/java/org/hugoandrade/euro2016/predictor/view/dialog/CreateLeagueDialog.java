package org.hugoandrade.euro2016.predictor.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.utils.ViewUtils;

public class CreateLeagueDialog {


    private final EditText etLeagueName;

    private OnCreateListener mOnCreateListener;
    private AlertDialog alert;

    public CreateLeagueDialog(Context context) {

        View view = View.inflate(context, R.layout.dialog_create_league, null);
        View tvCreate = view.findViewById(R.id.tv_create);
        tvCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnCreateListener != null) {
                    mOnCreateListener.onCreate(etLeagueName.getText().toString().trim());
                }
                alert.dismiss();
                ViewUtils.hideSoftKeyboardAndClearFocus(etLeagueName);
            }
        });
        etLeagueName = view.findViewById(R.id.et_league_name);

        // Initialize and build the AlertBuilderDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {

                        }
                        return false;
                    }
                });
        alert = builder.create();
    }

    public boolean isShowing() {
        return alert.isShowing();
    }

    public void show() {
        alert.show();
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public void dismiss() {
        alert.dismiss();
    }

    public void setOnCreateListener(OnCreateListener onCreateListener) {
        mOnCreateListener = onCreateListener;
    }

    public interface OnCreateListener {
        void onCreate(String leagueName);
    }
}
