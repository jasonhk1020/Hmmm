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
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.Firebase;

import java.util.ArrayList;

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

    public void addItem(ConnectionListItem item) {
        mConnectionListItems.add(0, item);
        notifyItemInserted(0);
    }

    public void removeItem(int position) {
        mConnectionListItems.remove(position);
        notifyItemRemoved(position);
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

                //button placeholders
                inputButton = (ImageButton) ((Activity)mContext).findViewById(R.id.sendButton);
                inputButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendMessage();
                    }
                });

                //input text placeholder
                inputText = (EditText) ((Activity)mContext).findViewById(R.id.messageInput);
                inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                        if (keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                            InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(inputText.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                            sendMessage();
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
        viewHolder.setText(connectionListItem.getText());
    }

    @Override
    public int getItemCount() {
        return mConnectionListItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
        }

        public void setText(String text) {
            this.text.setText(text);
        }
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

    private void sendMessage() {
        EditText inputText = (EditText) ((Activity)mContext).findViewById(R.id.messageInput);
        String input = inputText.getText().toString();
        if (!input.equals("")) {
            Chat chat = new Chat(input, mUsername);
            mFirebaseRef.push().setValue(chat);
            inputText.setText("");
        }
    }
}