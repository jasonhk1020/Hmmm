package com.humdinger.hmmm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
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
            if (json.has("sender")) {
                sender = json.getString("sender");
                message = json.getString("message");
                senderUid = json.getString("senderUid");
                generateMessageNotification(context, sender, message, senderUid);
            }

        } catch (Exception e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }

    public static void generateMessageNotification(Context context, String sender, String message, String senderUid) {
        // build the message notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher) //launcher icon
                        .setContentTitle(sender) //sender name as title
                        .setContentText(message) //sender message as title
                        .setAutoCancel(true);  //autocancel on click

        //set default sound and vibrate
        mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE); //set defaults

        //checking user system prefs first ...
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        //if it's NOT in SILENT, then at least allow notification vibrate
        if(audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT){
            //TODO: we should also check for user's in-app preferences

            //add vibrate
            mBuilder.setVibrate(new long[] {500, 500}); //2 buzzes
        }
        //if it's in NORMAL, then also turn on notification sound
        if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL){
            //get default notifification sound
            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }



        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.putExtra("messageNotification", true);
        resultIntent.putExtra("senderUid", senderUid);


/*
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MainActivity.class);

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);*/

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);


        // allows you to update the notification later on. oh and notify the user, it'll keep updating the same notification btw which is good!
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(ParseConstants.ID_MESSAGE, mBuilder.build());
    }
}