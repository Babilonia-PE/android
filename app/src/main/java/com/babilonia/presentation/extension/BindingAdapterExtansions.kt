package com.babilonia.presentation.extension

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

@BindingAdapter(value = ["imageUrl", "placeholder", "cornerRadius"], requireAll = false)
fun ImageView.setImageUrl(imageUrl: String?, placeholder: Drawable?, cornerRadius: Float?) {

    val transformations = if (cornerRadius == null) {
        arrayOf(CenterCrop())
    } else {
        arrayOf(CenterCrop(), RoundedCorners(cornerRadius.toInt()))
    }

    if (imageUrl == null) {
        Glide.with(this).load(placeholder)
    } else {
        Glide.with(this).load(imageUrl)
    }.placeholder(placeholder)
        .transform(*transformations)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}