package org.hugoandrade.euro2016.backend.object;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Calendar;

import org.hugoandrade.euro2016.backend.cloudsim.CloudDatabaseSimProvider;

public class SystemData implements Parcelable {

    @SuppressWarnings("unused") private static final String TAG = SystemData.class.getSimpleName();

    public static class Entry {
        public static final String TABLE_NAME = "SystemData";

        public static final String COLUMN__ID = "_id";
        public static final String COLUMN_RULES = "Rules";
        public static final String COLUMN_SYSTEM_DATE = "SystemDate";
        public static final String COLUMN_DATE_OF_CHANGE = "DateOfChange";
        public static final String COLUMN_APP_STATE = "AppState";

        // SQLite table mName
        // PATH & TOKEN for entire table
        public static final String PATH = TABLE_NAME;
        public static final int PATH_TOKEN = 190;

        // PATH & TOKEN for single row of table
        public static final String PATH_FOR_ID = PATH + "/#";
        public static final int PATH_FOR_ID_TOKEN = 200;

        // CONTENT/MIME TYPE for this content
        private final static String MIME_TYPE_END = PATH;
        public static final String CONTENT_TYPE_DIR = CloudDatabaseSimProvider.ORGANIZATIONAL_NAME
                + ".cursor.dir/" + CloudDatabaseSimProvider.ORGANIZATIONAL_NAME + "." + MIME_TYPE_END;
        public static final String CONTENT_ITEM_TYPE = CloudDatabaseSimProvider.ORGANIZATIONAL_NAME
                + ".cursor.item/" + CloudDatabaseSimProvider.ORGANIZATIONAL_NAME + "." + MIME_TYPE_END;
    }

    private int mID;
    public String rules;
    public boolean appState;
    public Calendar systemDate;
    public Calendar dateOfChange;

    public SystemData(int _id, String rules, boolean appState, Calendar systemDate, Calendar dateOfChange) {
        this.mID = _id;
        this.rules = rules;
        this.appState = appState;
        this.systemDate = systemDate;
        this.dateOfChange = dateOfChange;
    }

    public int getID() {
        return mID;
    }

    public String getRules() {
        return rules;
    }

    public boolean getAppState() {
        return appState;
    }

    public Calendar getSystemDate() {
        return systemDate;
    }

    public Calendar getDateOfChange() {
        return dateOfChange;
    }

    private SystemData(Parcel in) {
        Log.e(TAG, Boolean.toString(appState));
        mID = in.readInt();
        rules = in.readString();
        appState = in.readByte() != 0;
        systemDate = (Calendar) in.readSerializable();
        dateOfChange = (Calendar) in.readSerializable();
    }

    public static final Creator<SystemData> CREATOR = new Creator<SystemData>() {
        @Override
        public SystemData createFromParcel(Parcel in) {
            return new SystemData(in);
        }

        @Override
        public SystemData[] newArray(int size) {
            return new SystemData[size];
        }
    };

    @Override public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mID);
        dest.writeString(rules);
        dest.writeByte((byte) (appState ? 1 : 0));
        dest.writeSerializable(systemDate);
        dest.writeSerializable(dateOfChange);
        Log.e(TAG, Boolean.toString(appState));
    }

    public Calendar getDate() {
        Calendar c = Calendar.getInstance();
        long diff = c.getTimeInMillis() - dateOfChange.getTimeInMillis();
        c.setTimeInMillis(systemDate.getTimeInMillis() + diff);
        return c;
    }
}
