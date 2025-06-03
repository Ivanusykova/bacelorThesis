package com.example.bak_python

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#B3000000")
    }

    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = 300f

        canvas.drawCircle(centerX, centerY, radius, clearPaint)

        canvas.restoreToCount(saveCount)
    }
}
