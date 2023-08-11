package com.babilonia.presentation.extension

import android.content.Context
import android.util.TypedValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.babilonia.EmptyConstants
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import java.util.*

// Created by Anton Yatsenko on 26.02.2019.
/**
 * Kotlin extensions class
 */
fun <T1 : Any, T2 : Any, R : Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}

fun <T1 : Any, T2 : Any, T3 : Any, R : Any> safeLet(p1: T1?, p2: T2?, p3: T3?, block: (T1, T2, T3) -> R?): R? {
    return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
}

fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    p4: T4?,
    block: (T1, T2, T3, T4) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null) block(p1, p2, p3, p4) else null
}

fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    p4: T4?,
    p5: T5?,
    block: (T1, T2, T3, T4, T5) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null && p5 != null) block(p1, p2, p3, p4, p5) else null
}

fun <T : ViewModel> T.createFactory(): ViewModelProvider.Factory {
    val viewModel = this
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = viewModel as T
    }
}


fun Float.pxValue(unit: Int = TypedValue.COMPLEX_UNIT_DIP, context: Context): Float {
    return TypedValue.applyDimension(unit, this, context.resources.displayMetrics)
}

fun String?.formattedStringToInt(): Int? {
    return if (this.isNullOrEmpty().not())
        this?.replace(",", EmptyConstants.EMPTY_STRING)?.toInt()
    else 0
}

fun String?.formattedStringToLong(): Long? {
    return if (this.isNullOrEmpty().not())
        this?.replace(",", EmptyConstants.EMPTY_STRING)?.toLong()
    else 0
}

fun LatLng.toBounds(radius: Double): LatLngBounds {
    // radius is in meter
    val southwest = SphericalUtil.computeOffset(this, radius * Math.sqrt(2.0), 225.0)
    val northeast = SphericalUtil.computeOffset(this, radius * Math.sqrt(2.0), 45.0)
    return LatLngBounds(southwest, northeast)
}

fun String.capitalizeEachWord(): String {
    val words = this.trim().toLowerCase(Locale.getDefault()).split(" ")
    var concatWords = ""
    for (index in words.indices){
        concatWords = if(index==0) words[index].capitalize(Locale.getDefault())
        else concatWords.plus(" ").plus(words[index].capitalize(Locale.getDefault()))
    }
    return concatWords.trim()
}