package org.hugoandrade.euro2016.predictor.utils;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.data.raw.Match;

/**
 * Provides some general utility League helper methods.
 */
public final class StageUtils {
    /**
     * Logging tag.
     */
    @SuppressWarnings("unused")
    private static final String TAG = StageUtils.class.getSimpleName();

    /**
     * Ensure this class is only used as a utility.
     */
    private StageUtils() {
        throw new AssertionError();
    }

    public static StaticVariableUtils.SStage getStage(int stage) {

        switch (stage) {
            case 0:
                return StaticVariableUtils.SStage.all;
            case 1:
            case 2:
            case 3:
                return StaticVariableUtils.SStage.groupStage;
            case 4:
                return StaticVariableUtils.SStage.roundOf16;
            case 5:
                return StaticVariableUtils.SStage.quarterFinals;
            case 6:
                return StaticVariableUtils.SStage.semiFinals;
            case 7:
                return StaticVariableUtils.SStage.finals;
            default:
                return StaticVariableUtils.SStage.unknown;
        }
    }

    public static int getStageNumber(Match match) {
        if (match == null)
            return 0;

        int matchNumber = match.getMatchNumber();
        if (matchNumber >= 1 && matchNumber <= 12)
            return 1;
        if (matchNumber >= 13 && matchNumber <= 24)
            return 2;
        if (matchNumber >= 25 && matchNumber <= 36)
            return 3;
        if (matchNumber >= 37 && matchNumber <= 44)
            return 4;
        if (matchNumber >= 45 && matchNumber <= 48)
            return 5;
        if (matchNumber >= 49 && matchNumber <= 50)
            return 6;
        if (matchNumber >= 51 && matchNumber <= 51)
            return 7;

        return 0;
    }

    public static int getMinMatchNumber(int stage) {

        switch (stage) {
            case 0:
            case 1:
                return 1;
            case 2:
                return 13;
            case 3:
                return 25;
            case 4:
                return 37;
            case 5:
                return 45;
            case 6:
                return 49;
            case 7:
                return 51;
            default:
                return 1;
        }
    }

    public static int getMaxMatchNumber(int stage) {

        switch (stage) {
            case 0:
                return 51;
            case 1:
                return 12;
            case 2:
                return 24;
            case 3:
                return 36;
            case 4:
                return 44;
            case 5:
                return 48;
            case 6:
                return 50;
            case 7:
                return 51;
            default:
                return 51;
        }
    }
}

