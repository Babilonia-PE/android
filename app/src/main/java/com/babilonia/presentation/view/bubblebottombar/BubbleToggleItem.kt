package com.babilonia.presentation.view.bubblebottombar

import android.graphics.Color
import android.graphics.drawable.Drawable
import com.babilonia.R


// Created by Anton Yatsenko on 12.06.2019.


internal class BubbleToggleItem {

    var icon: Drawable? = null
    var shape: Drawable? = null
    var title = ""

    var colorActive = Color.BLUE
    var colorInactive = Color.BLACK
    var shapeColor = R.color.colorPrimary

    var badgeText: String? = null
    var badgeTextColor = Color.WHITE
    var badgeBackgroundColor = Color.BLACK

    var titleSize: Float = 12.toFloat()
    var badgeTextSize: Float = 0.toFloat()
    var iconWidth: Float = 24.toFloat()
    var iconHeight: Float = 24.toFloat()

    var titlePadding: Int = 0
    var internalPadding: Int = 0
}