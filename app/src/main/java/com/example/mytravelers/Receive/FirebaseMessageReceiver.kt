package com.example.mytravelers.Receive

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.mytravelers.Activity.DashboardActivity
import com.example.mytravelers.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessageReceiver: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("TAG", "RefreshedToken"+token)
    }

    override fun onMessageReceived(message: RemoteMessage) {

            if (message.notification != null){
                pushNotification(message.notification!!.title, message.notification!!.body)
            }


    }

    private fun pushNotification(title: String?, message: String?) {
        // NotificationManager initialization
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Notification channel parameters
        val CHANNEL_ID = "push_noti"
        val name = "Custom Channel"
        val description = "Channel for Push Notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        // Intent for notification
        val iNotify = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(this, 100, iNotify, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Build the notification
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Notification channel creation (for Android O and above)
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }
            // Register the notification channel with the system
            nm.createNotificationChannel(channel)

            // Notification for Android O and above
            Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.travellogo)
                .setContentIntent(pi)
                .setContentTitle(title)
                .setSubText(message)
                .setAutoCancel(true)
                .build()
        } else {
            // Notification for devices below Android O
            Notification.Builder(this)
                .setSmallIcon(R.drawable.travellogo)
                .setContentIntent(pi)
                .setContentTitle(title)
                .setSubText(message)
                .setAutoCancel(true)
                .build()
        }

        // Notify using NotificationManager
        nm.notify(1, notification)
    }

}