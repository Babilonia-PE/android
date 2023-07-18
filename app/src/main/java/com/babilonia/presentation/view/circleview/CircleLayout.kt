package com.babilonia.presentation.view.circleview

import android.content.Context
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet


class CircleLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ShapeOfView(context, attrs, defStyleAttr, defStyleRes) {

    init {
        setClipPathCreator(
            object : ClipPathManager.ClipPathCreator {
                override fun createClipPath(width: Int, height: Int): Path =
                    Path().apply {
                        reset()
                        val y = height.toFloat()
                        val x = width.toFloat()
                        val rectF = RectF(0f, 0F, x, y)

                        moveTo(x / 2, y / 2)
                        arcTo(rectF, 0f, 270f)
                        arcTo(rectF, 270f, 90f)
                        close()
                    }


                override fun requiresBitmap(): Boolean = false
            })
    }
}