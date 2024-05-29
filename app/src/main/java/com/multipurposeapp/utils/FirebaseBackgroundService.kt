package com.multipurposeapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.multipurposeapp.HomeActivity
import com.multipurposeapp.LoginActivity
import com.multipurposeapp.R

class FirebaseBackgroundService : Service() {

    private lateinit var myRef: DatabaseReference
    private lateinit var valueEventListener: ValueEventListener
    private val TAG = "FirebaseBackgroundService"
    private lateinit var notificationManager: NotificationManager
    private val channelId = "MyNotificationChannel"

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Create a database reference
        val database = FirebaseDatabase.getInstance()
        myRef = database.getReference("MEETINGS")
        Log.e(TAG,"Started")

        // Create a notification manager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "My Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create a ValueEventListener
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Handle data changes
                var updatedData:Any?=""
                for (childSnapshot in dataSnapshot.children) {
                    updatedData = childSnapshot.value
                    Log.e("TAG", "meetingId ${updatedData.toString()}")
                }
                Log.e(TAG,"updatedData: $updatedData")
                // Process the updated data
                // ...
                if (updatedData==""){

                }else {
                    showNotification(
                        "Your Class Started!",
                        (updatedData ?: "No data available").toString()
                    )
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        }

        // Attach the listener to the reference
        myRef.addValueEventListener(valueEventListener)
    }

    private fun showNotification(title: String, message: String) {
        val notificationIntent = Intent(this, LoginActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText("Please login to participate")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Show the notification
        notificationManager.notify(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Remove the listener when the service is destroyed
        myRef.removeEventListener(valueEventListener)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}