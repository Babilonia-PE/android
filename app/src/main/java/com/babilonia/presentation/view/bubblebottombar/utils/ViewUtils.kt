package com.babilonia.presentation.view.bubblebottombar.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import androidx.annotation.Nullable
import com.babilonia.R


// Created by Anton Yatsenko on 12.06.2019.
object ViewUtils {

    fun getThemeAccentColor(context: Context): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(R.attr.colorAccent, value, true)
        return value.data
    }

    @SuppressLint("ObsoleteSdkInt")
    fun updateDrawableColor(@Nullable drawable: Drawable?, color: Int) {
        if (drawable == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            drawable.setTint(color)
        else
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

}