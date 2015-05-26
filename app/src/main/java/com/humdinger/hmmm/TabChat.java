package com.humdinger.hmmm;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jasonhk1020 on 5/26/2015.
 */

public class TabChat extends Fragment {

    private EditText inputText;
    private ImageButton inputButton;
    private ListView contactsView;
    private ListView chatView;
    private ChatListAdapter mChatListAdapter;
    private String mUsername;
    private String uid;
    private String matchUsername;
    private String matchUid;

    private Firebase mainRef;
    private Firebase connectRef;
    private Firebase matchRef;
    private Firebase mFirebaseRef;
    private Firebase userRef;
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
        SharedPreferences prefs = this.getActivity().getSharedPreferences("userPrefs", 0);
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

        // look through the user's info
        mainRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("users").child(uid);

        //traverse down into user's connections
        connectRef = mainRef.child("connections");
        connectRef.addChildEventListener(new ChildEventListener() { //listen for when the there is child activity
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //loop through your connections
                //if the connection is at a true state
                if ((Boolean) dataSnapshot.getValue()) {

                    //get the match's firebase connections
                    matchRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("users").child(dataSnapshot.getKey());
                    matchRef.addListenerForSingleValueEvent(new ValueEventListener() { //take a quick look to see if the connection has accepted you
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //username and id
                            matchUsername = dataSnapshot.child("username").getValue().toString();
                            matchUid = dataSnapshot.child("uid").getValue().toString();
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });

                    //traverse into the connection and look for their matches
                    matchRef = matchRef.child("connections");
                    matchRef.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            //loop through match's connections
                            //if you are found and connection is at a true state
                            if (dataSnapshot.getKey().equals(uid) && (Boolean) dataSnapshot.getValue())  {
                                //now check to see if both users are in a room under members just once
                                memberRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("members");
                                memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //logic to see if we need to assign a room number
                                        boolean newRoom = true;

                                        //loop through each member room
                                        for (DataSnapshot child : dataSnapshot.getChildren()) {

                                            // determine if we already have a room for the two of you
                                            if (child.hasChild(uid) && child.hasChild(matchUid)){

                                                // this is great, we already have a room so let's just use that.  pass the user name and the room number both strings
                                                mConnectionListItem = new ConnectionListItem(matchUsername, child.getKey());

                                                // Add the item to the adapter
                                                mConnectionListAdapter.addItem(mConnectionListItem);

                                                //since we have a room, we don't need to specify a new one
                                                newRoom = false;
                                            }
                                        }

                                        // assign a new room?
                                        if (newRoom) {
                                            // let's setup a new room (just length of members rooms and + 1
                                            String room = String.valueOf(dataSnapshot.getChildrenCount() + 1);

                                            //add the people and room number to the members child
                                            Map<String, Object> members = new HashMap<String, Object>();
                                            members.put(uid, true);
                                            members.put(matchUid, true);
                                            memberRef.child(room).setValue(members);

                                            // pass the user name and the room number both strings
                                            mConnectionListItem = new ConnectionListItem(matchUsername, room);

                                            // Add the item to the adapter
                                            mConnectionListAdapter.addItem(mConnectionListItem);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(FirebaseError firebaseError) {
                                    }
                                });
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

        //text placeholder
        inputText = (EditText) v.findViewById(R.id.messageInput);

        //button placeholder
        inputButton = (ImageButton) v.findViewById(R.id.sendButton);

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
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            //show the message dialog
            chatView.setVisibility(View.VISIBLE);
            inputText.setVisibility(View.VISIBLE);
            inputButton.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
        }
    }
}