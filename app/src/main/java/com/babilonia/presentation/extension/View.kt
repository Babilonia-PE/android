package com.babilonia.presentation.extension

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.babilonia.Constants.CORNER_RADIUS
import com.babilonia.R
import com.babilonia.presentation.utils.SvgUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import java.lang.reflect.*
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// Created by Anton Yatsenko on 26.02.2019.

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun View.cancelTransition() {
    transitionName = null
}

fun View.isVisible() = this.visibility == View.VISIBLE

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.GONE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visibleOrGone(isVisible: Boolean) = if (isVisible) visible() else invisible()

fun ViewGroup.inflate(@LayoutRes layoutRes: Int): View =
    LayoutInflater.from(context).inflate(layoutRes, this, false)

fun Type.simpleErasedName(): String {
    return when (val jvmType = this) {
        is Class<*> -> (jvmType.enclosingClass?.simpleErasedName()?.plus(".")
            ?: "") + jvmType.simpleName
        is ParameterizedType -> jvmType.rawType.simpleErasedName()
        is GenericArrayType -> jvmType.genericComponentType.simpleErasedName()
        is WildcardType -> "*"
        is TypeVariable<*> -> jvmType.name
        else -> throw IllegalArgumentException("Unknown type $javaClass $this")
    }
}

fun Fragment.getLayoutRes(): Int {
    val fragmentLayoutName = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
        .simpleErasedName()
        .replace("Binding", "")
        .split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])".toRegex())
        .joinToString(separator = "_")
        .toLowerCase(Locale.getDefault())

    val resourceName = "${context?.applicationContext?.packageName}:layout/$fragmentLayoutName"
    return resources.getIdentifier(resourceName, null, null)
}

fun ImageView.loadSvg(
    url: String?,
    placeholder: Int = R.drawable.ic_listing_placeholder
) {
    SvgUtil.loadSvg(this, url, placeholder)
}

fun ImageView.withGlide(
    url: String?,
    placeholder: Int = R.drawable.ic_listing_placeholder,
    cornerRadius: Int = CORNER_RADIUS
) {
    Glide.get(context).clearMemory()
    Glide.with(context)
        .load(url)
        .placeholder(placeholder)
        .transform(CenterCrop(), RoundedCorners(cornerRadius))
        .into(this)
}

fun ImageView.withGlideFitImage(
    url: String?,
    placeholder: Int = R.drawable.ic_listing_placeholder
) {
    Glide.get(context).clearMemory()
    Glide.with(context)
        .load(url)
        .placeholder(placeholder)
        .transform(FitCenter())
        .into(this)
}

fun ImageView.withGlide(resource: Int, placeholder: Int = R.drawable.ic_listing_placeholder) {
    Glide.with(context)
        .load(resource)
        .placeholder(placeholder)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    if (currentFocus == null) View(this) else currentFocus?.let { hideKeyboard(it) }
}

fun Context?.hideKeyboard(view: View) {
    view.clearFocus()
    val inputMethodManager = this?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
}

fun String.removeCommas(): String {
    return this.replace(",","", false)
}

inline fun <T> convertable(
    initialValue: T,
    crossinline f: (v: T) -> T,
    crossinline onChange: (property: KProperty<*>, oldValue: T, newValue: T) -> Unit
):
        ReadWriteProperty<Any?, T> = object : ConvertableVar<T>(initialValue) {
    override fun onChange(property: KProperty<*>, oldValue: T, newValue: T) =
        onChange(property, oldValue, newValue)

    override fun convertValue(value: T) = f.invoke(value)
}

open class ConvertableVar<T>(initialValue: T) : ReadWriteProperty<Any?, T> {
    private var value = initialValue

    protected open fun convertValue(value: T): T = value
    protected open fun onChange(property: KProperty<*>, oldValue: T, newValue: T) = Unit
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val oldValue = this.value
        this.value = convertValue(value)
        onChange(property, oldValue, value)
    }
}