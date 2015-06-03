package com.humdinger.hmmm;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import org.apache.commons.lang3.text.WordUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    //for logging
    private static final String TAG = LoginActivity.class.getSimpleName();

    //Google Login
    public static final int RC_GOOGLE_LOGIN = 1;
    public static final int RC_GOOGLE_LOGOUT = 2;
    public GoogleApiClient mGoogleApiClient;
    private boolean mGoogleIntentInProgress;
    private boolean mGoogleLoginClicked;
    private ConnectionResult mGoogleConnectionResult;
    private SignInButton mGoogleLoginButton;
    private ProgressDialog mAuthProgressDialog;
    private Context mContext;

    //Firebase variables
    private Firebase mFirebaseRef;
    private AuthData mAuthData;
    private ValueEventListener mConnectedListener;

    //placeholder
    public LoginActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Create new google API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        //show the google sign in button
        mGoogleLoginButton = (SignInButton) findViewById(R.id.login_with_google);
        mGoogleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleLoginClicked = true;
                if (!mGoogleApiClient.isConnecting()) {
                    if (mGoogleConnectionResult != null) {
                        resolveSignInError();
                    } else if (mGoogleApiClient.isConnected()) {
                        getGoogleOAuthTokenAndLogin();
                    } else {
                        Log.d(TAG, "Trying to connect to Google API");
                        mGoogleApiClient.connect();
                    }
                }
            }
        });
        // Create the Firebase ref that is used for all authentication with Firebase */
        mFirebaseRef = new Firebase(getResources().getString(R.string.FIREBASE_URL));

        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading");
        mAuthProgressDialog.setCancelable(false);
        mAuthProgressDialog.show();

        mContext = this;

        mFirebaseRef.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                mAuthProgressDialog.hide();
                //go to function that will determine what to do if user is logged in or logged out
                setAuthenticatedUser(authData);
            }
        });

    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onConnected(final Bundle bundle) {
        /* Connected with Google API, use this to authenticate with Firebase */
        getGoogleOAuthTokenAndLogin();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mGoogleIntentInProgress) {
            /* Store the ConnectionResult so that we can use it later when the user clicks on the Google+ login button */
            mGoogleConnectionResult = result;

            if (mGoogleLoginClicked) {
                /* The user has already clicked login so we attempt to resolve all errors until the user is signed in,
                 * or they cancel. */
                resolveSignInError();
            } else {
                Log.e(TAG, result.toString());
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // ignore
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case RC_GOOGLE_LOGIN:
                if (resultCode != RESULT_OK) {
                    mGoogleLoginClicked = false;
                }
                mGoogleIntentInProgress = false;
                if (!mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }
                break;
            case RC_GOOGLE_LOGOUT:

                if (resultCode == RESULT_OK) {
                    //logic to determine if we need to forget the user
                    boolean forget = data.getBooleanExtra("forget", false);
                    if (this.mAuthData != null) {
                        mFirebaseRef.unauth();

                        if (this.mAuthData.getProvider().equals("google")) {

                            if (mGoogleApiClient.isConnected()) {

                                if (!forget) {
                                    //just logout
                                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                                    mGoogleApiClient.disconnect();
                                } else {
                                    //logout and forget user
                                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                                    Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                                            .setResultCallback(new ResultCallback<Status>() {
                                                @Override
                                                public void onResult(Status status) {
                                                    Log.e(TAG, "User access revoked!");
                                                    mGoogleApiClient.disconnect(); //this might be redundant
                                                    mAuthProgressDialog.show();
                                                }
                                            });
                                }
                            }
                        }
                    }
                } else {
                    mGoogleLoginClicked = true;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void getGoogleOAuthTokenAndLogin() {
        mAuthProgressDialog.show();
        /* Get OAuth token in Background */
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            String errorMessage = null;

            @Override
            protected String doInBackground(Void... params) {
                String token = null;

                try {
                    String scope = String.format("oauth2:%s", Scopes.PLUS_LOGIN);
                    token = GoogleAuthUtil.getToken(LoginActivity.this, Plus.AccountApi.getAccountName(mGoogleApiClient), scope);

                } catch (IOException transientEx) {
                    /* Network or server error */
                    Log.e(TAG, "Error authenticating with Google: " + transientEx);
                    errorMessage = "Network error: " + transientEx.getMessage();
                } catch (UserRecoverableAuthException e) {
                    Log.w(TAG, "Recoverable Google OAuth error: " + e.toString());
                    /* We probably need to ask for permissions, so start the intent if there is none pending */
                    if (!mGoogleIntentInProgress) {
                        mGoogleIntentInProgress = true;
                        Intent recover = e.getIntent();
                        startActivityForResult(recover, RC_GOOGLE_LOGIN);
                    }
                } catch (GoogleAuthException authEx) {
                    /* The call is not ever expected to succeed assuming you have already verified that
                     * Google Play services is installed. */
                    Log.e(TAG, "Error authenticating with Google: " + authEx.getMessage(), authEx);
                    errorMessage = "Error authenticating with Google: " + authEx.getMessage();
                }
                return token;
            }

            @Override
            protected void onPostExecute(String token) {
                mGoogleLoginClicked = false;
                if (token != null) {
                    /* Successfully got OAuth token, now login with Google */
                    mFirebaseRef.authWithOAuthToken("google", token, new AuthResultHandler("google"));

                    String email =  Plus.AccountApi.getAccountName(mGoogleApiClient);
                    //parse login
                    final HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("code", token);
                    params.put("email", email);

                    //loads the Cloud function to create a Google user in parse
                    ParseCloud.callFunctionInBackground("accessGoogleUser", params, new FunctionCallback<Object>() {
                        @Override
                        public void done(Object returnObj, ParseException e) {

                            if (e == null) {
                                ParseUser.becomeInBackground(returnObj.toString(), new LogInCallback() { //temporarily hardcoded
                                    public void done(ParseUser user, ParseException e) {
                                        if (user != null && e == null) {
                                            Log.i(TAG, "The Google user validated");

                                            // Associate the device with a user
                                            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                                            installation.put("user",user);
                                            installation.saveInBackground();

                                        } else if (e != null) {
                                            Toast.makeText(LoginActivity.this, "There was a problem creating your account.", Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                            mGoogleApiClient.disconnect();
                                        } else
                                            Log.i(TAG, "The Google token could not be validated");
                                    }
                                });
                            } else {
                                Toast.makeText(LoginActivity.this, "There was a problem creating your account.", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                                ParseErrorHandler.handleParseError(e);
                                mGoogleApiClient.disconnect();
                            }
                        }
                    });






                    token = null;
                } else if (errorMessage != null) {
                    mAuthProgressDialog.hide();
                    showErrorDialog(errorMessage);
                }
            }
        };
        task.execute();
    }




    private class AuthResultHandler implements Firebase.AuthResultHandler {

        private final String provider;

        public AuthResultHandler(String provider) {
            this.provider = provider;
        }

        @Override
        public void onAuthenticated(AuthData authData) {
            Log.i(TAG, provider + " auth successful");
            mAuthProgressDialog.hide();
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            mAuthProgressDialog.hide();
            showErrorDialog(firebaseError.toString());
        }
    }

    private void setAuthenticatedUser(AuthData authData) {
        if (authData != null) {
            //user is logged in
            if (authData.getProvider().equals("google")) {
                //setup shared preferences
                SharedPreferences prefs = getApplication().getSharedPreferences("userPrefs", 0);

                //establish map to input username and email
                Map<String, Object> map = new HashMap<String, Object>();

                //add uniqueid
                String uid = authData.getUid();
                map.put("uid", uid);
                prefs.edit().putString("uid",uid).commit();

                //add username
                String username = authData.getProviderData().get("displayName").toString();
                username = WordUtils.capitalizeFully(username);
                map.put("username", username);
                prefs.edit().putString("username", username).commit();

                //establish cacheduserprofile hash map to get the photo url
                Map<String,String> googleUserProfile = (HashMap<String,String>) authData.getProviderData().get("cachedUserProfile");
                //add photoUrl
                String photoUrl = googleUserProfile.get("picture");
                map.put("photoUrl", photoUrl);
                prefs.edit().putString("photoUrl", photoUrl).commit();

                //save data to firebase users
                mFirebaseRef.child("users").child(uid).updateChildren(map);

                //go to main activity
                Intent intent = new Intent(mContext, MainActivity.class);
                startActivityForResult(intent, RC_GOOGLE_LOGOUT);
                //moveTaskToBack(true);

            } else {
                Log.e(TAG, "Invalid provider: " + authData.getProvider());
            }
        } else {
            //user is not logged in
            Log.e(TAG, "Invalid authentication");
        }
        this.mAuthData = authData;
        supportInvalidateOptionsMenu();
    }
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    private void resolveSignInError() {
        if (mGoogleConnectionResult.hasResolution()) {
            try {
                mGoogleIntentInProgress = true;
                mGoogleConnectionResult.startResolutionForResult(this, RC_GOOGLE_LOGIN);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mGoogleIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }
}
