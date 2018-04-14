package org.hugoandrade.euro2016.predictor;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.SystemData;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;

public class GlobalData {

    private static final String TAG = GlobalData.class.getSimpleName();

    private static GlobalData mInstance = null;

    public User user;
    public SystemData systemData;

    private HashSet<OnMatchesChangedListener> mOnMatchesChangedListenerSet = new HashSet<>();
    private HashSet<OnCountriesChangedListener> mOnCountriesChangedListenerSet = new HashSet<>();
    private HashSet<OnPredictionsChangedListener> mOnPredictionsChangedListenerSet = new HashSet<>();
    private HashSet<OnUsersChangedListener> mOnUsersChangedListenerSet = new HashSet<>();
    private HashSet<OnLatestPerformanceChangedListener> mOnLatestPerformanceChangedListenerSet = new HashSet<>();

    private List<Country> mCountryList = new ArrayList<>();
    private List<Match> mMatchList = new ArrayList<>();
    private List<User> mUserList = new ArrayList<>();
    private List<Prediction> mPredictionList = new ArrayList<>();
    private HashMap<String, List<Prediction>> mLatestPerformanceMap = new HashMap<>();

    /*public static GlobalData getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException(TAG + " is not initialized");
        }
        return mInstance;
    }/**/

    public static GlobalData getInstance() {
        if (mInstance == null) {
            mInstance = new GlobalData();
        }
        return mInstance;
    }

    public static void unInitialize() {
        if (mInstance == null) {
            return;
        }
        try {
            mInstance.user = null;
        } catch (IllegalStateException e) {
            Log.e(TAG, "unInitialize error: " + e.getMessage());
        }
    }
    public Calendar getServerTime() {
        return systemData.getDate();
    }

    public void setSystemData(SystemData systemData) {
        this.systemData = systemData;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<User> getUserList() {
        return mUserList;
    }

    public void setUserList(List<User> userList) {
        this.mUserList = userList;

        for (OnUsersChangedListener listener : mOnUsersChangedListenerSet) {
            listener.onUsersChanged();
        }
    }

    public List<Prediction> getPredictionList() {
        return mPredictionList;
    }

    public void setPredictionList(List<Prediction> predictionList) {
        this.mPredictionList = predictionList;

        for (OnPredictionsChangedListener listener : mOnPredictionsChangedListenerSet) {
            listener.onPredictionsChanged();
        }
    }

    public List<Country> getCountryList() {
        return mCountryList;
    }

    public void setCountryList(List<Country> countryList) {
        this.mCountryList = countryList;

        for (OnCountriesChangedListener listener : mOnCountriesChangedListenerSet) {
            listener.onCountriesChanged();
        }
    }

    public List<Match> getMatchList() {
        return mMatchList;
    }

    public void setMatchList(List<Match> matchList) {
        this.mMatchList = matchList;

        for (OnMatchesChangedListener listener : mOnMatchesChangedListenerSet) {
            listener.onMatchesChanged();
        }
    }

    public void setLatestPerformanceOfUsers(List<Prediction> predictionList) {

        for (Prediction p : predictionList) {
            String userID = p.getUserID();

            if (mLatestPerformanceMap.containsKey(userID)) {
                mLatestPerformanceMap.get(userID).add(p);
            } else {
                mLatestPerformanceMap.put(userID, new ArrayList<Prediction>());
                mLatestPerformanceMap.get(userID).add(p);
            }
        }

        for (OnLatestPerformanceChangedListener listener : mOnLatestPerformanceChangedListenerSet) {
            listener.onLatestPerformanceChanged();
        }
    }

    public int[] getLatestPerformance(User user) {
        List<Prediction> latestPerformancePredictionList = mLatestPerformanceMap.get(user.getID());

        if (latestPerformancePredictionList == null) {
            latestPerformancePredictionList = new ArrayList<>();
        }

        int finalMatchNumber = MatchUtils.getMatchNumberOfFirstNotPlayedMatched(mMatchList, getServerTime().getTime());

        // 2
        // 7
        int initMatchNumber = finalMatchNumber < 6 ? 1 : finalMatchNumber - 5;

        int[] a = new int[finalMatchNumber - initMatchNumber];

        int i = 0;
        for (int matchNumber = finalMatchNumber - 1 ; matchNumber >= initMatchNumber; matchNumber--) {
            int score = systemData.getRules().getRuleIncorrectPrediction();

            //Prediction prediction = null;
            for (Prediction p : latestPerformancePredictionList) {
                if (p.getMatchNumber() == matchNumber && p.getScore() != -1) {
                    score = p.getScore();
                }
            }
            a[i] = score;
            i++;
        }

        return a;
    }

    public interface OnLatestPerformanceChangedListener {
        void onLatestPerformanceChanged();
    }

    public interface OnMatchesChangedListener {
        void onMatchesChanged();
    }

    public interface OnCountriesChangedListener {
        void onCountriesChanged();
    }

    public interface OnPredictionsChangedListener {
        void onPredictionsChanged();
    }

    public interface OnUsersChangedListener {
        void onUsersChanged();
    }

    public void addOnMatchesChangedListener(OnMatchesChangedListener listener) {
        mOnMatchesChangedListenerSet.add(listener);
    }

    public void removeOnMatchesChangedListener(OnMatchesChangedListener listener) {
        mOnMatchesChangedListenerSet.remove(listener);
    }

    public void addOnCountriesChangedListener(OnCountriesChangedListener listener) {
        mOnCountriesChangedListenerSet.add(listener);
    }

    public void removeOnCountriesChangedListener(OnCountriesChangedListener listener) {
        mOnCountriesChangedListenerSet.remove(listener);
    }

    public void addOnPredictionsChangedListener(OnPredictionsChangedListener listener) {
        mOnPredictionsChangedListenerSet.add(listener);
    }

    public void removeOnPredictionsChangedListener(OnPredictionsChangedListener listener) {
        mOnPredictionsChangedListenerSet.remove(listener);
    }

    public void addOnUsersChangedListener(OnUsersChangedListener listener) {
        mOnUsersChangedListenerSet.add(listener);
    }

    public void removeOnUsersChangedListener(OnUsersChangedListener listener) {
        mOnUsersChangedListenerSet.remove(listener);
    }

    public void addOnLatestPerformanceChangedListener(OnLatestPerformanceChangedListener listener) {
        mOnLatestPerformanceChangedListenerSet.add(listener);
    }

    public void removeOnLatestPerformanceChangedListener(OnLatestPerformanceChangedListener listener) {
        mOnLatestPerformanceChangedListenerSet.remove(listener);
    }
}
