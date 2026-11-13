package com.blockit.detox.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class BlockyMorphTimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bgDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#22242C")
        style = Paint.Style.FILL
    }

    private val activeBlockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F5F5F7")
        style = Paint.Style.FILL
    }

    private val colonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF3B30")
        style = Paint.Style.FILL
    }

    private val DIGITS = mapOf(
        '0' to intArrayOf(1,1,1, 1,0,1, 1,0,1, 1,0,1, 1,1,1),
        '1' to intArrayOf(0,1,0, 1,1,0, 0,1,0, 0,1,0, 1,1,1),
        '2' to intArrayOf(1,1,1, 0,0,1, 1,1,1, 1,0,0, 1,1,1),
        '3' to intArrayOf(1,1,1, 0,0,1, 1,1,1, 0,0,1, 1,1,1),
        '4' to intArrayOf(1,0,1, 1,0,1, 1,1,1, 0,0,1, 0,0,1),
        '5' to intArrayOf(1,1,1, 1,0,0, 1,1,1, 0,0,1, 1,1,1),
        '6' to intArrayOf(1,1,1, 1,0,0, 1,1,1, 1,0,1, 1,1,1),
        '7' to intArrayOf(1,1,1, 0,0,1, 0,0,1, 0,0,1, 0,0,1),
        '8' to intArrayOf(1,1,1, 1,0,1, 1,1,1, 1,0,1, 1,1,1),
        '9' to intArrayOf(1,1,1, 1,0,1, 1,1,1, 0,0,1, 1,1,1)
    )

    private var currentChars = charArrayOf('0', '0', '2', '5', '0', '0')
    private var prevChars = charArrayOf('0', '0', '2', '5', '0', '0')
    private var morphFraction = 1f
    private var animator: ValueAnimator? = null

    fun setTime(totalSeconds: Long) {
        val s = totalSeconds.coerceAtLeast(0)
        val hrs = s / 3600
        val mins = (s % 3600) / 60
        val secs = s % 60
        val str = String.format("%02d%02d%02d", hrs, mins, secs)

        if (str != String(currentChars)) {
            prevChars = currentChars.clone()
            currentChars = str.toCharArray()

            animator?.cancel()
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 220
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    morphFraction = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cellSize = height / 7.5f
        val cellGap = cellSize * 0.28f
        val digitWidth = 3 * cellSize + 2 * cellGap
        val digitHeight = 5 * cellSize + 4 * cellGap
        val colonWidth = cellSize * 1.3f
        val pairGap = cellSize * 0.7f

        val totalWidth = 6 * digitWidth + 3 * pairGap + 2 * colonWidth
        var curX = (width - totalWidth) / 2f
        val startY = (height - digitHeight) / 2f

        for (d in 0 until 6) {
            val prevMat = DIGITS[prevChars[d]] ?: DIGITS['0']!!
            val nextMat = DIGITS[currentChars[d]] ?: DIGITS['0']!!

            for (row in 0 until 5) {
                for (col in 0 until 3) {
                    val idx = row * 3 + col
                    val v0 = prevMat[idx].toFloat()
                    val v1 = nextMat[idx].toFloat()
                    val v = v0 + (v1 - v0) * morphFraction

                    val bx = curX + col * (cellSize + cellGap)
                    val by = startY + row * (cellSize + cellGap)

                    // Draw base inactive dot
                    canvas.drawRoundRect(bx, by, bx + cellSize, by + cellSize, cellSize * 0.25f, cellSize * 0.25f, bgDotPaint)

                    // Draw active interpolated block
                    if (v > 0.02f) {
                        val scale = 0.35f + 0.65f * v
                        val sSize = cellSize * scale
                        val offset = (cellSize - sSize) / 2f
                        activeBlockPaint.alpha = (v.coerceIn(0f, 1f) * 255).toInt()
                        canvas.drawRoundRect(
                            bx + offset, by + offset,
                            bx + offset + sSize, by + offset + sSize,
                            cellSize * 0.25f, cellSize * 0.25f, activeBlockPaint
                        )
                    }
                }
            }

            curX += digitWidth
            if (d == 1 || d == 3) {
                // Colon dots
                val cx = curX + colonWidth / 2f - cellSize * 0.25f
                val dotSize = cellSize * 0.5f
                canvas.drawRect(cx, startY + digitHeight * 0.28f, cx + dotSize, startY + digitHeight * 0.28f + dotSize, colonPaint)
                canvas.drawRect(cx, startY + digitHeight * 0.72f, cx + dotSize, startY + digitHeight * 0.72f + dotSize, colonPaint)
                curX += colonWidth
            } else {
                curX += pairGap
            }
        }
    }
}
