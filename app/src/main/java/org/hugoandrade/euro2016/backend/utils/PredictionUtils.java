package org.hugoandrade.euro2016.backend.utils;

import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.object.Prediction;

/**
 * Provides some general utility Match helper methods.
 */
public final class PredictionUtils {
    /**
     * Logging tag.
     */
    @SuppressWarnings("unused")
    private static final String TAG = PredictionUtils.class.getSimpleName();

    /**
     * Ensure this class is only used as a utility.
     */
    private PredictionUtils() {
        throw new AssertionError();
    }

    public static boolean isPredictionSet(Prediction prediction) {
        return prediction.getHomeTeamGoals() != -1 && prediction.getAwayTeamGoals() != -1;
    }

    public static boolean didPredictHomeTeamWin(Prediction prediction) {
        return prediction.getHomeTeamGoals() > prediction.getAwayTeamGoals();
    }

    public static boolean didPredictAwayTeamWin(Prediction prediction) {
        return prediction.getAwayTeamGoals() > prediction.getHomeTeamGoals();
    }

    public static boolean isPredictionCorrect(Match match, Prediction prediction) {
        return prediction.getHomeTeamGoals() == match.getHomeTeamGoals()
                && prediction.getAwayTeamGoals() == match.getAwayTeamGoals();
    }

    public static boolean didPredictTie(Prediction prediction) {
        return prediction.getHomeTeamGoals() == prediction.getAwayTeamGoals();
    }
}

