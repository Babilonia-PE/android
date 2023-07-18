package com.babilonia.presentation.view

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.TypefaceSpan
import com.babilonia.EmptyConstants


// Created by Anton Yatsenko on 21.06.2019.
class CustomTypefaceSpan(
    family: String,
    private val newType: Typeface,
    private val textSize: Float = EmptyConstants.EMPTY_FLOAT
) :
    TypefaceSpan(family) {

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, newType)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, newType)
    }

    private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
        if (textSize != EmptyConstants.EMPTY_FLOAT) {
            paint.textSize = textSize
        }
        val oldStyle: Int
        val old = paint.typeface
        oldStyle = old?.style ?: 0

        val fake = oldStyle and tf.style.inv()
        if (fake and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }

        if (fake and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
        }

        paint.typeface = tf
    }
}