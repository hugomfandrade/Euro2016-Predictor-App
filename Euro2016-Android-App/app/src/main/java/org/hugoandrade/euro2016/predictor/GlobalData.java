package org.hugoandrade.euro2016.predictor;

import java.util.Calendar;

import org.hugoandrade.euro2016.predictor.data.LoginData;
import org.hugoandrade.euro2016.predictor.data.SystemData;
import org.hugoandrade.euro2016.predictor.data.User;

public class GlobalData {
    public static User user;
    public static SystemData systemData;

    public static void initializeUser(User user) {
        GlobalData.user = user;
    }

    public static void initializeUser(LoginData loginData) {
        GlobalData.user = new User(loginData.getUserID(), loginData.getEmail(), null, 0);
    }

    public static void resetUser(){
        GlobalData.user = null;
        GlobalData.systemData = null;
    }

    public static Calendar getServerTime() {
        return GlobalData.systemData.getDate();
    }

    public static void setSystemData(SystemData systemData) {
        GlobalData.systemData = systemData;
    }
}
