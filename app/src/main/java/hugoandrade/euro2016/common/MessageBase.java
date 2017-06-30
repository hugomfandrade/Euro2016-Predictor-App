package hugoandrade.euro2016.common;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

import hugoandrade.euro2016.object.User;

public class MessageBase {

    private Message mMessage;

    public MessageBase(Message message) {
        this.mMessage = message;
    }

    public static MessageBase makeMessage(Message message) {
        return new MessageBase(Message.obtain(message));
    }

    public static MessageBase makeMessage(int requestCode, String requestResult) {
        // Create a RequestMessage that holds a reference to a Message
        // created via the Message.obtain() factory method.
        MessageBase requestMessage = new MessageBase(Message.obtain());
        requestMessage.setData(new Bundle());
        requestMessage.setRequestCode(requestCode);
        requestMessage.setRequestResult(requestResult);

        // Return the message to the caller.
        return requestMessage;
    }

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

    /**
     * Sets provided Bundle as the data of the underlying Message
     * @param data - the Bundle to set
     */
    public void setData(Bundle data) {
        mMessage.setData(data);
    }

    /**
     * Accessor method that sets the result code
     * @param resultCode - the code too set
     */
    public void setRequestCode(int resultCode) {
        mMessage.what = resultCode;
    }
    /**
     * Accessor method that sets the result code
     * @param requestResult - the code too set
     */
    public void setRequestResult(String requestResult) {
        mMessage.obj = requestResult;
    }

    /**
     * Accessor method that returns the result code of the message, which
     * can be used to check if the download succeeded.
     */
    public int getRequestCode() {
        return mMessage.what;
    }

    /**
     * Accessor method that returns the result code of the message, which
     * can be used to check if the download succeeded.
     */
    public String getRequestResult() {
        return (String) mMessage.obj;
    }

    public <T extends Parcelable> void putParcelableArrayList(String keyCode, ArrayList<T> parcelableList) {
        mMessage.getData().putParcelableArrayList(keyCode, parcelableList);
    }

    public void putString(String keyCode, String value) {
        mMessage.getData().putString(keyCode, value);
    }

    public void putBoolean(String keyCode, boolean value) {
        mMessage.getData().putBoolean(keyCode, value);
    }

    public <T extends Parcelable> void putParcelable(String keyCode, T parcelable) {
        mMessage.getData().putParcelable(keyCode, parcelable);
    }

    public void putSerializable(String keyCode, Serializable serializable) {
        mMessage.getData().putSerializable(keyCode, serializable);
    }

    /**
     * Accessor method that returns Messenger of the Message.
     */
    public Messenger getMessenger() {
        return mMessage.replyTo;
    }

    /**
     * Accessor method that sets Messenger of the Message
     * @param messenger
     */
    public void setMessenger(Messenger messenger) {
        mMessage.replyTo = messenger;
    }

    public Message getMessage() {
        return mMessage;
    }

    public <T extends Parcelable> T getParcelable(String keyCode) {
        return mMessage.getData().getParcelable(keyCode);
    }

    public String getString(String keyCode) {
        return mMessage.getData().getString(keyCode);
    }

    public void putInt(String keyCode, int value) {
        mMessage.getData().putInt(keyCode, value);
    }

    public int getInt(String keyCode) {
        return mMessage.getData().getInt(keyCode);
    }

    public <T extends Parcelable> ArrayList<T> getParcelableArrayList(String keyCode) {
        return mMessage.getData().getParcelableArrayList(keyCode);
    }
}
