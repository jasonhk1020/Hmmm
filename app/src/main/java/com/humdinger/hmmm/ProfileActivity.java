package com.humdinger.hmmm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class ProfileActivity extends MenuActivity {

    private Firebase mFirebaseRef;

    private SharedPreferences prefs;
    public String username;
    public String uid;
    public String position;
    public String company;
    public String industry;
    public String description;
    public String photoUrl;

    public ProfileActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //get uid to locate user information on firebase
        prefs = getApplication().getSharedPreferences("userPrefs", 0);
        uid = prefs.getString("uid", null);

        //connect to user firebase
        mFirebaseRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("users").child(uid);
        mFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            // Retrieve new posts as they are added to Firebase
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                //retrieve current user info from firebase
                Map<String, String> map = (HashMap<String, String>) snapshot.getValue();
                username = removeNull(map.get("username"));
                position = removeNull(map.get("position"));
                company = removeNull(map.get("company"));
                industry = removeNull(map.get("industry"));
                description = removeNull(map.get("description"));
                photoUrl = removeNull(map.get("photoUrl"));

                //profile image from google
                ImageView imageView = (ImageView) findViewById(R.id.profile_image);
                new LoadProfileImage(imageView).execute(photoUrl);

                //username
                TextView usernameText = (TextView) findViewById(R.id.profile_name);
                usernameText.setText(username);

                //position
                setEditText("position", position, R.id.profile_position);

                //company
                setEditText("company", company, R.id.profile_company);

                //industry
                setEditText("industry", industry, R.id.profile_industry);

                //description
                setEditText("description", description, R.id.profile_description);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        //get the toolbar
        Toolbar menuToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(menuToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,MatchActivity.class);
        startActivity(intent);
        finish();
    }

    private void setEditText(final String field, final String initialValue, final int id) {
        final EditText editText = (EditText) findViewById(id);
        editText.setText(initialValue, TextView.BufferType.EDITABLE);
        if (!initialValue.equals("")) {
            editText.setBackgroundColor(getResources().getColor(R.color.transparent));
        }
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String updatedValue = editText.getText().toString();
                    prefs.edit().putString(field, updatedValue).commit();
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(field, updatedValue);
                    mFirebaseRef.updateChildren(map);
                    if (updatedValue.equals("")) {
                        editText.setBackgroundColor(getResources().getColor(R.color.cardBackground));
                    } else {
                        editText.setBackgroundColor(getResources().getColor(R.color.transparent));
                    }
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
    }

    private String removeNull(String string) {
        if (string == null) {
            string = "";
        }
        return string;
    }

    /**
     * Background Async task to load user profile picture from url
     * */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
