package com.blockit.detox.model

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class SuitcaseBag(
    val name: String,
    val packages: MutableList<String>
)

data class TowerFloor(
    val day: String,
    val hours: Float,
    val sessions: Int
)

data class Routine(
    val id: String,
    val title: String,
    val bagIndex: Int,
    val start: String, // "HH:MM"
    val hours: Int,
    val gap: String, // "none" or "mid"
    var enabled: Boolean
)

object AppRepository {
    private const val PREFS_NAME = "blockit_state_prefs"
    private const val KEY_PARACHUTES = "parachutes"
    private const val KEY_STREAK_DAYS = "streak_days"
    private const val KEY_PENALTY_DAYS = "penalty_days"
    private const val KEY_CHUTES_USED = "chutes_used"
    private const val KEY_BAGS_JSON = "bags_json"
    private const val KEY_FLOORS_JSON = "floors_json"
    private const val KEY_ROUTINES_JSON = "routines_json"
    private const val KEY_SESSION_ACTIVE = "session_active"
    private const val KEY_SESSION_END_EPOCH = "session_end_epoch"
    private const val KEY_SESSION_TOTAL_SEC = "session_total_sec"
    private const val KEY_ACTIVE_BAG_IDX = "active_bag_idx"

    var parachutes: Int = 1
    var streakDays: Int = 5
    var penaltyDays: Int = 0
    var chutesUsed: Int = 0
    var activeBagIndex: Int = 0

    val bags = mutableListOf(
        SuitcaseBag("BAG 1: STUDY", mutableListOf("com.google.android.dialer", "com.google.android.calculator", "com.spotify.music")),
        SuitcaseBag("BAG 2: SLEEP", mutableListOf("com.google.android.dialer")),
        SuitcaseBag("BAG 3: COMMUTE", mutableListOf("com.google.android.apps.maps", "com.spotify.music", "com.whatsapp"))
    )

    val towerFloors = mutableListOf(
        TowerFloor("Nov 07", 1.5f, 2),
        TowerFloor("Nov 08", 2.2f, 3),
        TowerFloor("Nov 09", 3.0f, 3),
        TowerFloor("Nov 10", 2.6f, 2),
        TowerFloor("Nov 11", 4.2f, 4),
        TowerFloor("Nov 12", 3.5f, 3),
        TowerFloor("Nov 13", 1.5f, 1)
    )

    val routines = mutableListOf(
        Routine("r1", "Night Sleep Detox", 1, "22:30", 8, "none", true),
        Routine("r2", "Physics Deep Study", 0, "17:00", 3, "mid", true)
    )

    fun load(context: Context) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        parachutes = sp.getInt(KEY_PARACHUTES, 1)
        streakDays = sp.getInt(KEY_STREAK_DAYS, 5)
        penaltyDays = sp.getInt(KEY_PENALTY_DAYS, 0)
        chutesUsed = sp.getInt(KEY_CHUTES_USED, 0)
        activeBagIndex = sp.getInt(KEY_ACTIVE_BAG_IDX, 0)
    }

    fun save(context: Context) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit()
            .putInt(KEY_PARACHUTES, parachutes)
            .putInt(KEY_STREAK_DAYS, streakDays)
            .putInt(KEY_PENALTY_DAYS, penaltyDays)
            .putInt(KEY_CHUTES_USED, chutesUsed)
            .putInt(KEY_ACTIVE_BAG_IDX, activeBagIndex)
            .apply()
    }

    fun isSessionActive(context: Context): Boolean {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val active = sp.getBoolean(KEY_SESSION_ACTIVE, false)
        val endEpoch = sp.getLong(KEY_SESSION_END_EPOCH, 0L)
        return active && System.currentTimeMillis() < endEpoch
    }

    fun startSession(context: Context, durationSeconds: Long) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val endEpoch = System.currentTimeMillis() + (durationSeconds * 1000L)
        sp.edit()
            .putBoolean(KEY_SESSION_ACTIVE, true)
            .putLong(KEY_SESSION_END_EPOCH, endEpoch)
            .putLong(KEY_SESSION_TOTAL_SEC, durationSeconds)
            .apply()
    }

    fun stopSession(context: Context) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_SESSION_ACTIVE, false).apply()
    }

    fun getRemainingSeconds(context: Context): Long {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val endEpoch = sp.getLong(KEY_SESSION_END_EPOCH, 0L)
        val diff = (endEpoch - System.currentTimeMillis()) / 1000L
        return if (diff > 0) diff else 0L
    }

    fun getTotalSessionSeconds(context: Context): Long {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sp.getLong(KEY_SESSION_TOTAL_SEC, 1500L)
    }

    fun getActiveBag(): SuitcaseBag {
        return bags.getOrElse(activeBagIndex) { bags[0] }
    }
}
