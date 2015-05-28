package com.humdinger.hmmm;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.Firebase;

import java.io.InputStream;
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
    private Boolean mBoolean;
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


                //set the username of the connection
                matchText = (TextView) ((Activity)mContext).findViewById(R.id.chat_matchUsername);
                matchText.setText(connectionListItem.getMatchUsername());

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

                //change the view of buttons
                mBoolean = false;
                setInvisible(false);

                //

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
        } else {
            //show the message dialog
            chatView.setVisibility(View.VISIBLE);
            inputText.setVisibility(View.VISIBLE);
            inputButton.setVisibility(View.VISIBLE);
            matchText.setVisibility(View.VISIBLE);
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

    /**
     * Background Async task to load user profile picture from url
     * */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}