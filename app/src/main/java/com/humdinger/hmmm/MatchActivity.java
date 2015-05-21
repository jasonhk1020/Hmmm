package com.humdinger.hmmm;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuItem;
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


public class MatchActivity extends MenuActivity{

    private Firebase mFirebaseRef;
    private Firebase matchList;
    private String mUsername;
    private String uid;

    private List<String> matchUidList;
    private Map<String,Object> connectionsList;
    private int counter = 0;
    private String matchUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        SharedPreferences prefs = getApplication().getSharedPreferences("userPrefs", 0);
        mUsername = prefs.getString("username", null);
        uid = prefs.getString("uid", null);

        //add menu toolbar
        Toolbar menuToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(menuToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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

                if (!matchUidList.isEmpty()) {
                    populateView(matchUidList.get(counter));
                } else {
                    //warn user there are no more people to see
                    populateView(null);

                }

            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        //add match toolbar
        Toolbar matchToolbar = (Toolbar) findViewById(R.id.toolbar_match);
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
                                populateView(matchUidList.get(counter));
                            } else {
                                populateView(null);
                            }
                        }
                        return true;
                    case R.id.action_match_accept:
                        if (!matchUidList.isEmpty()) {
                            pairMatch(matchUidList.get(counter));
                            if (counter != matchUidList.size() - 1) {
                                counter = counter + 1;
                                populateView(matchUidList.get(counter));
                            } else {
                                populateView(null);
                            }
                        }
                        return true;
                    case R.id.action_match_previous:
                        if (counter != 0) {
                            counter = counter - 1;
                            populateView(matchUidList.get(counter));
                        } else {
                            Toast.makeText(MatchActivity.this,"There are no previous matches.",Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    default:
                        return true;
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
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

    protected void populateView(String matchUid) {
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
                    ImageView imageView = (ImageView) findViewById(R.id.match_image);
                    new LoadProfileImage(imageView).execute(photoUrl);

                    //name
                    TextView usernameView = (TextView) findViewById(R.id.match_username);
                    usernameView.setText(removeNull(username));

                    //position company and industry into sentence
                    Spannable sPosition = new SpannableString(position + " at ");
                    Spannable sCompany = new SpannableString(company + " in ");
                    Spannable sIndustry = new SpannableString(industry);
                    sPosition.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, position.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sCompany.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, company.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sIndustry.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, industry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    //write info to view
                    TextView positionCompanyIndustryView = (TextView) findViewById(R.id.match_position_company_industry);
                    positionCompanyIndustryView.setText(TextUtils.concat(sPosition, sCompany, sIndustry));

                    //description
                    TextView descriptionView = (TextView) findViewById(R.id.match_description);
                    descriptionView.setText(removeNull(description));

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        } else {
            //profile image from google
            ImageView imageView = (ImageView) findViewById(R.id.match_image);
            imageView.setImageResource(android.R.color.transparent);

            //name
            TextView usernameView = (TextView) findViewById(R.id.match_username);
            usernameView.setText("You've browsed through all the matches!");

            //write info to view
            TextView positionCompanyIndustryView = (TextView) findViewById(R.id.match_position_company_industry);
            positionCompanyIndustryView.setText("");

            //description
            TextView descriptionView = (TextView) findViewById(R.id.match_description);
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
