package com.babilonia.ar.base

import android.graphics.RectF

interface ITag {

    var tag: Any?
    var azimuth: Double
    var distance: Double
    var viewFrame: RectF
    var visible: Boolean
    var tagOptions: TagOptions

    fun remove()

    fun setDescription(description: String)

    fun select()

    fun unSelect()
}