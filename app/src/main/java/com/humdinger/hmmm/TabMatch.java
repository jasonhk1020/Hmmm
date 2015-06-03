package com.humdinger.hmmm;


import android.app.Dialog;
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
import android.view.Window;
import android.view.WindowManager;
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
    private int counter;
    private String matchUid;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v =inflater.inflate(R.layout.tab_match,container,false);

        SharedPreferences prefs = getActivity().getSharedPreferences("userPrefs", 0);
        mUsername = prefs.getString("username", null);
        uid = prefs.getString("uid", null);



        //call access to all firebase
        final Firebase allRef = new Firebase(getResources().getString(R.string.FIREBASE_URL));

        //my firebase
        mFirebaseRef = allRef.child("users").child(uid).child("connections");

        allRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //your connections
                connectionsList = (Map<String, Object>) dataSnapshot.child("users").child(uid).child("connections").getValue();

                //reinitialize every time something new happens in the user list
                matchUidList = new ArrayList<String>();
                counter = 0;

                //all users
                final Firebase userRef = allRef.child("users");
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //loop through each user
                        for (final DataSnapshot child : dataSnapshot.getChildren()) {

                            //if the user is not you
                            if (!child.getKey().equals(uid)) {

                                //check if you are in their connnection
                                if (child.child("connections").child(uid).exists()) {

                                    //if you exist and the value is true
                                    if (child.child("connections").child(uid).getValue() == true) {

                                        //check your connections to see if they exist in your connections
                                        if (dataSnapshot.child(uid).child("connections").child(child.getKey()).exists()) {

                                            //if value is true
                                            if (dataSnapshot.child(uid).child("connections").child(child.getKey()).getValue() == true) {

                                                //if value false
                                            } else if (dataSnapshot.child(uid).child("connections").child(child.getKey()).getValue() == false) {

                                            }
                                        } else {

                                            //show dialog
                                            final Dialog dialog = new Dialog(getActivity());
                                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                            dialog.setContentView(R.layout.floating_match);
                                            final Window window = dialog.getWindow();
                                            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

                                            Map<String, String> map = (HashMap<String, String>) child.getValue();
                                            String username = removeNull(map.get("username"));
                                            String position = removeNull(map.get("position"));
                                            String company = removeNull(map.get("company"));
                                            String industry = removeNull(map.get("industry"));
                                            String description = removeNull(map.get("description"));
                                            String photoUrl = removeNull(map.get("photoUrl"));

                                            //profile image from google
                                            ImageView imageView = (ImageView) dialog.findViewById(R.id.floating_match_image);
                                            new LoadProfileImage(imageView).execute(photoUrl);

                                            //name
                                            TextView usernameView = (TextView) dialog.findViewById(R.id.floating_match_username);
                                            usernameView.setText(removeNull(username));

                                            //position company and industry into sentence
                                            Spannable sPosition = new SpannableString("");
                                            Spannable sCompany = new SpannableString("");
                                            Spannable sIndustry = new SpannableString("");

                                            //logic for adding conjunctions and bolding
                                            if (!position.equals("")) {
                                                sPosition = new SpannableString(position);
                                                sPosition.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sPosition.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            }
                                            if (!company.equals("")) {
                                                if (!position.equals("")) {
                                                    sCompany = new SpannableString(" at " + company);
                                                    sCompany.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 4, sCompany.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                } else {
                                                    sCompany = new SpannableString(company);
                                                    sCompany.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sCompany.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                }
                                            }
                                            if (!industry.equals("")) {
                                                if (!position.equals("") || !company.equals("")) {
                                                    sIndustry = new SpannableString(" in " + industry);
                                                    sIndustry.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 4, sIndustry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                } else {
                                                    sIndustry = new SpannableString(industry);
                                                    sIndustry.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sIndustry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                }
                                            }

                                            //write info to view
                                            TextView positionCompanyIndustryView = (TextView) dialog.findViewById(R.id.floating_match_position_company_industry);
                                            positionCompanyIndustryView.setText(TextUtils.concat(sPosition, sCompany, sIndustry));

                                            //description
                                            TextView descriptionView = (TextView) dialog.findViewById(R.id.floating_match_description);
                                            descriptionView.setText(removeNull(description));


                                            //add match toolbar
                                            Toolbar floatingMatchToolbar = (Toolbar) dialog.findViewById(R.id.toolbar_floating_match);
                                            floatingMatchToolbar.inflateMenu(R.menu.menu_floating_match);
                                            floatingMatchToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                                                @Override
                                                public boolean onMenuItemClick(MenuItem menuItem) {
                                                    Map<String, Object> map;
                                                    switch (menuItem.getItemId()) {
                                                        case R.id.action_floating_match_cancel:
                                                            map = new HashMap<String, Object>();
                                                            map.put(child.getKey().toString(), false);
                                                            userRef.child(uid).child("connections").updateChildren(map);
                                                            dialog.dismiss();
                                                            return true;
                                                        case R.id.action_floating_match_accept:
                                                            map = new HashMap<String, Object>();
                                                            map.put(child.getKey().toString(), true);
                                                            userRef.child(uid).child("connections").updateChildren(map);
                                                            dialog.dismiss();
                                                            return true;
                                                        default:
                                                            return true;
                                                    }
                                                }
                                            });

                                            dialog.show();
                                        }

                                    }
                                } else {

                                    //check if you said yes to them if not, then add them to the list
                                    if (!dataSnapshot.child(uid).child("connections").child(child.getKey()).exists()) {
                                        //since you are not in their connnections...add them to the list (by the way even if you skipped them before you will see them again because you have not officially said NO to them when they say no to you
                                        matchUidList.add(child.getKey());

                                    }
                                }

                            }
                        }

                        //now that you've looped through all the views....deal with the first view or match (new)
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
                            //skipMatch(matchUidList.get(counter)); do nothing for now
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

                            if (matchUidList.size() == 1){
                                populateView(matchUidList.get(counter),v);
                            } else {

                                Toast.makeText(getActivity(), "There are no previous matches.", Toast.LENGTH_SHORT).show();

                            }

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

        //might need to change this to not say false and not even just mention their name until someone specifically asks them and just have it "skip"
    }

    private void sorryMatch(String matchUid) {
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
                    Spannable sPosition = new SpannableString("");
                    Spannable sCompany = new SpannableString("");
                    Spannable sIndustry = new SpannableString("");

                    //logic for adding conjunctions and bolding
                    if (!position.equals("")) {
                        sPosition = new SpannableString(position);
                        sPosition.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sPosition.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    if(!company.equals("")) {
                        if (!position.equals("")) {
                            sCompany = new SpannableString(" at " + company);
                            sCompany.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 4, sCompany.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else {
                            sCompany = new SpannableString(company);
                            sCompany.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sCompany.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                    if(!industry.equals("")) {
                        if (!position.equals("") || !company.equals("")) {
                            sIndustry = new SpannableString(" in " + industry);
                            sIndustry.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 4, sIndustry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else {
                            sIndustry = new SpannableString(industry);
                            sIndustry.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sIndustry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }

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