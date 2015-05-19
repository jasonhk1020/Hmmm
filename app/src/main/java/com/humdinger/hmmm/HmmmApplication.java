package com.humdinger.hmmm;

import com.firebase.client.Firebase;

/**
 * Initialize Firebase with the application context. This must happen before the client is used.
 */
public class HmmmApplication extends android.app.Application {

    public HmmmApplication() {
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
