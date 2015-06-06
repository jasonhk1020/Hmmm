package com.humdinger.hmmm;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.tab_match,container,false);


        //get the user shared preferences
        SharedPreferences prefs = getActivity().getSharedPreferences("userPrefs", 0);
        String mUsername = prefs.getString("username", null);
        final String uid = prefs.getString("uid", null);

        //setup text view
        TextView textView = (TextView) v.findViewById(R.id.match_empty_message);

        //create the card container
        final CardContainer mCardContainer = (CardContainer) v.findViewById(R.id.match_card_layout);
        mCardContainer.setOrientation(Orientations.Orientation.Ordered);
        mCardContainer.bringToFront();
        textView.invalidate();

        //create card container for diaglos
        final CardContainer mDialogContainer = (CardContainer) v.findViewById(R.id.match_dialog_layout);
        mDialogContainer.setOrientation(Orientations.Orientation.Ordered);
        mDialogContainer.bringToFront();
        mCardContainer.invalidate();
        textView.invalidate();

        //create adapters
        final SimpleCardStackAdapter adapter = new SimpleCardStackAdapter(getActivity());
        final DialogAdapter dialogAdapter = new DialogAdapter(getActivity(), v);

        //set adapters to the view
        mCardContainer.setAdapter(adapter);
        mDialogContainer.setAdapter(dialogAdapter);

        //we are going to find matches to add
        // get my connections
        final Firebase myRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("connections").child(uid);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //setup the myconnections dataSnapshot
                final DataSnapshot myConnections = dataSnapshot;

                //query the connections and order by your uid (null, then false, then true, then by alpha) just 50 results
                Query queryRef = new Firebase(getResources().getString(R.string.FIREBASE_URL)).child("connections").orderByChild(uid).limitToFirst(50);

                //listen for new children added
                queryRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot connection, String s) {

                        //make sure it's not you
                        if(!connection.getKey().equals(uid)) {

                            //for each person, check to see if you are NOT mentioned
                            if (!connection.child(uid).exists()) {

                                //since you are NOT mentioned check to see if you have NOT mentioned them
                                if(!myConnections.child(connection.getKey()).exists()) {

                                    //since you have NOT mentioned them

                                    //let's add them to the adapter
                                    CardModel cardModel = new CardModel(connection.getKey());

                                    //check if the item already exists in the adapater, if not let's add it
                                    if(!adapter.exists(cardModel)) {
                                        adapter.add(cardModel);
                                    }
                                }
                            } else { //since you exist in their connect... check the value

                                //if it's true
                                if((Boolean) connection.child(uid).getValue()) {

                                    //check to see if you have NOT mentioned them
                                    if(!myConnections.child(connection.getKey()).exists()) {

                                        //since you have NOT mentioned them

                                        //let's add them to the adapter for displaying a popup dialog (notification too later on)
                                        DialogModel dialogModel = new DialogModel(uid, connection.getKey());

                                        //check if the item already exists in the adapter, if not let's add it
                                        if(!dialogAdapter.exists(dialogModel)) {

                                            dialogAdapter.add(dialogModel);
                                        }
                                    }


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
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        CardModel cardModel = new CardModel(null);
        cardModel.setOnClickListener(new CardModel.OnClickListener() {
            @Override
            public void OnClickListener() {
                Log.i("Swipeable Cards","I am pressing the card");
            }
        });
        cardModel.setOnCardDismissedListener(new CardModel.OnCardDismissedListener() {
            @Override
            public void onLike() {
                Log.i("Swipeable Cards","I like the card");
            }

            @Override
            public void onDislike() {
                Log.i("Swipeable Cards","I dislike the card");
            }
        });

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

}