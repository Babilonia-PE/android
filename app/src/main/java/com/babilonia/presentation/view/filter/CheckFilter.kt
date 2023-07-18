package com.babilonia.presentation.view.filter

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.babilonia.R

class CheckFilter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val tvTitle: TextView
    private val ivIcon: ImageView
    private val checkBox: CheckBox

    private var onCheckChangedCallback: ((Boolean) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_filter_check, this, true)
        tvTitle = findViewById(R.id.tvTitle)
        ivIcon = findViewById(R.id.ivIcon)
        checkBox = findViewById(R.id.checkBox)
        readAttributes(context, attrs)
        setOnClickListener {
            val newValue = !isChecked()
            setChecked(newValue)
            onCheckChangedCallback?.invoke(newValue)
        }
    }

    private fun readAttributes(context: Context, attrs: AttributeSet?) {
        if (attrs == null) return

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CheckFilter)
        tvTitle.text = typedArray.getString(R.styleable.CheckFilter_filter_name) ?: ""
        ivIcon.setImageDrawable(typedArray.getDrawable(R.styleable.CheckFilter_filter_icon))
        typedArray.recycle()
    }

    fun setOnCheckChangedCallback(callback: (Boolean) -> Unit) {
        onCheckChangedCallback = callback
    }

    fun setChecked(checked: Boolean) {
        checkBox.isChecked = checked
    }

    fun isChecked(): Boolean = checkBox.isChecked
}