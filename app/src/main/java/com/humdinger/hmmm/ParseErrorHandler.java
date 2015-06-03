package com.humdinger.hmmm;

import com.parse.ParseException;

/**
 * Created by jasonhk1020 on 6/3/2015.
 */
public class ParseErrorHandler {
    private static final int INVALID_SESSION_TOKEN = 209;

    public static void handleParseError(ParseException e) {
        switch (e.getCode()) {
            case INVALID_SESSION_TOKEN:
                handleInvalidSessionToken();
                break;
        }
    }

    private static void handleInvalidSessionToken() {
        //--------------------------------------
        // Option 1: Show a message asking the user to log out and log back in.
        //--------------------------------------
        // If the user needs to finish what they were doing, they have the opportunity to do so.
        //
        // new AlertDialog.Builder(getActivity())
        //   .setMessage("Session is no longer valid, please log out and log in again.")
        //   .setCancelable(false).setPositiveButton("OK", ...).create().show();

        //--------------------------------------
        // Option #2: Show login screen so user can re-authenticate.
        //--------------------------------------
        // You may want this if the logout button could be inaccessible in the UI.
        //
        // startActivityForResult(new ParseLoginBuilder(getActivity()).build(), 0);
    }
}