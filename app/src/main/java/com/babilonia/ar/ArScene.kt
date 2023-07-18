package com.babilonia.ar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout

class ArScene @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.RED
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textSize = 20f
    }


    init {
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        drawCrossLine(canvas)
    }

    private fun drawCrossLine(canvas: Canvas) {

        val pathX = Path()
        pathX.moveTo(0f, height / 2f)
        pathX.lineTo(width * 1f, height / 2f)
        pathX.close()

        canvas.drawPath(pathX, linePaint)
        canvas.drawTextOnPath("max X $width", pathX, 0f, -4f, linePaint)


        val pathY = Path()
        pathX.moveTo(width / 2f, 0f)
        pathX.lineTo(width / 2f, height * 1f)
        pathX.close()
        canvas.drawPath(pathX, linePaint)
        canvas.drawTextOnPath("max Y $height", pathY, width / 2f, 10f, linePaint)
    }
}