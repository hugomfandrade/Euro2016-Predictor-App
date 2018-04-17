package org.hugoandrade.euro2016.predictor;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.SystemData;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;
import org.hugoandrade.euro2016.predictor.utils.StaticVariableUtils;

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

    public Country getCountry(Country country) {
        if (country == null) return null;
        for (Country c : mCountryList) {
            if (c.getID().equals(country.getID())) {
                return c;
            }
        }
        return null;
    }

    public List<Match> getMatchList(Country country) {
        if (country == null || country.getName() == null) return new ArrayList<>();
        List<Match> matchList = new ArrayList<>();
        for (Match m : mMatchList) {
            if (country.getName().equals(m.getHomeTeamName())) {
                matchList.add(m);
            }
            if (country.getName().equals(m.getAwayTeamName())) {
                matchList.add(m);
            }
        }
        return matchList;
    }

    public List<Match> getMatchList(StaticVariableUtils.SStage stage) {
        List<Match> matchList = new ArrayList<>();
        for (Match m : mMatchList) {
            if (stage.name.equals(m.getStage())) {
                matchList.add(m);
            }
        }
        return matchList;
    }

    public List<Match> getMatchList(StaticVariableUtils.SStage stage, int matchday) {
        List<Match> matchList = new ArrayList<>();
        for (Match m : mMatchList) {
            if (stage.name.equals(m.getStage())) {
                if (matchday == 1 && m.getMatchNumber() >= 1 && m.getMatchNumber() <= 12) {
                    matchList.add(m);
                }
                else if (matchday == 2 && m.getMatchNumber() >= 13 && m.getMatchNumber() <= 24) {
                    matchList.add(m);
                }
                else if (matchday == 3 && m.getMatchNumber() >= 25 && m.getMatchNumber() <= 36) {
                    matchList.add(m);
                }
            }
        }
        return matchList;
    }

    public List<Country> getCountryList(Country country) {
        if (country == null || country.getGroup() == null) return new ArrayList<>();
        List<Country> countryList = new ArrayList<>();
        for (Country c : mCountryList) {
            if (country.getGroup().equals(c.getGroup())) {
                countryList.add(c);
            }
        }

        Collections.sort(countryList, new Comparator<Country>() {
            @Override
            public int compare(Country o1, Country o2) {
                return o1.getPosition() - o2.getPosition();
            }
        });
        return countryList;
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
