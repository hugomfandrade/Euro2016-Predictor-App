package org.hugoandrade.euro2016.predictor.admin.processing;

import android.os.Parcel;
import android.os.Parcelable;

import org.hugoandrade.euro2016.predictor.admin.data.Country;
import org.hugoandrade.euro2016.predictor.admin.data.Match;

public class ProgressContainer implements Parcelable {

    public final Country mCountry;
    public final Match mMatch;

    public ProgressContainer(Match match) {
        this(null, match);
    }

    public ProgressContainer(Country country) {
        this(country, null);
    }

    private ProgressContainer(Country country, Match match) {
        mCountry = country;
        mMatch = match;
    }

    protected ProgressContainer(Parcel in) {
        mCountry = in.readParcelable(Country.class.getClassLoader());
        mMatch = in.readParcelable(Match.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mCountry, flags);
        dest.writeParcelable(mMatch, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProgressContainer> CREATOR = new Creator<ProgressContainer>() {
        @Override
        public ProgressContainer createFromParcel(Parcel in) {
            return new ProgressContainer(in);
        }

        @Override
        public ProgressContainer[] newArray(int size) {
            return new ProgressContainer[size];
        }
    };
}
