package com.example.myapplication.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GridBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x0C19A1E4.toInt()
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x2019A1E4.toInt()
        style = Paint.Style.FILL
    }

    private val cellSize = 64f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        var x = 0f
        while (x <= w) {
            canvas.drawLine(x, 0f, x, h, gridPaint)
            x += cellSize
        }

        var y = 0f
        while (y <= h) {
            canvas.drawLine(0f, y, w, y, gridPaint)
            y += cellSize
        }

        x = 0f
        while (x <= w) {
            var yDot = 0f
            while (yDot <= h) {
                canvas.drawCircle(x, yDot, 2.5f, dotPaint)
                yDot += cellSize
            }
            x += cellSize
        }
    }
}