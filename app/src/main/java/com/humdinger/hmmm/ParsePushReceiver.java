package com.humdinger.hmmm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONObject;

/**
 * Created by jasonhk1020 on 6/4/2015.
 */
public class ParsePushReceiver extends ParsePushBroadcastReceiver {
    private static final String TAG = "ParsePushReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            String channel = intent.getExtras().getString("com.parse.Channel");
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            String sender = "from who";
            String message = "what did they say?";
            String senderUid = "googlewhat?";
            int notificationId = 0;
            if (json.has("type")) {
                if (json.getString("type").equals("messageNotification")) {
                    message = json.getString("message");
                    sender = json.getString("sender");
                    senderUid = json.getString("senderUid");
                    notificationId = Integer.parseInt(senderUid.substring(22));
                    generateNotification(context, "messageNotification", message, sender, senderUid, notificationId);
                } else if (json.getString("type").equals("requestNotification")) {
                    message = "would like to connect!";
                    sender = json.getString("sender");
                    senderUid = json.getString("senderUid");
                    notificationId = ParseConstants.ID_REQUEST;
                    generateNotification(context, "requestNotification", message, sender, senderUid, notificationId);
                } else if (json.getString("type").equals("acceptNotification")) {
                    message = "has connected with you!";
                    sender = json.getString("sender");
                    senderUid = json.getString("senderUid");
                    notificationId = ParseConstants.ID_ACCEPT;
                    generateNotification(context, "acceptNotification", message, sender, senderUid, notificationId);
                }
            }

        } catch (Exception e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }

    public static void generateNotification(Context context, String messageType, String message, String sender, String senderUid, int notificationId) {

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        // build the message notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher) //launcher icon
                        .setContentTitle(sender) //sender name as title
                        .setContentText(message) //sender message as title
                        .setAutoCancel(true)  //autocancel on click
                        .setGroup(ParseConstants.ID_NOTIFICATION_GROUP); //group notifications

        //for now no notification sounds and vibrates.
        mBuilder.setDefaults(0);

        //setup intent and pending intent
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.putExtra(messageType, true);
        resultIntent.putExtra("senderUid", senderUid);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        mBuilder.setContentIntent(resultPendingIntent);

        // allows you to update the notification later on. oh and notify the user, it'll keep updating the same notification btw which is good!
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        SharedPreferences statusPrefs = context.getSharedPreferences("statusPrefs", 0);
        Boolean opened = statusPrefs.getBoolean("opened", false);
        int position = statusPrefs.getInt("position", 0);
        String whoUid = statusPrefs.getString("whoUid", "");

        //check what type of notification this is
        if (notificationId == ParseConstants.ID_REQUEST) {
            //it's a match request
            if (opened) {
                //it's opened
                if (position == 0) {
                    //if it's on the match page, don't notify, because it should pop up automatically
                } else {
                    //since it's on a different page, still notify (in future there should be an in app drop down notification or something)
                    mNotificationManager.notify(notificationId, mBuilder.build());
                }
            } else {
                //since not opened, then create the notification so we can launch from there
                mNotificationManager.notify(notificationId, mBuilder.build());
            }
        } else if (notificationId == ParseConstants.ID_ACCEPT) {
            //it's a match accept, always show since we don't have an in-app dialog
            mNotificationManager.notify(notificationId, mBuilder.build());

        } else {
            //it's a message
            if (opened) {
                //since it's opened
                if (position != 1 || !whoUid.equals(senderUid)) {
                    //since it's not in the chat page or the current user isn't chatting with this person...create the notification
                    mNotificationManager.notify(notificationId, mBuilder.build());
                } else {
                    //don't notify, because we are already actively talking to this person
                }

            } else {
                //since not opened, then create the notification so we can launch from there
                mNotificationManager.notify(notificationId, mBuilder.build());
            }

        }



        /*
        //set default sound and vibrate
        mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE); //set defaults

        //checking user system prefs first ...
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        //if it's NOT in SILENT, then at least allow notification vibrate
        if(audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT){
            //TODO: we should also check for user's in-app preferences
            //add vibrate
            mBuilder.setVibrate(new long[] {500, 500}); //2 buzzes originally 500

        }
        //if it's in NORMAL, then also turn on notification sound
        if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL){
            //get default notification sound
            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
*/
    }

}