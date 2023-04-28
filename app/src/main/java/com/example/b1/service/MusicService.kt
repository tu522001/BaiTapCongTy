package com.example.b1.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.b1.R

class MusicService : Service() {

    private val mBinder: IBinder = MyBinder()
    companion object {
        const val ACTION_NEXT = "NEXT"
        const val ACTION_PREV = "PREVIOUS"
        const val ACTION_PLAY = "PLAY"
    }
    var actionPlaying: ActionPlaying? = null

    override fun onBind(intent: Intent?): IBinder? {
        Log.e("Bind", "Method")
        return mBinder
    }

    inner class MyBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val actionName = intent?.getStringExtra("myAcitonName")
//        actionName?.let {
//            when (it) {
//                ACTION_PLAY -> actionPlaying?.playClicked(startId)
//                ACTION_NEXT -> actionPlaying?.nextClicked(startId)
//                ACTION_PREV -> actionPlaying?.prevClicked(startId)
//                else -> {}
//            }
//        }
//        return START_STICKY
//    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val actionName = intent?.getStringExtra("myAcitonName")
        actionName?.let {
            when (it) {
                ACTION_PLAY -> actionPlaying?.playClicked(startId)
                ACTION_NEXT -> actionPlaying?.nextClicked(startId)
                ACTION_PREV -> actionPlaying?.prevClicked(startId)
                else -> {}
            }
        }

        // Create a notification channel for Android Oreo and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                ApplicationClass.CHANNEL_ID_2,
                "My Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.setSound(null, null)
            notificationChannel.enableVibration(false)
            notificationChannel.description = "My Service Channel Description" // add description
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Create a notification for the service
        val notification = NotificationCompat.Builder(this, ApplicationClass.CHANNEL_ID_2)
            .setContentTitle("My Service")
            .setContentText("Running...")
            .setSmallIcon(R.drawable.c)
            .build()

        // Start the service in the foreground with a notification
        startForeground(1, notification)

        // Return START_STICKY to keep the service running when the app is destroyed
        return START_STICKY
    }


    fun setCallBack(actionPlaying: ActionPlaying) {
        this.actionPlaying = actionPlaying
    }
}