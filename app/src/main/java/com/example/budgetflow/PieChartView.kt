package com.example.budgetflow

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class PieChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint()
    private val textPaint = Paint()
    private val data = mutableListOf<PieSlice>()

    data class PieSlice(val label: String, val value: Float, val color: Int)

    init {
        paint.isAntiAlias = true

        textPaint.color = Color.BLACK
        textPaint.textSize = 32f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isAntiAlias = true
    }

    fun setData(data: List<PieSlice>) {
        this.data.clear()
        this.data.addAll(data)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val radius = Math.min(width, height) / 2.5f

        canvas.translate(width / 2f, height / 2f)

        val total = data.sumOf { it.value.toDouble() }.toFloat()

        if (total <= 0f) return

        var startAngle = 0f

        data.forEach { slice ->
            val sweepAngle = slice.value / total * 360f
            paint.color = slice.color
            canvas.drawArc(-radius, -radius, radius, radius, startAngle, sweepAngle, true, paint)

            // Mid-angle for label position
            val midAngle = startAngle + sweepAngle / 2
            val labelRadius = radius * 0.75f
            val x = (labelRadius * cos(Math.toRadians(midAngle.toDouble()))).toFloat()
            val y = (labelRadius * sin(Math.toRadians(midAngle.toDouble()))).toFloat()

            val percent = (slice.value / total * 100).toInt()
            canvas.drawText("${slice.label} ($percent%)", x, y, textPaint)

            startAngle += sweepAngle
        }
    }
}
