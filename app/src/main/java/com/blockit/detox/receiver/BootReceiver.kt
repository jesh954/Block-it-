package com.blockit.detox.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blockit.detox.LockSessionActivity
import com.blockit.detox.model.AppRepository

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (AppRepository.isSessionActive(context)) {
                val lockIntent = Intent(context, LockSessionActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(lockIntent)
            }
        }
    }
}
