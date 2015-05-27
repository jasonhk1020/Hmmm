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

        //get snapshot of everything!
        Firebase ref = new Firebase(getResources().getString(R.string.FIREBASE_URL));
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //now go loop through your connections
                for (DataSnapshot child : dataSnapshot.child("users").child(uid).child("connections").getChildren()) {

                    //set up the potential match's name and room number
                    String matchUsername = dataSnapshot.child("users").child(child.getKey()).child("username").getValue().toString();
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
                                    if(mChild.hasChild(child.getKey()) && mChild.hasChild(uid)) {

                                        //check to see if both are true
                                        if((Boolean) mChild.child(child.getKey()).getValue() && (Boolean) mChild.child(uid).getValue()) {

                                            //assign matches name and room number
                                            roomId = mChild.getKey();

                                            //create new list item based off username and uid
                                            mConnectionListItem = new ConnectionListItem(matchUsername, roomId);

                                            // Add the item to the adapter
                                            mConnectionListAdapter.addItem(mConnectionListItem);

                                            //don't need a new room, we already assigned!
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
                                    members.put(matchUid, true);
                                    memberRef.child(roomId).setValue(members);

                                    // pass the user name and the room number both strings
                                    mConnectionListItem = new ConnectionListItem(matchUsername, roomId);

                                    // Add the item to the adapter
                                    mConnectionListAdapter.addItem(mConnectionListItem);

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