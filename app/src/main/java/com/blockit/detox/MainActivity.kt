package com.blockit.detox

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blockit.detox.model.AppRepository
import com.blockit.detox.service.SessionForegroundService
import com.blockit.detox.views.BlockyMorphTimerView
import com.blockit.detox.views.RotaryWheelView

class MainActivity : AppCompatActivity() {

    private var selectedMinutes = 25

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppRepository.load(this)

        if (AppRepository.isSessionActive(this)) {
            startActivity(Intent(this, LockSessionActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val timerView = findViewById<BlockyMorphTimerView>(R.id.morphTimer)
        val rotaryWheel = findViewById<RotaryWheelView>(R.id.rotaryWheel)
        val btnLock = findViewById<Button>(R.id.btnLockPhone)
        val tvChutes = findViewById<TextView>(R.id.tvParachutes)
        val tvBag = findViewById<TextView>(R.id.tvActiveBag)

        tvChutes?.text = "🪂 ${AppRepository.parachutes}/2"
        tvBag?.text = AppRepository.getActiveBag().name

        timerView.setTime(selectedMinutes * 60L)
        rotaryWheel.setMinutes(selectedMinutes)

        rotaryWheel.onDurationChanged = { mins ->
            selectedMinutes = mins
            timerView.setTime(mins * 60L)
        }

        btnLock.setOnClickListener {
            // Check Accessibility Service permission
            val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
            if (!enabledServices.contains(packageName)) {
                Toast.makeText(this, "Please enable blockit Accessibility Service to lock your phone", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                return@setOnClickListener
            }

            AppRepository.startSession(this, selectedMinutes * 60L)
            startForegroundService(Intent(this, SessionForegroundService::class.java))
            startActivity(Intent(this, LockSessionActivity::class.java))
            finish()
        }
    }
}
