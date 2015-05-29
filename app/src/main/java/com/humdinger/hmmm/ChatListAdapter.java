package com.humdinger.hmmm;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.client.Query;

/**
 * @author greg
 * @since 6/21/13
 *
 * This class is an example of how to use FirebaseListAdapter. It uses the <code>Chat</code> class to encapsulate the
 * data for each individual chat message
 */
public class ChatListAdapter extends FirebaseListAdapter<Chat> {

    // The mUsername for this client. We use this to indicate which messages originated from this user
    private String mUsername;

    public ChatListAdapter(Query ref, Activity activity, int layout, String mUsername) {
        super(ref, Chat.class, layout, activity);
        this.mUsername = mUsername;
    }

    /**
     * Bind an instance of the <code>Chat</code> class to our view. This method is called by <code>FirebaseListAdapter</code>
     * when there is a data change, and we are given an instance of a View that corresponds to the layout that we passed
     * to the constructor, as well as a single <code>Chat</code> instance that represents the current data to bind.
     *
     * @param view A view instance corresponding to the layout we passed to the constructor.
     * @param chat An instance representing the current state of a chat message
     */
    @Override
    protected void populateView(View view, Chat chat) {
        // Map a Chat object to an entry in our listview
        String author = chat.getAuthor();
        TextView messageText = (TextView) view.findViewById(R.id.message);
        LinearLayout wrapper = (LinearLayout) view.findViewById(R.id.wrapper);
        float scale = view.getResources().getDisplayMetrics().density;

        // If the message was sent by this user, color it differently
        if (author != null && author.equals(mUsername)) {
            //this is you
            messageText.setTextColor(view.getResources().getColor(R.color.textColorPrimary));
            messageText.setBackgroundResource(R.drawable.chatbubble_right);
            messageText.setPadding((int) (8*scale +0.5f),(int) (8*scale +0.5f),(int) (8*scale +5.5f),(int) (8*scale +0.5f)); //add 5 pixels more for right because of the triangle
            wrapper.setGravity(Gravity.RIGHT);

        } else {
            //this is others
            messageText.setTextColor(view.getResources().getColor(R.color.colorPrimaryDark));
            messageText.setBackgroundResource(R.drawable.chatbubble_left);
            messageText.setPadding((int) (8*scale +5.5f),(int) (8*scale +0.5f),(int) (8*scale +0.5f),(int) (8*scale +0.5f)); //add 5 pixels more for left because of the triangle
            wrapper.setGravity(Gravity.LEFT);
        }
        messageText.setText(chat.getMessage());


    }



}
