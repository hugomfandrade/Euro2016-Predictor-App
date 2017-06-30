package hugoandrade.euro2016.object;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonObject;

public class LoginData implements Parcelable {

    public final static String LOGIN_SHARED_PREFERENCES_NAME = "hugoandrade.euro2016.LOGIN_SHARED_PREFERENCES_NAME";
    public final static String USERNAME = "hugoandrade.euro2016.USERNAME";
    public final static String PASSWORD = "hugoandrade.euro2016.PASSWORD";

    public static final String REQUEST_TYPE = "RequestType";
    public static final String OPERATION_TYPE_LOGIN = "Login";
    public static final String OPERATION_TYPE_SIGN_UP = "SignUp";

    public final static String TABLE_NAME = "Account";
    private final static String COL_NAME_USERNAME = "Username";
    private final static String COL_NAME_PASSWORD = "Password";

    public String username;
    public String password;

    public LoginData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public LoginData(Parcel in) {
        username = in.readString();
        password = in.readString();
    }

    public static final Creator<LoginData> CREATOR = new Creator<LoginData>() {
        @Override
        public LoginData createFromParcel(Parcel in) {
            return new LoginData(in);
        }

        @Override
        public LoginData[] newArray(int size) {
            return new LoginData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(password);
    }

    public JsonObject getAsJsonObject() {
        JsonObject newUser = new JsonObject();
        newUser.addProperty(LoginData.COL_NAME_USERNAME, username);
        newUser.addProperty(LoginData.COL_NAME_PASSWORD, password);
        return newUser;
    }
}
