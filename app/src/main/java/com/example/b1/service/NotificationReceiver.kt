package com.example.b1.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.b1.service.ApplicationClass.ACTION_NEXT
import com.example.b1.service.ApplicationClass.ACTION_PLAY
import com.example.b1.service.ApplicationClass.ACTION_PREV

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val intent1 = Intent(context, MusicService::class.java)
        intent?.action?.let {
            when (it) {
                ACTION_PLAY -> {
                    Toast.makeText(context, "Play", Toast.LENGTH_SHORT).show()
                    intent1.putExtra("myAcitonName", intent.action)
                    context?.startService(intent1)
                }
                ACTION_NEXT -> {
                    Toast.makeText(context, "Next", Toast.LENGTH_SHORT).show()
                    intent1.putExtra("myAcitonName", intent.action)
                    context?.startService(intent1)
                }
                ACTION_PREV -> {
                    Toast.makeText(context, "Prev", Toast.LENGTH_SHORT).show()
                    intent1.putExtra("myAcitonName", intent.action)
                    context?.startService(intent1)
                }
                else -> {}
            }
        }
    }
}