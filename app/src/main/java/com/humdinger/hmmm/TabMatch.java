package com.humdinger.hmmm;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jasonhk1020 on 5/26/2015.
 */

public class TabMatch extends Fragment {

    private View v;
    private Firebase mFirebaseRef;
    private Firebase matchList;
    private String mUsername;
    private String uid;

    private List<String> matchUidList;
    private Map<String,Object> connectionsList;
    private int counter = 0;
    private String matchUid;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v =inflater.inflate(R.layout.tab_match,container,false);

        SharedPreferences prefs = this.getActivity().getSharedPreferences("userPrefs", 0);
        mUsername = prefs.getString("username", null);
        uid = prefs.getString("uid", null);

        //call access to my firebase connections
        mFirebaseRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("users").child(uid).child("connections");
        mFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                connectionsList = (Map<String,Object>) dataSnapshot.getValue();
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        //get list of users (initialize)
        matchUidList = new ArrayList<String>();

        matchList = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("users");
        matchList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //compile list of all users
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    matchUidList.add(child.getKey());
                }

                // remove you from list
                matchUidList.remove(uid);

                // remove people you already talk to
                if (connectionsList != null){
                    for (Map.Entry<String, Object> entry : connectionsList.entrySet()) {
                        if ((Boolean) entry.getValue()) {
                            matchUidList.remove(entry.getKey());
                        }
                    }
                }

                //deal with the first view or match
                if (!matchUidList.isEmpty()) {
                    populateView(matchUidList.get(counter), v);
                } else {
                    //warn user there are no more people to see
                    populateView(null, v);

                }

            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        //add match toolbar
        Toolbar matchToolbar = (Toolbar) v.findViewById(R.id.toolbar_match);
        matchToolbar.inflateMenu(R.menu.menu_match);
        matchToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_match_cancel:
                        if (!matchUidList.isEmpty()) {
                            skipMatch(matchUidList.get(counter));
                            if (counter != matchUidList.size() - 1) {
                                counter = counter + 1;
                                populateView(matchUidList.get(counter), v);
                            } else {
                                populateView(null, v);
                            }
                        }
                        return true;
                    case R.id.action_match_accept:
                        if (!matchUidList.isEmpty()) {
                            pairMatch(matchUidList.get(counter));
                            if (counter != matchUidList.size() - 1) {
                                counter = counter + 1;
                                populateView(matchUidList.get(counter), v);
                            } else {
                                populateView(null, v);
                            }
                        }
                        return true;
                    case R.id.action_match_previous:
                        if (counter != 0) {
                            counter = counter - 1;
                            populateView(matchUidList.get(counter), v);
                        } else {
                            Toast.makeText(getActivity(), "There are no previous matches.", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    case R.id.action_match_settings:
                        Toast.makeText(getActivity(),"The settings menu is currently disabled.  We'll add search and filtering capability soon.",Toast.LENGTH_SHORT).show();
                    default:
                        return true;
                }
            }
        });

        return v;
    }
    private void skipMatch(String matchUid) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put(matchUid,false);
        mFirebaseRef.updateChildren(map);
    }
    private void pairMatch(String matchUid) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put(matchUid,true);
        mFirebaseRef.updateChildren(map);
    }

    protected void populateView(String matchUid,final View v) {
        // get uid
        if (matchUid != null) {
            Firebase matchRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("users").child(matchUid);
            matchRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    ImageView imageView = (ImageView) v.findViewById(R.id.match_image);
                    new LoadProfileImage(imageView).execute(photoUrl);

                    //name
                    TextView usernameView = (TextView) v.findViewById(R.id.match_username);
                    usernameView.setText(removeNull(username));

                    //position company and industry into sentence
                    Spannable sPosition = new SpannableString(position + " at ");
                    Spannable sCompany = new SpannableString(company + " in ");
                    Spannable sIndustry = new SpannableString(industry);
                    sPosition.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, position.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sCompany.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, company.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sIndustry.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, industry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    //write info to view
                    TextView positionCompanyIndustryView = (TextView) v.findViewById(R.id.match_position_company_industry);
                    positionCompanyIndustryView.setText(TextUtils.concat(sPosition, sCompany, sIndustry));

                    //description
                    TextView descriptionView = (TextView) v.findViewById(R.id.match_description);
                    descriptionView.setText(removeNull(description));

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        } else {
            //profile image from google
            ImageView imageView = (ImageView) v.findViewById(R.id.match_image);
            imageView.setImageResource(android.R.color.transparent);

            //name
            TextView usernameView = (TextView) v.findViewById(R.id.match_username);
            usernameView.setText("You've browsed through all the matches!");
            usernameView.setGravity(Gravity.CENTER);

            //write info to view
            TextView positionCompanyIndustryView = (TextView) v.findViewById(R.id.match_position_company_industry);
            positionCompanyIndustryView.setText("");

            //description
            TextView descriptionView = (TextView) v.findViewById(R.id.match_description);
            descriptionView.setText("");
        }
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