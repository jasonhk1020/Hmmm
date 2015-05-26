package com.humdinger.hmmm;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Created by jasonhk1020 on 5/26/2015.
 */

public class TabProfile extends Fragment {

    private View v;
    private SharedPreferences prefs;
    private Firebase mFirebaseRef;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v =inflater.inflate(R.layout.tab_profile,container,false);



        //get uid to locate user information on firebase
        final SharedPreferences prefs = this.getActivity().getSharedPreferences("userPrefs", 0);
        String uid = prefs.getString("uid", null);

        //connect to user firebase
        mFirebaseRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("users").child(uid);
        mFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            // Retrieve new posts as they are added to Firebase
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                //retrieve current user info from firebase
                Map<String, String> map = (HashMap<String, String>) snapshot.getValue();
                String username = removeNull(map.get("username"));
                String position = removeNull(map.get("position"));
                String company = removeNull(map.get("company"));
                String industry = removeNull(map.get("industry"));
                String description = removeNull(map.get("description"));
                String photoUrl = removeNull(map.get("photoUrl"));

                //profile image from google
                ImageView imageView = (ImageView) v.findViewById(R.id.profile_image);
                new LoadProfileImage(imageView).execute(photoUrl);

                //username
                TextView usernameText = (TextView) v.findViewById(R.id.profile_name);
                usernameText.setText(username);

                //position
                setEditText("position", position, R.id.profile_position, v);

                //company
                setEditText("company", company, R.id.profile_company, v);

                //industry
                setEditText("industry", industry, R.id.profile_industry, v);

                //description
                setEditText("description", description, R.id.profile_description, v);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });



        return v;
    }
    private void setEditText(final String field, final String initialValue, final int id, final View v) {
        final EditText editText = (EditText) v.findViewById(id);
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
                    /*InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);*/
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