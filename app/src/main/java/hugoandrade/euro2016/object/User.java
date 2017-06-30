package hugoandrade.euro2016.object;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonObject;
import hugoandrade.euro2016.utils.NetworkUtils;

public class User implements Parcelable {


    public static final String TABLE_NAME = "Account";
    public final static String COL_NAME_ID = "_id";
    public final static String COL_NAME_USERNAME = "Username";
    public final static String COL_NAME_PASSWORD = "Password";
    public final static String COL_NAME_SCORE = "Score";

    public final String id;
    public final String username;
    public final String password;
    public final int score;

    public User(String id, String username, String password, int score) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.score = score;
    }

    public static User instanceFromJsonObject(JsonObject jsonObject) {
        return new User(
                NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_ID, null),
                NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_USERNAME, null),
                NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_PASSWORD, null),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_SCORE, -1));
    }

    protected User(Parcel in) {
        id = in.readString();
        username = in.readString();
        password = in.readString();
        score = in.readInt();
    }

    public static final Parcelable.Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(username);
        dest.writeString(password);
        dest.writeInt(score);
    }
}
