package hugoandrade.euro2016backend.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import hugoandrade.euro2016backend.MVP;
import hugoandrade.euro2016backend.R;
import hugoandrade.euro2016backend.common.GenericActivity;
import hugoandrade.euro2016backend.object.SystemData;
import hugoandrade.euro2016backend.presenter.EditSystemDataPresenter;
import hugoandrade.euro2016backend.utils.ISO8601;

public class EditSystemDataDialog extends GenericActivity<MVP.RequiredEditSystemDataViewOps,
                                                          MVP.ProvidedEditSystemDataPresenterOps,
                                                          EditSystemDataPresenter>
        implements MVP.RequiredEditSystemDataViewOps {

    private static final String TAG = EditSystemDataDialog.class.getSimpleName();

    private Switch switchAppEnabled;
    private SeekBar
            seekBarCorrectPrediction, seekBarCorrectOutcomeViaPenalties,
            seekBarCorrectOutcome, seekBarIncorrectPredictionAndOutcome;
    private TextView tvCorrectPrediction, tvCorrectOutcome, tvCorrectOutcomeViaPenalties, tvIncorrectPredictionAndOutcome,
                     tvSetSystemDate, tvSystemDate;
    private DatePicker datePickerSystemDate;
    private TimePicker timePickerSystemDate;
    private RelativeLayout rlSystemDate, rlProgressBar;
    private Button btSetSystemData, btSetDatePicker, btSetTimePicker;

    private SystemData mSystemData;
    //private int[] currentTimePickerValues = new int[]{ 0, 0} ;
    //private int[] currentDatePickerValues = new int[]{ 0, 0, 0} ;

    public static Intent makeIntent(Context activityContext) {
        return new Intent(activityContext, EditSystemDataDialog.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.90);
        setContentView(R.layout.activity_edit_system_data);
        getWindow().setLayout(screenWidth, ViewGroup.LayoutParams.WRAP_CONTENT);

        setResult(Activity.RESULT_CANCELED);

        initializeViews();

        super.onCreate(EditSystemDataPresenter.class, this);
    }

    @Override
    protected void onDestroy() {
        getPresenter().onDestroy(isChangingConfigurations());

        super.onDestroy();
    }

    private void initializeViews() {
        btSetSystemData                      = (Button) findViewById(R.id.bt_set_system_data);
        switchAppEnabled                     = (Switch) findViewById(R.id.switch_app_enabled);
        seekBarCorrectPrediction             = (SeekBar) findViewById(R.id.seekBar_correct_prediction);
        seekBarCorrectOutcome                = (SeekBar) findViewById(R.id.seekBar_correct_outcome);
        seekBarCorrectOutcomeViaPenalties    = (SeekBar) findViewById(R.id.seekBar_correct_outcome_via_penalties);
        seekBarIncorrectPredictionAndOutcome = (SeekBar) findViewById(R.id.seekBar_incorrect_prediction_and_outcome);
        tvCorrectPrediction                  = (TextView) findViewById(R.id.tv_correct_prediction);
        tvCorrectOutcome                     = (TextView) findViewById(R.id.tv_correct_outcome);
        tvCorrectOutcomeViaPenalties         = (TextView) findViewById(R.id.tv_correct_outcome_via_penalties);
        tvIncorrectPredictionAndOutcome      = (TextView) findViewById(R.id.tv_incorrect_prediction_and_outcome);
        tvSetSystemDate                      = (TextView) findViewById(R.id.tv_set_system_date);
        tvSystemDate                         = (TextView) findViewById(R.id.tv_system_date);
        datePickerSystemDate                 = (DatePicker) findViewById(R.id.datePicker_system_date);
        btSetDatePicker                      = (Button) findViewById(R.id.bt_set_datePicker_system_date);
        timePickerSystemDate                 = (TimePicker) findViewById(R.id.timePicker_system_date);
        btSetTimePicker                      = (Button) findViewById(R.id.bt_set_timePicker_system_date);
        rlSystemDate                         = (RelativeLayout) findViewById(R.id.rl_set_system_date);
        rlProgressBar                        = (RelativeLayout) findViewById(R.id.rl_progressBar);

        seekBarCorrectPrediction.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvCorrectPrediction.setText(Integer.toString(progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarCorrectOutcome.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvCorrectOutcome.setText(Integer.toString(progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarCorrectOutcomeViaPenalties.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvCorrectOutcomeViaPenalties.setText(Integer.toString(progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarIncorrectPredictionAndOutcome.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvIncorrectPredictionAndOutcome.setText(Integer.toString(progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        datePickerSystemDate.setVisibility(View.GONE);
        timePickerSystemDate.setVisibility(View.GONE);
        rlSystemDate.setVisibility(View.GONE);
        rlSystemDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        rlProgressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        tvSetSystemDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlSystemDate.setVisibility(View.VISIBLE);
                datePickerSystemDate.setVisibility(View.VISIBLE);
                btSetDatePicker.setVisibility(View.VISIBLE);
            }
        });
        btSetSystemData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSystemData();
            }
        });
    }

    private void saveSystemData() {
        if (mSystemData == null) {
            reportMessage("Retrieve system data first before setting!!");
            return;
        }
        mSystemData.dateOfChange = Calendar.getInstance();
        mSystemData.appState = switchAppEnabled.isChecked();
        mSystemData.rules =
                Integer.toString(seekBarIncorrectPredictionAndOutcome.getProgress()) + "," +
                        Integer.toString(seekBarCorrectOutcomeViaPenalties.getProgress()) + "," +
                        Integer.toString(seekBarCorrectOutcome.getProgress()) + "," +
                        Integer.toString(seekBarCorrectPrediction.getProgress());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            mSystemData.systemDate.set(
                    datePickerSystemDate.getYear(),
                    datePickerSystemDate.getMonth(),
                    datePickerSystemDate.getDayOfMonth(),
                    timePickerSystemDate.getHour(),
                    timePickerSystemDate.getMinute());
        else
            mSystemData.systemDate.set(
                    datePickerSystemDate.getYear(),
                    datePickerSystemDate.getMonth(),
                    datePickerSystemDate.getDayOfMonth(),
                    timePickerSystemDate.getCurrentHour(),
                    timePickerSystemDate.getCurrentMinute());

        getPresenter().setSystemData(mSystemData);
    }

    @Override
    public void reportSystemData(SystemData systemData) {
        this.mSystemData = systemData;

        rlProgressBar.setVisibility(View.GONE);
        switchAppEnabled.setChecked(mSystemData.appState);

        seekBarCorrectPrediction.setProgress(Integer.parseInt(mSystemData.rules.split(",")[3]));
        seekBarCorrectOutcome.setProgress(Integer.parseInt(mSystemData.rules.split(",")[2]));
        seekBarCorrectOutcomeViaPenalties.setProgress(Integer.parseInt(mSystemData.rules.split(",")[1]));
        seekBarIncorrectPredictionAndOutcome.setProgress(Integer.parseInt(mSystemData.rules.split(",")[0]));

        tvSystemDate.setText(ISO8601.fromCalendarButClean(mSystemData.getSystemDate()));

        /*datePickerSystemDate.init(
                mSystemData.getSystemDate().get(Calendar.YEAR),
                mSystemData.getSystemDate().get(Calendar.MONTH),
                mSystemData.getSystemDate().get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        //if (dayOfMonth != mSystemData.getSystemDate().get(Calendar.DAY_OF_MONTH)) {
                            datePickerSystemDate.setVisibility(View.GONE);
                            timePickerSystemDate.setVisibility(View.VISIBLE);

                            mSystemData.systemDate.set(Calendar.YEAR, year);
                            mSystemData.systemDate.set(Calendar.MONTH, monthOfYear);
                            mSystemData.systemDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        //}
                    }
                });/**/
        btSetDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerSystemDate.setVisibility(View.GONE);
                btSetDatePicker.setVisibility(View.GONE);
                timePickerSystemDate.setVisibility(View.VISIBLE);
                btSetTimePicker.setVisibility(View.VISIBLE);
            }
        });
        //currentDatePickerValues[0] = mSystemData.getSystemDate().get(Calendar.YEAR);
        //currentDatePickerValues[1] = mSystemData.getSystemDate().get(Calendar.MONTH);
        //currentDatePickerValues[2] = mSystemData.getSystemDate().get(Calendar.DAY_OF_MONTH);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePickerSystemDate.setHour(mSystemData.getSystemDate().get(Calendar.HOUR));
            timePickerSystemDate.setMinute(mSystemData.getSystemDate().get(Calendar.MINUTE));
        }
        else {
            timePickerSystemDate.setCurrentHour(mSystemData.getSystemDate().get(Calendar.HOUR));
            timePickerSystemDate.setCurrentMinute(mSystemData.getSystemDate().get(Calendar.MINUTE));
        }
        //currentTimePickerValues[0] = mSystemData.getSystemDate().get(Calendar.HOUR);
        //currentTimePickerValues[1] = mSystemData.getSystemDate().get(Calendar.MINUTE);

        /*timePickerSystemDate.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                if (currentTimePickerValues[1] != minute) {
                    Log.e(TAG, "changed: " +
                            Integer.toString(mSystemData.getSystemDate().get(Calendar.MINUTE)) + " , " +
                            Integer.toString(minute));
                    mSystemData.systemDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    mSystemData.systemDate.set(Calendar.MINUTE, minute);
                    mSystemData.dateOfChange = Calendar.getInstance();

                    rlSystemDate.setVisibility(View.GONE);
                    timePickerSystemDate.setVisibility(View.GONE);
                    tvSystemDate.setText(ISO8601.fromCalendarButClean(mSystemData.getSystemDate()));
                }
                currentTimePickerValues[0] = hourOfDay;
                currentTimePickerValues[1] = minute;
            }
        });/**/
        btSetTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlSystemDate.setVisibility(View.GONE);
                timePickerSystemDate.setVisibility(View.GONE);
                btSetTimePicker.setVisibility(View.GONE);

                Calendar c = Calendar.getInstance();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    c.set(
                            datePickerSystemDate.getYear(),
                            datePickerSystemDate.getMonth(),
                            datePickerSystemDate.getDayOfMonth(),
                            timePickerSystemDate.getHour(),
                            timePickerSystemDate.getMinute());
                else
                    c.set(
                            datePickerSystemDate.getYear(),
                            datePickerSystemDate.getMonth(),
                            datePickerSystemDate.getDayOfMonth(),
                            timePickerSystemDate.getCurrentHour(),
                            timePickerSystemDate.getCurrentMinute());
                tvSystemDate.setText(ISO8601.fromCalendarButClean(c));
            }
        });
    }

    @Override
    public void reportMessage(String message) {
        showToast(message);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
