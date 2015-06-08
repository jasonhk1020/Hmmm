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

        //get snapshot of your connections
        Firebase myRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("connections").child(uid);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //setup the myconnections dataSnapshot
                final DataSnapshot myConnections = dataSnapshot;

                //query the connections and order by your uid (null, then false, then true, then by alpha) all of them
                Query queryRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("connections").orderByChild(uid).equalTo(true);
                queryRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(final DataSnapshot connection, String s) {

                        //make sure it's not you
                        if(!connection.getKey().equals(uid)) {

                            //for each person, check to see if you mention them
                            if (myConnections.child(connection.getKey()).exists()) {

                                //since you both said yes, let's make sure you have a room
                                Query roomRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("members");

                                //look into all the rooms
                                roomRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot rooms) {

                                        boolean newRoom = true;

                                        //loop through the rooms
                                        for (DataSnapshot room : rooms.getChildren()) {

                                            //if both of you are mentioned here
                                            if(room.child(uid).exists() && room.child(connection.getKey()).exists()) {
                                                //we don't need a new room anymore
                                                newRoom = false;

                                                //let's add the room to the adapter
                                                String roomId = room.getKey();

                                                //create the list item
                                                mConnectionListItem = new ConnectionListItem(room.getKey(), connection.getKey());

                                                //if it's not already on the list, then add it
                                                if(!mConnectionListAdapter.exists(mConnectionListItem)) {
                                                    mConnectionListAdapter.addItem(mConnectionListItem);
                                                }

                                                break;
                                            }
                                        }

                                        //check if you need a new room
                                        if(newRoom) {
                                            //yes we do!

                                            //add the people and room number to the members child
                                            Map<String, Object> members = new HashMap<String, Object>();
                                            members.put(uid, true);
                                            members.put(connection.getKey(), true);

                                            //get the new room by pushing
                                            memberRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("members");
                                            Firebase newRoomRef = memberRef.push();

                                            //add the data to the new room
                                            newRoomRef.setValue(members);

                                            // pass the match user name and the room number both strings
                                            mConnectionListItem = new ConnectionListItem(newRoomRef.getKey(), connection.getKey());

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
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


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

    @Override
    public void onResume() {

        Intent intent = getActivity().getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String senderUid = extras.getString("senderUid");
            int position = mConnectionListAdapter.getPosition(senderUid);

            //go to the chat!
            mRecyclerView.findViewHolderForAdapterPosition(position).itemView.performClick();
        }

        super.onResume();
    }

}