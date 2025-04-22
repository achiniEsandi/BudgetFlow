package com.example.budgetflow

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class PieChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint()
    private val data = mutableListOf<PieSlice>()

    data class PieSlice(val value: Float, val color: Int)

    init {
        paint.isAntiAlias = true
    }

    // Set the data for the pie chart
    fun setData(data: List<PieSlice>) {
        this.data.clear()
        this.data.addAll(data)
        invalidate()  // Redraw the view when data is set
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val radius = Math.min(width, height) / 2f

        // Move the canvas to the center of the view
        canvas.translate(width / 2f, height / 2f)

        var startAngle = 0f
        val total = data.sumByDouble { it.value.toDouble() }.toFloat()

        // Draw each slice
        data.forEach {
            val sweepAngle = it.value / total * 360f
            paint.color = it.color
            canvas.drawArc(
                -radius, -radius, radius, radius,
                startAngle, sweepAngle, true, paint
            )
            startAngle += sweepAngle
        }
    }
}
