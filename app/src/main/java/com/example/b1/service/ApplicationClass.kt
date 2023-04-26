package com.example.b1.service

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class ApplicationClass : Application() {
    companion object{
        const val CHANNEL_ID_1 = "CHANNEL_1"
        const val CHANNEL_ID_2 = "CHANNEL_2"
        const val ACTION_NEXT = "NEXT"
        const val ACTION_PREV = "PREVIOUS"
        const val ACTION_PLAY = "PLAY"
    }


    override fun onCreate() {
        super.onCreate()
        creatNotificationChannel()

    }

    private fun creatNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel1 = NotificationChannel(CHANNEL_ID_1, "channel (1)", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel1.description = "channel 1 Description"
            val notificationChannel2 = NotificationChannel(CHANNEL_ID_2, "channel (2)", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel1.description = "channel 2 Description"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel1)
            notificationManager.createNotificationChannel(notificationChannel2)
        }
    }
}