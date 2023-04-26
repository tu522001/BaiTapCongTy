package com.example.b1.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val actionName = intent?.getStringExtra("myAcitonName")
        actionName?.let {
            when (it) {
                ACTION_PLAY -> actionPlaying?.playClicked()
                ACTION_NEXT -> actionPlaying?.nextClicked()
                ACTION_PREV -> actionPlaying?.prevClicked()
                else -> {}
            }
        }
        return START_STICKY
    }

    fun setCallBack(actionPlaying: ActionPlaying) {
        this.actionPlaying = actionPlaying
    }
}