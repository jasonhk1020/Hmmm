package com.humdinger.hmmm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jasonhk1020 on 4/13/2015.
 */

public class ConnectionListAdapter extends RecyclerView.Adapter<ConnectionListAdapter.ViewHolder> {
    private Context mContext;
    private String mUsername;
    private RecyclerView mRecyclerView;
    private ArrayList<ConnectionListItem> mConnectionListItems = new ArrayList<ConnectionListItem>();
    private Firebase roomRef;

    private ListView chatView;
    private EditText inputText;
    private ImageButton inputButton;
    private RelativeLayout messageBar;
    private TextView matchText;

    public void addItem(ConnectionListItem item) {
        mConnectionListItems.add(0, item);
        notifyItemInserted(0);
    }

    public void removeItem(int position) {
        mConnectionListItems.remove(position);
        notifyItemRemoved(position);
    }

    public ArrayList<ConnectionListItem> getList() {
        return mConnectionListItems;
    }

    public ConnectionListAdapter(Context context, RecyclerView recyclerView, String mUsername) {
        this.mContext = context;
        this.mRecyclerView = recyclerView;
        this.mUsername = mUsername;
    }


    @Override
    public ConnectionListAdapter.ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.connection_list_item, viewGroup, false);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get room
                int mPosition = mRecyclerView.getChildPosition(v);
                ConnectionListItem connectionListItem = mConnectionListItems.get(mPosition);
                String mRoom = connectionListItem.getRoom();
                final String matchUid = connectionListItem.getMatchUid();

                //set the chat room and attach to adapter
                roomRef = new Firebase(mContext.getResources().getString(R.string.FIREBASE_URL)).child("chat").child(mRoom);
                final ChatListAdapter mChatListAdapter = new ChatListAdapter(roomRef, ((Activity)mContext), R.layout.chat_message, mUsername);

                //fill the chatview
                chatView = (ListView) ((Activity)mContext).findViewById(R.id.chatView);
                chatView.setAdapter(mChatListAdapter);
                mChatListAdapter.registerDataSetObserver(new DataSetObserver() {
                    @Override
                    public void onChanged() {
                        super.onChanged();
                        chatView.setSelection(mChatListAdapter.getCount() - 1);
                    }
                });

                //setup the match text
                matchText = (TextView) ((Activity)mContext).findViewById(R.id.chat_matchUsername);

                //need to ask firebase for the match's username!
                Firebase matchRef = new Firebase(mContext.getResources().getString(R.string.FIREBASE_URL)).child("users").child(connectionListItem.getMatchUid()).child("username");
                //just once
                matchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //setup the match username display
                        matchText.setText(dataSnapshot.getValue().toString());
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });


                //set the message bar layout for the buttons
                messageBar = (RelativeLayout) ((Activity)mContext).findViewById(R.id.messageBar);

                //setup the buttons and text input
                inputButton = (ImageButton) ((Activity)mContext).findViewById(R.id.sendButton);
                inputButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendMessage(matchUid);
                    }
                });
                inputText = (EditText) ((Activity)mContext).findViewById(R.id.messageInput);
                inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                        if (keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                            InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(inputText.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                        }
                        return true;
                    }
                });
                inputText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                    }
                });

                //change the view of buttons
                setInvisible(false);
            }
        });

        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v){
                return true;
            }
        });

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ConnectionListAdapter.ViewHolder viewHolder, int i) {
        ConnectionListItem connectionListItem = mConnectionListItems.get(i);

        Firebase matchRef = new Firebase(mContext.getResources().getString(R.string.FIREBASE_URL)).child("users").child(connectionListItem.getMatchUid());
        matchRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //get username
                String matchUsername = dataSnapshot.child("username").getValue().toString();

                //get photourl
                String photoUrl = dataSnapshot.child("photoUrl").getValue().toString();

                //initialize matchInfo details and format
                Spannable sPosition = new SpannableString("");
                Spannable sCompany = new SpannableString("");
                Spannable sIndustry = new SpannableString("");
                CharSequence matchInfo;
                if (dataSnapshot.child("position").exists()) {
                    sPosition = new SpannableString(dataSnapshot.child("position").getValue().toString());
                    sPosition.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sPosition.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (dataSnapshot.child("company").exists()) {
                    if (sPosition.length() != 0 && !dataSnapshot.child("company").getValue().toString().equals("")) {
                        sCompany = new SpannableString(" at " + dataSnapshot.child("company").getValue().toString());
                        sCompany.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 4, sCompany.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    } else {
                        sCompany = new SpannableString(dataSnapshot.child("company").getValue().toString());
                        sCompany.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sCompany.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                if (dataSnapshot.child("industry").exists()) {
                    if (sPosition.length() != 0 || sCompany.length() != 0 && !dataSnapshot.child("industry").getValue().toString().equals("")) {
                        sIndustry = new SpannableString(" in " + dataSnapshot.child("industry").getValue().toString());
                        sIndustry.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 4, sIndustry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        sIndustry = new SpannableString(dataSnapshot.child("industry").getValue().toString());
                        sIndustry.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sIndustry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                if (sCompany.length() == 0 && sPosition.length() == 0 && sIndustry.length() == 0) {
                    matchInfo = "Profile Details Unavailable";
                } else {
                    matchInfo = TextUtils.concat(sPosition, sCompany, sIndustry);
                }

                //update the view
                viewHolder.setText(matchUsername, matchInfo);
                viewHolder.setImage(photoUrl);

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mConnectionListItems.size();
    }

    public int getPosition(String senderUid) {
        int position = 0;

        if (mConnectionListItems.size() != 0) {
            for (ConnectionListItem i : mConnectionListItems) {
                if (i.getMatchUid().equals(senderUid)) {
                    break;
                }
                position++;
            }
        }else {
            position = -1;
        }

        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView usernameText;
        private TextView infoText;
        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            usernameText = (TextView) itemView.findViewById(R.id.username_connection);
            infoText = (TextView) itemView.findViewById(R.id.info_connection);
            imageView = (ImageView) itemView.findViewById(R.id.image_connection);

        }

        public void setText(String username, CharSequence info) {
            this.usernameText.setText(username);
            this.infoText.setText(info);

        }

        public void setImage(String photoUrl) {
            new LoadProfileImage(imageView).execute(photoUrl);
        }
    }

    public void setInvisible(boolean mBoolean) {
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

    public void sendMessage(String matchUid) {
        EditText inputText = (EditText) ((Activity)mContext).findViewById(R.id.messageInput);
        String input = inputText.getText().toString();
        if (!input.equals("")) {
            Chat chat = new Chat(input, mUsername);
            roomRef.push().setValue(chat);
            inputText.setText("");

            //get user info
            SharedPreferences prefs = mContext.getSharedPreferences("userPrefs", 0);
            String uid = prefs.getString("uid", null);

            String fixedMatchUid = matchUid.replace("google:","");
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("message", input); //the message
            params.put("uid",fixedMatchUid); //the person to send too, uid adjusted to remove the google:
            params.put("username",mUsername); //from name
            params.put("senderUid", uid); //from uid full with google
            ParseCloud.callFunctionInBackground("notification", params, new FunctionCallback<String>() {
                @Override
                public void done(String result, ParseException e) {
                    if (e == null) {
                        // result is "Hello world!"
                    }
                }
            });
        }
    }

    public boolean exists(ConnectionListItem item) {
        boolean found = false;
        for (ConnectionListItem i : mConnectionListItems) {
            if (i.getRoom() != null) {
                if (i.getRoom().equals(item.getRoom())) {
                    found = true;
                }
            }
        }
        return found;
    }

}