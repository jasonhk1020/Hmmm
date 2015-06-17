package com.humdinger.hmmm;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
    private RelativeLayout messageBar;

    private Firebase memberRef;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ConnectionListAdapter mConnectionListAdapter;
    private ConnectionListItem mConnectionListItem;
    private Boolean mBoolean;

    private Query queryRef;
    private ChildEventListener queryRefListener;
    private View v;
    private Query yourRef;
    private ChildEventListener yourRefListener;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.tab_chat,container,false);

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

        //text placeholder
        inputText = (EditText) v.findViewById(R.id.messageInput);

        //button placeholder
        inputButton = (ImageButton) v.findViewById(R.id.sendButton);

        //usertext placeholder
        matchText = (TextView) v.findViewById(R.id.chat_matchUsername);

        //deal with relative layout
        messageBar = (RelativeLayout) v.findViewById(R.id.messageBar);

        //deal with display visibility
        mBoolean = true;
        setInvisible(mBoolean);

        //animate adding new item to view
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        //query the connections ONLY if the child with your uid is set to true.  If they say yes to you, then you will know.
        queryRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("connections").orderByChild(uid).equalTo(true);
        queryRefListener = queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot connection, String s) {
                //make sure it's not you
                if(!connection.getKey().equals(uid)) {
                    //at this point we already know they said true to you based on the query
                    //now we need to look at your connections real quick and just once to see if you mention them or say yes
                    Firebase myRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("connections").child(uid);
                    myRef.addListenerForSingleValueEvent(new CustomValueEventListener(connection.getKey()) {
                        @Override
                        public void onDataChange(DataSnapshot myConnections) {
                            //for each person, check to see if you mention them
                            if (myConnections.child(getUid()).exists()) {
                                //make sure you said yes
                                if (myConnections.child(getUid()).getValue() == true) {
                                    //since you both said yes, let's make sure you have a room
                                    //just loop through all the rooms once
                                    Query roomRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("members");
                                    //look into all the rooms
                                    roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot rooms) {
                                            boolean newRoom = true;
                                            //loop through the rooms
                                            for (DataSnapshot room : rooms.getChildren()) {
                                                //if both of you are mentioned here (there are no other values but true, so yea!)
                                                if (room.child(uid).exists() && room.child(getUid()).exists()) {
                                                    newRoom = false; //we don't need a new room anymore
                                                    String roomId = room.getKey(); //let's add the room to the adapter
                                                    mConnectionListItem = new ConnectionListItem(room.getKey(), getUid()); //create the list item
                                                    //if it's not already on the list, then add it
                                                    if (!mConnectionListAdapter.exists(mConnectionListItem)) {
                                                        mConnectionListAdapter.addItem(mConnectionListItem);
                                                    }
                                                    break; //since we found it, no need to look elsewhere
                                                }
                                            }

                                            //check if you need a new room
                                            if (newRoom) {
                                                //yes we do!
                                                //add the people and room number to the members child
                                                Map<String, Object> members = new HashMap<String, Object>();
                                                members.put(uid, true);
                                                members.put(getUid(), true);

                                                //get the new room by pushing
                                                memberRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("members");
                                                Firebase newRoomRef = memberRef.push();
                                                newRoomRef.setValue(members); //add the data to the new room
                                                mConnectionListItem = new ConnectionListItem(newRoomRef.getKey(), getUid()); // pass the match user name and the room number both strings
                                                mConnectionListAdapter.addItem(mConnectionListItem); // Add the item to the adapter
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
        //check your connections for new true values
        yourRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("connections").child(uid).orderByValue().equalTo(true);
        yourRefListener = yourRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot myConnection, String s) {
                //check them if they said yes to you
                Firebase theirRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("connections").child(myConnection.getKey());
                theirRef.addListenerForSingleValueEvent(new CustomValueEventListener(myConnection.getKey()) {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(uid).exists()) {
                            //make sure you said yes
                            if (dataSnapshot.child(uid).getValue() == true) {
                                //since you both said yes, let's make sure you have a room
                                //just loop through all the rooms once

                                //check if you two have room together
                                Query roomRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("members");
                                //look into all the rooms
                                roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot rooms) {
                                        boolean newRoom = true;
                                        //loop through the rooms
                                        for (DataSnapshot room : rooms.getChildren()) {
                                            //if both of you are mentioned here (there are no other values but true, so yea!)
                                            if (room.child(uid).exists() && room.child(getUid()).exists()) {
                                                newRoom = false; //we don't need a new room anymore
                                                String roomId = room.getKey(); //let's add the room to the adapter
                                                mConnectionListItem = new ConnectionListItem(room.getKey(), getUid()); //create the list item
                                                //if it's not already on the list, then add it
                                                if (!mConnectionListAdapter.exists(mConnectionListItem)) {
                                                    mConnectionListAdapter.addItem(mConnectionListItem);
                                                }
                                                break; //since we found it, no need to look elsewhere
                                            }
                                        }

                                        //check if you need a new room
                                        if (newRoom) {
                                            //yes we do!
                                            //add the people and room number to the members child
                                            Map<String, Object> members = new HashMap<String, Object>();
                                            members.put(uid, true);
                                            members.put(getUid(), true);

                                            //get the new room by pushing
                                            memberRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("members");
                                            Firebase newRoomRef = memberRef.push();
                                            newRoomRef.setValue(members); //add the data to the new room
                                            mConnectionListItem = new ConnectionListItem(newRoomRef.getKey(), getUid()); // pass the match user name and the room number both strings
                                            mConnectionListAdapter.addItem(mConnectionListItem); // Add the item to the adapter
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
    public void onResume() {

        Intent intent = getActivity().getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Boolean isMessageNotification = extras.getBoolean("messageNotification");
            Boolean isAcceptNotification = extras.getBoolean("acceptNotification");
            if (isMessageNotification) {
                String senderUid = extras.getString("senderUid");
                int position = mConnectionListAdapter.getPosition(senderUid);

                //go to the chat!
                if (position != -1) {
                    mRecyclerView.findViewHolderForAdapterPosition(position).itemView.performClick();
                }

                //clear the intent
                intent.removeExtra("senderUid");
                intent.removeExtra("messageNotification");
            } else if(isAcceptNotification) {
                String senderUid = extras.getString("senderUid");
                int position = mConnectionListAdapter.getPosition(senderUid);

                //go to the chat!
                if (position != -1) {
                    mRecyclerView.findViewHolderForAdapterPosition(position).itemView.performClick();
                }

                //clear the intent
                intent.removeExtra("senderUid");
                intent.removeExtra("acceptNotification");
            }

        }

        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        //remove event listener
        queryRef.removeEventListener(queryRefListener);
        yourRef.removeEventListener(yourRefListener);

        //disconnect listeners and discard arraylists
        mConnectionListAdapter.cleanup();
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

    private void setInvisible(boolean mBoolean) {
        if (mBoolean) {
            //show the connections list
            chatView.setVisibility(View.INVISIBLE);
            inputText.setVisibility(View.INVISIBLE);
            inputButton.setVisibility(View.INVISIBLE);
            matchText.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
            messageBar.setVisibility(View.INVISIBLE);
        } else {
            //show the message dialog
            chatView.setVisibility(View.VISIBLE);
            inputText.setVisibility(View.VISIBLE);
            inputButton.setVisibility(View.VISIBLE);
            matchText.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
            messageBar.setVisibility(View.VISIBLE);
        }
    }

}