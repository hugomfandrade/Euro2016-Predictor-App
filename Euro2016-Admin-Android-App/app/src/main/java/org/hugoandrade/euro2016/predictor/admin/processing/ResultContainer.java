package org.hugoandrade.euro2016.predictor.admin.processing;

import android.os.Parcel;
import android.os.Parcelable;

import org.hugoandrade.euro2016.predictor.admin.data.Country;
import org.hugoandrade.euro2016.predictor.admin.data.Match;

import java.util.List;

public class ResultContainer implements Parcelable {

    public final List<Country> mCountryList;
    public final List<Match> mMatchList;

    public ResultContainer(List<Country> countryList, List<Match> matchList) {
        mCountryList = countryList;
        mMatchList = matchList;
    }

    protected ResultContainer(Parcel in) {
        mCountryList = in.createTypedArrayList(Country.CREATOR);
        mMatchList = in.createTypedArrayList(Match.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mCountryList);
        dest.writeTypedList(mMatchList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ResultContainer> CREATOR = new Creator<ResultContainer>() {
        @Override
        public ResultContainer createFromParcel(Parcel in) {
            return new ResultContainer(in);
        }

        @Override
        public ResultContainer[] newArray(int size) {
            return new ResultContainer[size];
        }
    };
}
