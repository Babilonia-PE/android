package com.babilonia.presentation.view.circleview

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path

class ClipPathManager : ClipManager {

    val path: Path = Path()

    private var createClipPath: ClipPathCreator? = null

    val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            setColor(Color.BLACK)
            setStyle(Paint.Style.FILL)
            setAntiAlias(true)
            setStrokeWidth(1F)
        }

    override fun getPaint(): Paint {
        return clipPaint
    }

    override fun requiresBitmap(): Boolean {
        return createClipPath?.requiresBitmap() ?: false
    }

    fun createClipPath(width: Int, height: Int): Path? {
        return createClipPath?.createClipPath(width, height)
    }

    fun setClipPathCreator(createClipPath: ClipPathCreator) {
        this.createClipPath = createClipPath
    }

    override fun createMask(width: Int, height: Int): Path? {
        return path
    }

    override fun getShadowConvexPath(): Path? {
        return path
    }

    override fun setupClipLayout(width: Int, height: Int) {
        path.reset()
        val clipPath = createClipPath(width, height)
        clipPath?.apply { path.set(this) }
    }

    interface ClipPathCreator {
        fun createClipPath(width: Int, height: Int): Path
        fun requiresBitmap(): Boolean
    }
}