package org.hugoandrade.euro2016.backend.model.parser;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;

import java.util.ArrayList;

import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.object.SystemData;

public class MessageBase {

    private static final String SYSTEM_DATA         = "SYSTEM_DATA";
    private static final String COUNTRY_DATA        = "COUNTRY_DATA";
    private static final String COUNTRY_LIST_DATA   = "COUNTRY_LIST_DATA";
    private static final String MATCH_DATA          = "MATCH_DATA";
    private static final String MATCH_LIST_DATA     = "MATCH_LIST_DATA";
    private static final String ERROR_MESSAGE       = "ERROR_MESSAGE";

    // Data Extras Key
    public static final int REQUEST_RESULT_FAILURE = 0;
    public static final int REQUEST_RESULT_SUCCESS = 1;

    public enum OperationType {
        @SuppressWarnings("unused") OPERATION_UNKNOWN,

        GET_ALL_COUNTRIES,
        GET_ALL_MATCHES,
        UPDATE_MATCH_UP,
        UPDATE_MATCH_RESULT,
        UPDATE_COUNTRY,
        GET_SYSTEM_DATA,
        SET_SYSTEM_DATA
    }

    /**
     * Message object
     */
    private Message mMessage;

    /**
     * Private constructor. Initializes Message
     */
    private MessageBase(Message message) {
        mMessage = message;
    }

    /**
     * Factory Method
     */
    public static MessageBase makeMessage(Message message) {
        return new MessageBase(Message.obtain(message));
    }

    /**
     * Factory Method
     */
    public static MessageBase makeMessage(int requestCode, int requestResult) {
        // Create a RequestMessage that holds a reference to a Message
        // created via the Message.obtain() factory method.
        MessageBase requestMessage = new MessageBase(Message.obtain());
        requestMessage.setData(new Bundle());
        requestMessage.setRequestCode(requestCode);
        requestMessage.setRequestResult(requestResult);

        // Return the message to the caller.
        return requestMessage;
    }

    /**
     * Factory Method
     */
    public static MessageBase makeMessage(int requestCode, Messenger messenger) {
        // Create a RequestMessage that holds a reference to a Message
        // created via the Message.obtain() factory method.
        MessageBase requestMessage = new MessageBase(Message.obtain());
        requestMessage.setData(new Bundle());
        requestMessage.setRequestCode(requestCode);
        requestMessage.setMessenger(messenger);

        // Return the message to the caller.
        return requestMessage;
    }

    public Message getMessage() {
        return mMessage;
    }

    /**
     * Sets provided Bundle as the data of the underlying Message
     * @param data - the Bundle to set
     */
    public void setData(Bundle data) {
        mMessage.setData(data);
    }

    /**
     * Accessor method that sets the result code
     * @param resultCode - the code tooset
     */
    public void setRequestCode(int resultCode) {
        mMessage.what = resultCode;
    }

    /**
     * Accessor method that returns the result code of the message, which
     * can be used to check if the download succeeded.
     */
    public int getRequestCode() {
        return mMessage.what;
    }

    /**
     * Accessor method that sets the result code
     * @param requestResult - the code to set
     */
    public void setRequestResult(int requestResult) {
        mMessage.arg1 = requestResult;
    }

    /**
     * Accessor method that returns the result code of the message, which
     * can be used to check if the download succeeded.
     */
    public int getRequestResult() {
        return mMessage.arg1;
    }

    /**
     * Accessor method that sets Messenger of the Message
     */
    public void setMessenger(Messenger messenger) {
        mMessage.replyTo = messenger;
    }

    /**
     * Accessor method that returns Messenger of the Message.
     */
    public Messenger getMessenger() {
        return mMessage.replyTo;
    }

    /**
     *
     */
    public void setSystemData(SystemData systemData) {
        mMessage.getData().putParcelable(SYSTEM_DATA, systemData);
    }

    /**
     *
     */
    public SystemData getSystemData() {
        return mMessage.getData().getParcelable(SYSTEM_DATA);
    }

    /**
     *
     */
    public void setCountry(Country country) {
        mMessage.getData().putParcelable(COUNTRY_DATA, country);
    }

    /**
     *
     */
    public Country getCountry() {
        return mMessage.getData().getParcelable(COUNTRY_DATA);
    }

    /**
     *
     */
    public void setCountryList(ArrayList<Country> countryList) {
        mMessage.getData().putParcelableArrayList(COUNTRY_LIST_DATA, countryList);
    }

    /**
     *
     */
    public ArrayList<Country> getCountryList() {
        return mMessage.getData().getParcelableArrayList(COUNTRY_LIST_DATA);
    }

    /**
     *
     */
    public void setMatch(Match match) {
        mMessage.getData().putParcelable(MATCH_DATA, match);
    }

    /**
     *
     */
    public Match getMatch() {
        return mMessage.getData().getParcelable(MATCH_DATA);
    }

    /**
     *
     */
    public ArrayList<Match> getMatchList() {
        return mMessage.getData().getParcelableArrayList(MATCH_LIST_DATA);
    }

    /**
     *
     */
    public void setMatchList(ArrayList<Match> matchList) {
        mMessage.getData().putParcelableArrayList(MATCH_LIST_DATA, matchList);
    }

    /**
     *
     */
    public void setErrorMessage(String errorMessage) {
        mMessage.getData().putString(ERROR_MESSAGE, errorMessage);
    }

    /**
     *
     */
    public String getErrorMessage() {
        return mMessage.getData().getString(ERROR_MESSAGE);
    }
}
