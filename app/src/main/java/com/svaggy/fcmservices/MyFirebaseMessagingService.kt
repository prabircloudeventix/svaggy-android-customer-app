package com.svaggy.fcmservices

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.svaggy.R
import com.svaggy.ui.activities.SplashScreen
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        PrefUtils.instance.setString(Constants.DeviceToken,token)


    }

    @SuppressLint("SuspiciousIndentation")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if the message contains a notification payload.
        remoteMessage.notification?.let {
            // Handle notification data
            val title = it.title
            val body = it.body

            val  type = remoteMessage.data["type"].toString()
            val restaurantId = remoteMessage.data["restaurant_id"]?.toIntOrNull() ?: -1
            val broadcastId = remoteMessage.data["broadcast_id"]?.toIntOrNull() ?: -1

          val   orderId = remoteMessage.data["order_id"]
         val    reviewType = remoteMessage.data["review_type"]
         val    restaurantName = remoteMessage.data["restaurant_name"]





            // Display the notification
            sendNotification(title, remoteMessage.data.toString(),type,orderId,reviewType,restaurantName,body)
        }

    }


    private fun sendNotification(
        title: String?,
        body: String?,
        type: String,
        orderId: String?,
        reviewType: String?,
        restaurantName: String?,
        titleBody:String?
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "MyChannelId"

        // Create NotificationChannel for Android Oreo and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel Name",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notificationId = System.currentTimeMillis().toInt()


        val intent =  Intent(this, SplashScreen::class.java)
        intent.putExtra("type", type)
        intent.putExtra("body", body)
        intent.putExtra("order_id", orderId)
        intent.putExtra("review_type", reviewType)
        intent.putExtra("restaurant_name", restaurantName)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0 /* Request code */,
            intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)


        // Create a notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(titleBody)
            .setSmallIcon(R.drawable.ic_svaggy_notification1)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
            .setColor(ContextCompat.getColor(this@MyFirebaseMessagingService,R.color.primaryColor))
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Show the notification
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

}