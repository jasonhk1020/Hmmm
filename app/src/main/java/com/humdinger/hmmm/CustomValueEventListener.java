package com.humdinger.hmmm;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by jasonhk1020 on 6/10/2015.
 */
public class CustomValueEventListener implements ValueEventListener{

    private String uid;
    private ValueEventListener listener;
    private CustomValueEventListener() {

    }

    public CustomValueEventListener(String uid, ValueEventListener listener) {
        this.uid = uid;
        this.listener = listener;
    }

    public CustomValueEventListener(String uid) {
        this.uid = uid;
    }

    public CustomValueEventListener(ValueEventListener listener) {
        this.listener = listener;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ValueEventListener getListener() {
        return listener;
    }

    public void setListener(ValueEventListener listener) {
        this.listener = listener;
    }

    public String getUid() {
        return uid;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
}
