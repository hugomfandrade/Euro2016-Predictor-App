package hugoandrade.euro2016;

import java.util.Calendar;

import hugoandrade.euro2016.object.LoginData;
import hugoandrade.euro2016.object.SystemData;
import hugoandrade.euro2016.object.User;

public class GlobalData {
    public static User user;
    public static SystemData systemData;

    public static void initializeUser(User user) {
        GlobalData.user = user;
    }

    public static void resetUser(){
        GlobalData.user = null;
        GlobalData.systemData = null;
    }

    public static Calendar getServerTime() {
        return GlobalData.systemData.getSystemDate();
    }

    public static void setSystemData(SystemData systemData) {
        GlobalData.systemData = systemData;
    }
}
