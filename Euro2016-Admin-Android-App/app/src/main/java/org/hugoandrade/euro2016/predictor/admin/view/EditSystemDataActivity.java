package org.hugoandrade.euro2016.predictor.admin.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CalendarView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.admin.R;
import org.hugoandrade.euro2016.predictor.admin.data.SystemData;
import org.hugoandrade.euro2016.predictor.admin.utils.ISO8601;

import java.util.Calendar;

public class EditSystemDataActivity extends AppCompatActivity {

    private static final String INTENT_EXTRA_SYSTEM_DATA = "SystemData";

    private SystemData mSystemData;

    private Switch switchAppEnabled;
    private ViewStruct viewStructCorrectPrediction;
    private ViewStruct viewStructCorrectOutcome;
    private ViewStruct viewStructCorrectOutcomeViaPenalties;
    private ViewStruct viewStructIncorrectPrediction;

    private TextView tvSystemDate;
    private CalendarView cvSelect;
    private NumberPicker npHour;
    private NumberPicker npMinute;

    public static Intent makeIntent(Context context, SystemData systemData) {
        return new Intent(context, EditSystemDataActivity.class)
                .putExtra(INTENT_EXTRA_SYSTEM_DATA, systemData);
    }

    public static SystemData extractSystemDataFromIntent(Intent data) {
        return data.getParcelableExtra(INTENT_EXTRA_SYSTEM_DATA);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSystemData = extractSystemDataFromIntent(getIntent());

        initializeUI();

        setupUI();
    }

    private void initializeUI() {
        setContentView(R.layout.activity_edit_system_data);

        switchAppEnabled = (Switch) findViewById(R.id.switch_app_enabled);


        viewStructCorrectPrediction
                = new ViewStruct(findViewById(R.id.layout_correct_prediction),
                                 R.string.correct_prediction);
        viewStructCorrectOutcome
                = new ViewStruct(findViewById(R.id.layout_correct_outcome),
                                 R.string.correct_outcome);
        viewStructCorrectOutcomeViaPenalties
                = new ViewStruct(findViewById(R.id.layout_correct_outcome_via_penalties),
                                 R.string.correct_outcome_via_penalties);
        viewStructIncorrectPrediction
                = new ViewStruct(findViewById(R.id.layout_incorrect_prediction_and_outcome),
                                 R.string.incorrect_prediction_and_outcome);

        tvSystemDate = (TextView) findViewById(R.id.tv_system_date);
        cvSelect = (CalendarView) findViewById(R.id.cv_select_date);
        cvSelect.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view,
                                            int year,
                                            int month,
                                            int dayOfMonth) {
                mSystemData.setSystemDate(year, month, dayOfMonth);
                updateSystemDataUI();
            }
        });
        npHour = (NumberPicker) findViewById(R.id.np_hour);
        npHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mSystemData.setSystemDate(Calendar.HOUR_OF_DAY, newVal);
                updateSystemDataUI();
            }
        });
        npMinute = (NumberPicker) findViewById(R.id.np_minute);
        npMinute.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mSystemData.setSystemDate(Calendar.MINUTE, newVal);
                updateSystemDataUI();
            }
        });

        View vGoBack = findViewById(R.id.v_go_back);
        vGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
        View tvCancel = findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
        View tvSet = findViewById(R.id.tv_set);
        tvSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });
    }

    private void setupUI() {

        switchAppEnabled.setChecked(mSystemData.getAppState());

        viewStructCorrectPrediction.setProgress(mSystemData.getRules().getRuleCorrectPrediction());
        viewStructCorrectOutcome.setProgress(mSystemData.getRules().getRuleCorrectOutcome());
        viewStructCorrectOutcomeViaPenalties.setProgress(mSystemData.getRules().getRuleCorrectOutcomeViaPenalties());
        viewStructIncorrectPrediction.setProgress(mSystemData.getRules().getRuleIncorrectPrediction());

        cvSelect.setDate(mSystemData.getSystemDate().getTimeInMillis());
        npHour.setValue(mSystemData.getSystemDate().get(Calendar.HOUR));
        npMinute.setValue(mSystemData.getSystemDate().get(Calendar.MINUTE));

        updateSystemDataUI();
    }

    private void updateSystemDataUI() {
        tvSystemDate.setText(ISO8601.fromCalendarButClean(mSystemData.getSystemDate()));
    }

    private void confirm() {
        mSystemData.setDateOfChange(Calendar.getInstance());
        mSystemData.setAppState(switchAppEnabled.isChecked());
        mSystemData.setRules(new SystemData.Rules(
                viewStructIncorrectPrediction.getProgress(),
                viewStructCorrectOutcomeViaPenalties.getProgress(),
                viewStructCorrectOutcome.getProgress(),
                viewStructCorrectPrediction.getProgress()));

        setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_SYSTEM_DATA, mSystemData));
        onBackPressed();
    }

    private void goBack() {
        setResult(RESULT_CANCELED);
        onBackPressed();
    }

    private class ViewStruct implements SeekBar.OnSeekBarChangeListener {

        private SeekBar mSeekBar;
        private TextView tvTitle, tvValue;

        int mValue = 0;

        ViewStruct(View view, int titleResID) {
            mSeekBar = (SeekBar) view.findViewById(R.id.seekBar_value);
            tvTitle = (TextView) view.findViewById(R.id.tv_title);
            tvValue = (TextView) view.findViewById(R.id.tv_value);

            tvTitle.setText(titleResID);
            mSeekBar.setProgress(mValue);
            mSeekBar.setOnSeekBarChangeListener(this);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mValue = progress;
            tvValue.setText(String.valueOf(mValue));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { }

        void setProgress(int value) {
            mValue = value;
            mSeekBar.setProgress(mValue);
            tvValue.setText(String.valueOf(mValue));
        }

        int getProgress() {
            return mValue;
        }
    }
}
