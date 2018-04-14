package org.hugoandrade.euro2016.predictor.data;

import org.hugoandrade.euro2016.predictor.data.raw.Match;

public class MatchPrediction {/*extends Match {



    protected ExercisePlanFull(Parcel in) {
        super(in);

        mOriginalCalendarEvent = in.readParcelable(CalendarEvent.class.getClassLoader());
        mOriginalExercisePlan = in.readParcelable(ExercisePlan.class.getClassLoader());
        mOriginalExercisePlanReport = in.readParcelable(ExercisePlanReport.class.getClassLoader());

        mReportDatetime = (Calendar) in.readSerializable();
        mExerciseSetReportList = in.createTypedArrayList(ExerciseSetReport.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeParcelable(mOriginalCalendarEvent, flags);
        dest.writeParcelable(mOriginalExercisePlan, flags);
        dest.writeParcelable(mOriginalExercisePlanReport, flags);

        dest.writeSerializable(mReportDatetime);
        dest.writeTypedList(mExerciseSetReportList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ExercisePlanFull> CREATOR = new Creator<ExercisePlanFull>() {
        @Override
        public ExercisePlanFull createFromParcel(Parcel in) {
            return new ExercisePlanFull(in);
        }

        @Override
        public ExercisePlanFull[] newArray(int size) {
            return new ExercisePlanFull[size];
        }
    };/**/
}
