package com.babilonia.ar.base

import android.graphics.RectF

interface IArea {

    var viewFrame: RectF
    var tag: Any?

    fun add(tag: ITag)
    fun remove(tag: ITag)
    fun restore()
}