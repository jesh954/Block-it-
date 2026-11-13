package com.blockit.detox.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.blockit.detox.LockSessionActivity
import com.blockit.detox.model.AppRepository

class SessionBlockAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val context = applicationContext
        if (!AppRepository.isSessionActive(context)) return

        val pkgName = event.packageName?.toString() ?: return

        // Allowed System UI: Calling, Emergency Dialing, or our own App package
        if (pkgName == applicationContext.packageName ||
            pkgName == "com.android.incallui" ||
            pkgName == "com.android.server.telecom" ||
            pkgName == "com.google.android.dialer") {
            return
        }

        // Check if package is in active Suitcase
        val activeBag = AppRepository.getActiveBag()
        if (activeBag.packages.contains(pkgName)) {
            Log.d("BlockitA11y", "Allowed package in Suitcase: $pkgName")
            return
        }

        // Intercept and bring LockSessionActivity back to front
        Log.w("BlockitA11y", "Blocked package: $pkgName. Relaunching LockSessionActivity.")
        val lockIntent = Intent(context, LockSessionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("BLOCKED_PKG", pkgName)
        }
        startActivity(lockIntent)
    }

    override fun onInterrupt() {
        Log.e("BlockitA11y", "Accessibility Service interrupted")
    }
}
