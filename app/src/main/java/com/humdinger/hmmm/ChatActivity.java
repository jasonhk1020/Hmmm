package com.humdinger.hmmm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

public class ChatActivity extends MenuActivity {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get user info
        SharedPreferences prefs = getApplication().getSharedPreferences("userPrefs", 0);
        mUsername = prefs.getString("username", null);
        uid = prefs.getString("uid", null);

        //add menu toolbar
        Toolbar menuToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(menuToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // establish recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //set up chatview only to hide
        chatView = (ListView) findViewById(R.id.chatView);

        //add notelist items to view
        mConnectionListAdapter = new ConnectionListAdapter(this, mRecyclerView, mUsername);
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
        inputText = (EditText) findViewById(R.id.messageInput);

        //button placeholder
        inputButton = (ImageButton) findViewById(R.id.sendButton);

        //deal with display visibility
        mBoolean = true;
        setInvisible(mBoolean);

        //animate adding new item to view
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onStart() {
        super.onStart();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (mBoolean) {
            Intent intent = new Intent(this,ChatActivity.class);
            startActivity(intent);
            finish();
        } else {
            mBoolean = true;
            setInvisible(mBoolean);
        }

    }
}
