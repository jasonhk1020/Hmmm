package com.humdinger.hmmm;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
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

import com.firebase.client.Firebase;
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
    private ListView chatView;
    private ChatListAdapter mChatListAdapter;
    private EditText inputText;
    private ImageButton inputButton;
    private Firebase mFirebaseRef;
    private Boolean mBoolean;
    private TextView matchText;
    private RelativeLayout messageBar;

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
                final String matchUsername = connectionListItem.getMatchUsername();

                //set the chat room
                mFirebaseRef = new Firebase(mContext.getResources().getString(R.string.FIREBASE_URL)).child("chat").child(mRoom);
                mChatListAdapter = new ChatListAdapter(mFirebaseRef, ((Activity)mContext), R.layout.chat_message, mUsername);

                //chat view
                chatView = (ListView) ((Activity)mContext).findViewById(R.id.chatView);

                //fill the chatview
                chatView.setAdapter(mChatListAdapter);
                mChatListAdapter.registerDataSetObserver(new DataSetObserver() {
                    @Override
                    public void onChanged() {
                        super.onChanged();
                        chatView.setSelection(mChatListAdapter.getCount() - 1);
                    }
                });


                //relative layout for message bar
                messageBar = (RelativeLayout) ((Activity)mContext).findViewById(R.id.messageBar);

                //button placeholders
                inputButton = (ImageButton) ((Activity)mContext).findViewById(R.id.sendButton);
                inputButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendMessage(matchUid);
                    }
                });

                //input text handle hide key and send message (placeholder probable doesn't get kalled because on keycode_enter doesn't exist)
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
                //if deselecting the keyboard
                inputText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                    }
                });

                //set the username of the connection
                matchText = (TextView) ((Activity)mContext).findViewById(R.id.chat_matchUsername);
                matchText.setText(connectionListItem.getMatchUsername());

                //change the view of buttons
                mBoolean = false;
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
    public void onBindViewHolder(ConnectionListAdapter.ViewHolder viewHolder, int i) {
        ConnectionListItem connectionListItem = mConnectionListItems.get(i);
        viewHolder.setText(connectionListItem.getMatchUsername(), connectionListItem.getMatchInfo());
        viewHolder.setImage(connectionListItem.getPhotoUrl());

    }

    @Override
    public int getItemCount() {
        return mConnectionListItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView username;
        private TextView info;
        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.username_connection);
            info = (TextView) itemView.findViewById(R.id.info_connection);
            imageView = (ImageView) itemView.findViewById(R.id.image_connection);

        }

        public void setText(String username, CharSequence info) {
            this.username.setText(username);
            this.info.setText(info);
        }

        public void setImage(String photoUrl) {
            new LoadProfileImage(imageView).execute(photoUrl);
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

    private void sendMessage(String matchUid) {
        EditText inputText = (EditText) ((Activity)mContext).findViewById(R.id.messageInput);
        String input = inputText.getText().toString();
        if (!input.equals("")) {
            Chat chat = new Chat(input, mUsername);
            mFirebaseRef.push().setValue(chat);
            inputText.setText("");

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("message", input);
            params.put("uid",matchUid);
            params.put("username",mUsername);
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
}