package com.babilonia.presentation.view.circleview

import android.graphics.Paint
import android.graphics.Path

interface ClipManager {

    fun createMask(width: Int, height: Int): Path?

    fun getShadowConvexPath(): Path?

    fun setupClipLayout(width: Int, height: Int)

    fun getPaint(): Paint

    fun requiresBitmap(): Boolean
}