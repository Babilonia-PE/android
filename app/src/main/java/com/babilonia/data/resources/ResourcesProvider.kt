package com.babilonia.data.resources

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.babilonia.R
import javax.inject.Inject

class ResourcesProvider @Inject constructor(val context: Context) {

    companion object {
        val INVALID_RESOURCE_ID = 0
    }


    fun provideDeviceOrientation() = context.resources.configuration.orientation

    fun provideView(width: Int, height: Int) = View(context).apply {
        layoutParams = ViewGroup.LayoutParams(width, height)
        setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
    }


}