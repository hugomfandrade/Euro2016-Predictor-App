package org.hugoandrade.euro2016.predictor.utils;

/**
 * Class containing static helper methods to perform login related tasks.
 */
public final class LoginUtils {
    /**
     * Ensure this class is only used as a utility.
     */
    private LoginUtils() {
        throw new AssertionError();
    }

    /**
     * This method returns true if the password is at least 8 characters long.
     */
    public static boolean isPasswordAtLeast8CharactersLong(String password) {
        return password != null && password.length() >= 8;
    }

    /**
     * This method returns true if the password is not all spaces.
     */
    public static boolean isPasswordNotAllSpaces(String password) {
        return password != null && password.trim().length() > 0;
    }

    /**
     * This method returns true if the email address has an '@' sign.
     */
    public static boolean hasAnAtSign(String email) {
        return email != null && email.contains("@");
    }

    /**
     * This method returns true if the email address has at least one character
     * before the '@' sign.
     */
    public static boolean hasAtLeast1CharacterBeforeTheAtSign(String email) {
        return email != null && email.indexOf("@") >= 1;
    }

    /**
     * This method returns true if the email address has at least three characters
     * including a '.' isAfter the '@' sign.
     */
    public static boolean hasAtLeast3CharactersIncludingADotAfterTheAtSign(String email) {
        return email != null
                && email.substring(email.indexOf("@")).length() >= 3
                && email.substring(email.indexOf("@")).contains(".");
    }

    public static boolean isValid(String username, String password) {
        return LoginUtils.isPasswordAtLeast8CharactersLong(password)
                && LoginUtils.isPasswordNotAllSpaces(password)
                && LoginUtils.isPasswordNotAllSpaces(username);
    }
}
