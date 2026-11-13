package com.blockit.detox

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blockit.detox.model.AppRepository
import com.blockit.detox.views.BlockyMorphTimerView

class LockSessionActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var holdStartMs = 0L
    private var isHolding = false

    private val tickerRunnable = object : Runnable {
        override fun run() {
            val remaining = AppRepository.getRemainingSeconds(this@LockSessionActivity)
            val timerView = findViewById<BlockyMorphTimerView>(R.id.lockMorphTimer)
            val progressBar = findViewById<ProgressBar>(R.id.glyphProgressBar)

            if (remaining <= 0) {
                AppRepository.stopSession(this@LockSessionActivity)
                Toast.makeText(this@LockSessionActivity, "Focus Session Complete! Floor added to Tower.", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            timerView?.setTime(remaining)
            val total = AppRepository.getTotalSessionSeconds(this@LockSessionActivity)
            val progress = ((remaining.toFloat() / total.toFloat()) * 100).toInt()
            progressBar?.progress = progress

            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_session)

        val btnParachute = findViewById<Button>(R.id.btnHoldParachute)
        val tvBlocked = findViewById<TextView>(R.id.tvBlockedNotice)

        val blockedPkg = intent.getStringExtra("BLOCKED_PKG")
        if (blockedPkg != null) {
            tvBlocked?.text = "🚫 Blocked: $blockedPkg"
        }

        btnParachute?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    holdStartMs = System.currentTimeMillis()
                    isHolding = true
                    checkHoldProgress()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isHolding = false
                    true
                }
                else -> false
            }
        }
    }

    private fun checkHoldProgress() {
        if (!isHolding) return
        val elapsed = System.currentTimeMillis() - holdStartMs
        if (elapsed >= 3000) {
            isHolding = false
            if (AppRepository.parachutes > 0) {
                AppRepository.parachutes -= 1
                AppRepository.penaltyDays += 3
                AppRepository.save(this)
                AppRepository.stopSession(this)
                Toast.makeText(this, "Parachute Deployed! Session Ended.", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "0 Parachutes! Cognitive challenge required.", Toast.LENGTH_LONG).show()
            }
        } else {
            handler.postDelayed({ checkHoldProgress() }, 100)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(tickerRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tickerRunnable)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent back press from dismissing lock screen during active session
    }
}
