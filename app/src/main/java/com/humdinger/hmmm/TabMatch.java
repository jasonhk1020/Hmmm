package com.humdinger.hmmm;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jasonhk1020 on 5/26/2015.
 */

public class TabMatch extends Fragment {

    private Query queryRef;
    private ChildEventListener queryRefListener;
    private CardContainer mDialogContainer;
    private CardContainer mCardContainer;
    private SimpleCardStackAdapter adapter;
    private DialogAdapter dialogAdapter;
    private SharedPreferences prefs;
    private String mUsername;
    private String uid;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.tab_match,container,false);

        //get the user shared preferences
        prefs = getActivity().getSharedPreferences("userPrefs", 0);
        mUsername = prefs.getString("username", null);
        uid = prefs.getString("uid", null);

        //setup text view
        TextView textView = (TextView) v.findViewById(R.id.match_empty_message);

        //create the card container
        mCardContainer = (CardContainer) v.findViewById(R.id.match_card_layout);
        mCardContainer.setOrientation(Orientations.Orientation.Ordered);
        mCardContainer.bringToFront();
        textView.invalidate();

        //create card container for diaglos
        mDialogContainer = (CardContainer) v.findViewById(R.id.match_dialog_layout);
        mDialogContainer.setOrientation(Orientations.Orientation.Ordered);
        mDialogContainer.bringToFront();
        mCardContainer.invalidate();
        textView.invalidate();

        //create adapters
        adapter = new SimpleCardStackAdapter(getActivity());
        dialogAdapter = new DialogAdapter(getActivity(), v);

        //set adapters to the view
        mCardContainer.setAdapter(adapter);
        mDialogContainer.setAdapter(dialogAdapter);


        //add match toolbar
        Toolbar matchToolbar = (Toolbar) v.findViewById(R.id.toolbar_match);
        matchToolbar.inflateMenu(R.menu.menu_match);
        matchToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                CardModel card;
                switch (menuItem.getItemId()) {
                    case R.id.action_match_previous:
                        if(!adapter.isEmpty()) {
                            //get the bottom card
                            card = adapter.getCardModel(adapter.getCount() - 1);
                            //move card to top
                            adapter.moveToTop(card);
                        }
                        return true;
                    case R.id.action_match_cancel:
                        if(!adapter.isEmpty()) {
                            //get the top card
                            card = adapter.getCardModel(0);
                            //remove it from the adapter view
                            adapter.remove(card);
                            //then add to back of stack
                            adapter.add(card);
                        }
                        return true;
                    case R.id.action_match_accept:
                        if(!adapter.isEmpty()) {
                            //get the top card
                            card = adapter.getCardModel(0);
                            //remove it from the adapter view completely
                            adapter.remove(card);
                            //add them to your connections list
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put(card.getUid(), true);


                            Firebase myRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("connections").child(uid);
                            myRef.updateChildren(map);
                        }
                        return true;
                    case R.id.action_match_settings:
                        Toast.makeText(getActivity(),"The settings menu is currently disabled.  We'll add search and filtering capability once there are enough users.",Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        return true;
                }
            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        //query the connections and order by your uid (null, then false, then true, then by alpha) just 50 results
        queryRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("connections").orderByChild(uid).limitToFirst(50);
        //listen for new children added
        queryRefListener = queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot connection, String s) {
                //make sure it's not you
                if(!connection.getKey().equals(uid)) {
                    //for each person, check to see if you are NOT mentioned
                    if (!connection.child(uid).exists()) {
                        //YOU are not mentioned!
                        //get your connections just once to get a quick look at your list
                        Firebase myRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("connections").child(uid);
                        myRef.addListenerForSingleValueEvent(new CustomValueEventListener(connection.getKey()) {
                            @Override
                            public void onDataChange(DataSnapshot myConnections) {
                                //since you are NOT mentioned check to see if you have NOT mentioned them, we wouldn't want to annoy you again if you already said yes to them
                                if(!myConnections.child(getUid()).exists()) {
                                    //since you have NOT mentioned them
                                    //first check list before you create a listener for their data
                                    if(!adapter.exists(getUid())) {
                                        //since they don't exist at this point, listen in on their data and update as data changes
                                        final Firebase matchRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("users").child(getUid());
                                        matchRef.addValueEventListener(new ValueEventListener() {
                                            // Retrieve new posts as they are added to Firebase
                                            @Override
                                            public void onDataChange(DataSnapshot snapshot) {

                                                ValueEventListener theListener = this;

                                                Map<String, Object> map = FormatSnapshot(snapshot);

                                                //let's add them to the adapter
                                                final CardModel cardModel = new CardModel(
                                                        (String) map.get("uid"),
                                                        (String) map.get("username"),
                                                        (CharSequence) map.get("info"),
                                                        (String) map.get("description"),
                                                        (String) map.get("photoUrl"));

                                                //check if the item already exists in the adapter, if not let's add it\
                                                if(!adapter.exists(snapshot.getKey())) {

                                                    //check your connections again to see if they are in your connections now
                                                    Firebase myLastRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("connections").child(uid);
                                                    myLastRef.addListenerForSingleValueEvent(new CustomValueEventListener(snapshot.getKey(), theListener) {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            //check to see if they exist in your connections one last time
                                                            if (dataSnapshot.child(getUid()).exists()) {
                                                                //they do exist, so let's not bother you once more and also, let's turn off this listener
                                                                matchRef.removeEventListener(getListener());
                                                            } else {
                                                                //since neither of you mention each other add the potential new match!
                                                                adapter.add(cardModel);
                                                            }
                                                        }
                                                        @Override
                                                        public void onCancelled(FirebaseError firebaseError) {
                                                        }
                                                    });
                                                } else {
                                                    //replace contents because info was updated
                                                    adapter.update(cardModel);
                                                }
                                            }
                                            @Override
                                            public void onCancelled(FirebaseError firebaseError) {
                                            }
                                        });
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                            }
                        });
                    } else { //since you do exist in their connections, probably means they said yes, but let's check anyways incase weird bug
                        //check to see if they said yes to you
                        if((Boolean) connection.child(uid).getValue()) {
                            //since they said yes to you, check to see if you have NOT mentioned them

                            //we'll check our list again once
                            Firebase myRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("connections").child(uid);
                            myRef.addListenerForSingleValueEvent(new CustomValueEventListener(connection.getKey()) {
                                @Override
                                public void onDataChange(DataSnapshot myConnections) {
                                    //do they even exist in my connections?
                                    if(!myConnections.child(getUid()).exists()) {
                                        //since you have NOT mentioned them
                                        //first check your dialog if they are on the list already
                                        if(!dialogAdapter.exists(getUid())) {

                                            //since they don't exist at this point, listen in on their data and update as data changes
                                            final Firebase matchRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("users").child(getUid());
                                            matchRef.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot snapshot) {

                                                    ValueEventListener theListener = this;
                                                    // Retrieve new posts as they are added to Firebase
                                                    Map<String, Object> map = FormatSnapshot(snapshot);

                                                    //let's add them to the adapter for displaying a popup dialog (notification too later on)
                                                    final DialogModel dialogModel = new DialogModel(
                                                            uid,
                                                            (String) map.get("uid"),
                                                            (String) map.get("username"),
                                                            (CharSequence) map.get("info"),
                                                            (String) map.get("description"),
                                                            (String) map.get("photoUrl"));

                                                    //check if the item already exists in the adapter, if not let's add it
                                                    if(!dialogAdapter.exists(snapshot.getKey())) {
                                                        //since the match isnt in your dialog

                                                        //check your connections again to see if they are in your connections now
                                                        Firebase myLastRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("connections").child(uid);
                                                        myLastRef.addListenerForSingleValueEvent(new CustomValueEventListener(snapshot.getKey(), theListener) {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                //check to see if they exist in your connections one last time
                                                                if (dataSnapshot.child(getUid()).exists()) {
                                                                    //they do exist, so let's not bother you once more and also, let's turn off this listener
                                                                    matchRef.removeEventListener(getListener());
                                                                } else {
                                                                    //they don't exist in your connections (no true or false value) so you should add them for dialog
                                                                    dialogAdapter.add(dialogModel);
                                                                }
                                                            }
                                                            @Override
                                                            public void onCancelled(FirebaseError firebaseError) {
                                                            }
                                                        });

                                                    } else {
                                                        //replace contents because info was updated
                                                        dialogAdapter.update(dialogModel);
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(FirebaseError firebaseError) {
                                                }
                                            });
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                }
                            });
                        }
                    }
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });


    }

    @Override
    public void onStop() {
        super.onStop();

        //loop through each person that's still on the list and remove the listener
        queryRef.removeEventListener(queryRefListener);
    }

    private String RemoveNull(String string) {
        if (string == null) {
            string = "";
        }
        return string;
    }

    public Map<String,Object> FormatSnapshot(DataSnapshot snapshot) {
        //retrieve current user info from firebase
        Map<String, String> map = (HashMap<String, String>) snapshot.getValue();
        String username = RemoveNull(map.get("username"));
        String position = RemoveNull(map.get("position"));
        String company = RemoveNull(map.get("company"));
        String industry = RemoveNull(map.get("industry"));
        String description = RemoveNull(map.get("description"));
        String photoUrl = RemoveNull(map.get("photoUrl"));
        String matchUid = RemoveNull(map.get("uid"));

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

        CharSequence info = TextUtils.concat(sPosition, sCompany, sIndustry);

        Map<String, Object> formattedMap = new HashMap<String, Object>();
        formattedMap.put("uid", matchUid);
        formattedMap.put("username", username);
        formattedMap.put("info", info);
        formattedMap.put("description", description);
        formattedMap.put("photoUrl", photoUrl);

        return formattedMap;
    }

}