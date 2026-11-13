package com.blockit.detox.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.blockit.detox.LockSessionActivity
import com.blockit.detox.R
import com.blockit.detox.model.AppRepository
import kotlinx.coroutines.*

class SessionForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification(AppRepository.getRemainingSeconds(this))
        startForeground(NOTIFICATION_ID, notification)

        if (!isRunning) {
            isRunning = true
            serviceScope.launch {
                while (isActive) {
                    val remaining = AppRepository.getRemainingSeconds(applicationContext)
                    if (remaining <= 0) {
                        AppRepository.stopSession(applicationContext)
                        stopSelf()
                        break
                    }
                    val updatedNotification = buildNotification(remaining)
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.notify(NOTIFICATION_ID, updatedNotification)
                    delay(1000)
                }
            }
        }
        return START_STICKY
    }

    private fun buildNotification(remainingSec: Long): Notification {
        val hrs = remainingSec / 3600
        val mins = (remainingSec % 3600) / 60
        val secs = remainingSec % 60
        val timeStr = String.format("%02d:%02d:%02d", hrs, mins, secs)

        val intent = Intent(this, LockSessionActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("blockit · Focus Lock Active")
            .setContentText("Remaining: $timeStr · Monotonic Shield Active")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Lock Enforcement",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "blockit_session_channel"
        const val NOTIFICATION_ID = 2026
    }
}
