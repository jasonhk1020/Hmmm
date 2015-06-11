package com.humdinger.hmmm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
            String matchMessage = "Hey!";
            if (json.has("message")) {
                sender = json.getString("sender");
                message = json.getString("message");
                senderUid = json.getString("senderUid");
                generateMessageNotification(context, sender, message, senderUid);
            } else if (json.has("requestSender")) {
                matchMessage = json.getString("requestSender") + " would like to connect.";
                generateRequestNotification(context, matchMessage);
            }

        } catch (Exception e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }

    public static void generateMessageNotification(Context context, String sender, String message, String senderUid) {

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
        resultIntent.putExtra("messageNotification", true);
        resultIntent.putExtra("senderUid", senderUid);

        String temp = senderUid.substring(22);
        int notificationId = Integer.parseInt(temp);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        mBuilder.setContentIntent(resultPendingIntent);

        // allows you to update the notification later on. oh and notify the user, it'll keep updating the same notification btw which is good!
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        mNotificationManager.notify(notificationId, mBuilder.build());

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

    public static void generateRequestNotification(Context context, String matchMessage) {

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        // build the message notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher) //launcher icon
                        .setContentTitle("Hello!") //sender name as title
                        .setContentText(matchMessage) //sender message as title
                        .setAutoCancel(true)  //autocancel on click
                        .setGroup(ParseConstants.ID_NOTIFICATION_GROUP); //group notifications

        //for now no notification sounds and vibrates.
        mBuilder.setDefaults(0);

        //setup intent and pending intent
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.putExtra("requestNotification", true);

        int notificationId = ParseConstants.ID_REQUEST;

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        mBuilder.setContentIntent(resultPendingIntent);

        // allows you to update the notification later on. oh and notify the user, it'll keep updating the same notification btw which is good!
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationId, mBuilder.build());

    }
}