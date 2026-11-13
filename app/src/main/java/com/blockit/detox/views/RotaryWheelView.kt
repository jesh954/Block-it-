package com.blockit.detox.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class RotaryWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bezelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2C2E37")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
    }

    private val wellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#121318")
        style = Paint.Style.FILL
    }

    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF3B30")
        style = Paint.Style.FILL
    }

    private val thumbDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private var currentMinutes: Int = 25
    private var currentAngle: Double = -PI / 2 + 0.35 * (2 * PI)
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    var onDurationChanged: ((Int) -> Unit)? = null

    fun setMinutes(mins: Int) {
        currentMinutes = mins.coerceIn(1, 720)
        val norm = (currentMinutes.toDouble() / 720.0).pow(0.55)
        currentAngle = -PI / 2 + norm * (2 * PI)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val outerR = min(cx, cy) - 16f
        val innerR = outerR * 0.72f

        canvas.drawCircle(cx, cy, outerR, bezelPaint)

        val totalTicks = 48
        var norm = (currentAngle - (-PI / 2)) / (2 * PI)
        while (norm < 0) norm += 1.0
        while (norm > 1) norm -= 1.0
        val activeTickCount = (norm * totalTicks).roundToInt()

        for (i in 0 until totalTicks) {
            val angle = -PI / 2 + (i.toDouble() / totalTicks) * (2 * PI)
            val isMajor = i % 4 == 0
            val r1 = if (isMajor) innerR + 6f else innerR + 12f
            val r2 = outerR - 6f

            val x1 = (cx + cos(angle) * r1).toFloat()
            val y1 = (cy + sin(angle) * r1).toFloat()
            val x2 = (cx + cos(angle) * r2).toFloat()
            val y2 = (cy + sin(angle) * r2).toFloat()

            tickPaint.color = if (i <= activeTickCount) Color.parseColor("#F5F5F7") else Color.parseColor("#2C2E37")
            tickPaint.strokeWidth = if (isMajor) 6f else 3.5f
            canvas.drawLine(x1, y1, x2, y2, tickPaint)
        }

        // Inner well
        canvas.drawCircle(cx, cy, innerR - 6f, wellPaint)

        // Thumb indicator
        val handleR = (innerR + outerR) / 2f
        val hx = (cx + cos(currentAngle) * handleR).toFloat()
        val hy = (cy + sin(currentAngle) * handleR).toFloat()
        canvas.drawCircle(hx, hy, 18f, thumbPaint)
        canvas.drawCircle(hx, hy, 6f, thumbDotPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val cx = width / 2f
        val cy = height / 2f
        val dx = event.x - cx
        val dy = event.y - cy
        val angle = atan2(dy.toDouble(), dx.toDouble())

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                currentAngle = angle
                var a = angle - (-PI / 2)
                while (a < 0) a += 2 * PI
                while (a >= 2 * PI) a -= 2 * PI
                val norm = a / (2 * PI)
                val rawMins = norm.pow(1.0 / 0.55) * 720.0
                val newMins = when {
                    rawMins <= 30 -> max(1, rawMins.roundToInt())
                    rawMins <= 120 -> (rawMins / 5.0).roundToInt() * 5
                    else -> min(720, (rawMins / 15.0).roundToInt() * 15)
                }

                if (newMins != currentMinutes) {
                    currentMinutes = newMins
                    triggerHaptic()
                    onDurationChanged?.invoke(currentMinutes)
                }
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun triggerHaptic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(12)
        }
    }
}
