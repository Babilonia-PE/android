package com.babilonia.domain.utils

import android.graphics.RectF

interface ArTagScreenPositionProvider {
    fun getPosition(type: ArTagType, coordinateVector: FloatArray, sceneWidth: Int, sceneHeight: Int): RectF
    fun getArPinPosition(coordinateVector: FloatArray, sceneWidth: Int, sceneHeight: Int): RectF
}