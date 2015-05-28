package com.humdinger.hmmm;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jasonhk1020 on 5/26/2015.
 */

public class TabChat extends BackHandledFragment {

    private EditText inputText;
    private ImageButton inputButton;
    private ListView contactsView;
    private ListView chatView;
    private ChatListAdapter mChatListAdapter;
    private String mUsername;
    private String uid;
    private String matchUsername;
    private String matchUid;
    private TextView matchText;

    private Firebase memberRef;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ConnectionListAdapter mConnectionListAdapter;
    private ConnectionListItem mConnectionListItem;
    private Boolean mBoolean;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_chat,container,false);

        //get user info
        SharedPreferences prefs = getActivity().getSharedPreferences("userPrefs", 0);
        mUsername = prefs.getString("username", null);
        uid = prefs.getString("uid", null);

        // establish recycler view
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        //set up chatview only to hide
        chatView = (ListView) v.findViewById(R.id.chatView);

        //add notelist items to view
        mConnectionListAdapter = new ConnectionListAdapter(this.getActivity(), mRecyclerView, mUsername);
        mRecyclerView.setAdapter(mConnectionListAdapter);

        //get snapshot of everything!
        Firebase ref = new Firebase(getResources().getString(R.string.FIREBASE_URL));
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //now go loop through your connections
                for (DataSnapshot child : dataSnapshot.child("users").child(uid).child("connections").getChildren()) {

                    //set up the potential match's name and room number
                    if (dataSnapshot.child("users").child(child.getKey()).child("username").exists()) {

                        //get username
                        String matchUsername = dataSnapshot.child("users").child(child.getKey()).child("username").getValue().toString();

                        //get photourl
                        String photoUrl = dataSnapshot.child("users").child(child.getKey()).child("photoUrl").getValue().toString();

                        //initialize matchInfo details and format
                        Spannable sPosition = new SpannableString("");
                        Spannable sCompany = new SpannableString("");
                        Spannable sIndustry = new SpannableString("");
                        CharSequence matchInfo;

                        if (dataSnapshot.child("users").child(child.getKey()).child("position").exists()) {
                            sPosition = new SpannableString(dataSnapshot.child("users").child(child.getKey()).child("position").getValue().toString());
                            sPosition.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sPosition.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        if (dataSnapshot.child("users").child(child.getKey()).child("company").exists()) {
                            if (sPosition.length() != 0 && !dataSnapshot.child("users").child(child.getKey()).child("company").getValue().toString().equals("")) {
                                sCompany = new SpannableString(" at " + dataSnapshot.child("users").child(child.getKey()).child("company").getValue().toString());
                                sCompany.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 4, sCompany.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            } else {
                                sCompany = new SpannableString(dataSnapshot.child("users").child(child.getKey()).child("company").getValue().toString());
                                sCompany.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sCompany.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }
                        if (dataSnapshot.child("users").child(child.getKey()).child("industry").exists()) {
                            if (sPosition.length() != 0 || sCompany.length() != 0 && !dataSnapshot.child("users").child(child.getKey()).child("industry").getValue().toString().equals("")) {
                                sIndustry = new SpannableString(" in " + dataSnapshot.child("users").child(child.getKey()).child("industry").getValue().toString());
                                sIndustry.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 4, sIndustry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else {
                                sIndustry = new SpannableString(dataSnapshot.child("users").child(child.getKey()).child("industry").getValue().toString());
                                sIndustry.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sIndustry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }

                        if (sCompany.length() == 0 && sPosition.length() == 0 && sIndustry.length() == 0) {
                            matchInfo = "Profile Details Unavailable";
                        } else {
                            matchInfo = TextUtils.concat(sPosition, sCompany, sIndustry);
                        }

                        //initialize roomId
                        String roomId = null;


                        //if connection is true
                        if ((Boolean) child.getValue()) {

                            //let's assume you exist in their connections
                            DataSnapshot you = dataSnapshot.child("users").child(child.getKey()).child("connections").child(uid);

                            //check if you exist
                            if (you.exists()) {

                                //and make sure it's true
                                if ((Boolean) you.getValue()) {

                                    //refresh new room status for every new successful match
                                    boolean newRoom = true;

                                    //then check to see if you have a room together by looping through each member room
                                    for (DataSnapshot mChild : dataSnapshot.child("members").getChildren()) {

                                        //check to see if they and you are in there
                                        if (mChild.hasChild(child.getKey()) && mChild.hasChild(uid)) {

                                            //check to see if both are true
                                            if ((Boolean) mChild.child(child.getKey()).getValue() && (Boolean) mChild.child(uid).getValue()) {

                                                //assign matches name and room number
                                                roomId = mChild.getKey();

                                                //if roomid is not in the list then add it
                                                ArrayList<ConnectionListItem> arrayList = mConnectionListAdapter.getList();

                                                //deal with refreshes in the firebase, make sure that if we already have the recyclerview item not to create another one
                                                boolean newItem = true;
                                                for (ConnectionListItem i : arrayList) {

                                                    //check by roomid
                                                    if (i.getRoom().equals(roomId)) {
                                                        newItem = false;
                                                    }
                                                }

                                                //check if the item is already in the recyclerview, if it's new then go ahead and create a new item
                                                if (newItem) {

                                                    //create new list item based off username and uid
                                                    mConnectionListItem = new ConnectionListItem(matchUsername, roomId, matchInfo, photoUrl);

                                                    // Add the item to the adapter
                                                    mConnectionListAdapter.addItem(mConnectionListItem);

                                                }
                                                //don't need a new room, we already have one
                                                newRoom = false;
                                            }
                                        }

                                    }

                                    //we need a new room
                                    if (newRoom) {

                                        // let's assign a new room number (just length of members rooms + 1)
                                        roomId = String.valueOf(dataSnapshot.child("members").getChildrenCount() + 1);

                                        //add the people and room number to the members child
                                        Map<String, Object> members = new HashMap<String, Object>();
                                        members.put(uid, true);
                                        members.put(child.getKey(), true);
                                        memberRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("members");
                                        memberRef.child(roomId).setValue(members);

                                        // pass the user name and the room number both strings
                                        mConnectionListItem = new ConnectionListItem(matchUsername, roomId, matchInfo, photoUrl);

                                        // Add the item to the adapter
                                        mConnectionListAdapter.addItem(mConnectionListItem);

                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        //text placeholder
        inputText = (EditText) v.findViewById(R.id.messageInput);

        //button placeholder
        inputButton = (ImageButton) v.findViewById(R.id.sendButton);

        //usertext placeholder
        matchText = (TextView) v.findViewById(R.id.chat_matchUsername);

        //deal with display visibility
        mBoolean = true;
        setInvisible(mBoolean);

        //animate adding new item to view
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        return v;
    }

    private void setInvisible(boolean mBoolean) {
        if (mBoolean) {
            //show the connections list
            chatView.setVisibility(View.INVISIBLE);
            inputText.setVisibility(View.INVISIBLE);
            inputButton.setVisibility(View.INVISIBLE);
            matchText.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            //show the message dialog
            chatView.setVisibility(View.VISIBLE);
            inputText.setVisibility(View.VISIBLE);
            inputButton.setVisibility(View.VISIBLE);
            matchText.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public String getTagText() {
        return null;
    }

    @Override
    public boolean onBackPressed(){
        if(chatView.getVisibility() == View.INVISIBLE) {
            // go back to the the match activity by letting activity handle it
            return false;
        } else {
            setInvisible(true); //now show the match list display
            return true;
        }
    }
}