package com.humdinger.hmmm;

import com.firebase.client.Firebase;
import com.parse.Parse;
import com.parse.ParseInstallation;

/**
 * Initialize Firebase with the application context. This must happen before the client is used.
 * Initialize Parse with the application context.  This must happen before the client is used.
 */
public class HmmmApplication extends android.app.Application {

    public HmmmApplication() {
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);

        Parse.initialize(this, "anosokQZYDYl9RJkLFdZOZwgXjlThwKHXFZUXmCG", "v6AIkGOVQKkjZNRkC3KUgmKcRJRNYXI0vkYgsbVP");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
