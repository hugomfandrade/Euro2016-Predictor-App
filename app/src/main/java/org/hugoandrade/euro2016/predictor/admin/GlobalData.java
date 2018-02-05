package org.hugoandrade.euro2016.predictor.admin;

import org.hugoandrade.euro2016.predictor.admin.object.SystemData;

public class GlobalData {

    private static SystemData mSystemData;

    public static SystemData getSystemData() {
        return mSystemData;
    }

    public static void setSystemData(SystemData systemData) {
        mSystemData = systemData;
    }
}
