
/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mobileid;

import org.json.JSONObject;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    
    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GCM Demo Intent Service";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
//                sendNotification("Send error: " + extras.toString());
            	Log.i(TAG,"GCM Error");
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
//                sendNotification("Deleted messages on server: " + extras.toString());
            	Log.i(TAG,"GCM deleted on server");
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
        		Log.i(TAG, "Received: " + extras.getString("message"));
                sendNotification(intent);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(Intent intent) {
        mNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        
        Bundle extras = intent.getExtras();
        JSONObject gcmObj;
		try {
			gcmObj = new JSONObject(extras.getString("message"));
			String msg = gcmObj.getString("info");
			
	        Intent passIntent = new Intent();
			passIntent.setClass(this, MainActivity.class);
			passIntent.putExtra("gcmMsg", gcmObj.toString());            
		
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, passIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
			
			if(msg.compareToIgnoreCase("websign") != 0){
//		        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
		        mBuilder
				.setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("mobileID")
		        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
		        .setContentText(msg);
			} else {
//				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
				mBuilder
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("Web Sign - "+gcmObj.getString("title"))
		        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
		        .setContentText(gcmObj.getString("content"));
			}
			
			//default notification sound
	        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	        mBuilder.setSound(alarmSound);
	
	        //cleared after clicking
	        mBuilder.setAutoCancel(true);
	        
	        mBuilder.setContentIntent(contentIntent);
	        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}