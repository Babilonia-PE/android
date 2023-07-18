package com.babilonia.presentation.view.filter

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.annotation.StringRes
import com.babilonia.R
import com.babilonia.presentation.extension.hideKeyboard
import com.babilonia.presentation.extension.removeCommas
import com.babilonia.presentation.view.priceview.SimpleRangeView
import kotlinx.android.synthetic.main.layout_filter_two_side.view.*
import java.util.*

class TwoSideFilter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // We use Long because we can exceed Int capacity during calculations
    private var minValue = 0L
    private var maxValue = 100L
    private var currentLeftValue = 0
    private var currentRightValue = 100

    private var onLeftPinChangedCallback: ((String, Boolean) -> Unit)? = null
    private var onRightPinChangedCallback: ((String, Boolean) -> Unit)? = null

    private var formatWithComas = true

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_filter_two_side, this, true)

        rangeBar.onTrackRangeListener = object : SimpleRangeView.OnTrackRangeListener {
            override fun onStartRangeChanged(rangeView: SimpleRangeView, leftPinIndex: Int) {
                val newValue: Long = (leftPinIndex * (maxValue - minValue) / 100 + minValue)
                currentLeftValue = newValue.toInt()
                updateMinValueText()
                onLeftPinChangedCallback?.invoke(currentLeftValue.toString(), rangeBar.start == 0)
            }

            override fun onEndRangeChanged(rangeView: SimpleRangeView, rightPinIndex: Int) {
                val newValue: Long = (rightPinIndex * (maxValue - minValue) / 100L + minValue)
                currentRightValue = newValue.toInt()
                updateMaxValueText()
                onRightPinChangedCallback?.invoke(currentRightValue.toString(), rangeBar.end == 100)
            }
        }

        etFrom.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etFrom.setText(etFrom.text.toString().removeCommas())
            } else {
                val enteredText = etFrom.text.toString().removeCommas()
                currentLeftValue = if (enteredText.isBlank()) {
                    minValue.toInt()
                } else {
                    enteredText.toInt()
                }
                fixLeftValue()
                updateMinValuePosition()
                updateMinValueText()
                onLeftPinChangedCallback?.invoke(currentLeftValue.toString(), rangeBar.start == 0)
            }
        }
        etTo.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etTo.setText(etTo.text.toString().removeCommas())
            } else {
                val enteredText = etTo.text.toString().removeCommas()
                currentRightValue = if (enteredText.isBlank()) {
                    maxValue.toInt()
                } else {
                    enteredText.toInt()
                }
                fixRightValue()
                updateMaxValuePosition()
                updateMaxValueText()
                onRightPinChangedCallback?.invoke(currentRightValue.toString(), rangeBar.end == 100)
            }
        }
        etFrom.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                context.hideKeyboard(view)
            }
            return@setOnEditorActionListener false
        }
        etTo.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                context.hideKeyboard(view)
            }
            return@setOnEditorActionListener false
        }
    }

    fun initListeners(
        onLeftPinChanged: ((String, Boolean) -> Unit),
        onRightPinChanged: ((String, Boolean) -> Unit)
    ) {
        onLeftPinChangedCallback = onLeftPinChanged
        onRightPinChangedCallback = onRightPinChanged
    }

    fun setRange(minValue: Int, maxValue: Int) {
        val newMinValue = minValue.toLong()
        val newMaxValue = maxValue.toLong()
        if (newMinValue != this.minValue || newMaxValue != this.maxValue) {
            this.minValue = newMinValue
            this.maxValue = newMaxValue
            currentLeftValue = minValue
            currentRightValue = maxValue
            fixLeftValue()
            fixRightValue()
            updateMinValuePosition()
            updateMaxValuePosition()
            updateMinValueText()
            updateMaxValueText()
        }
    }

    fun setTitleText(@StringRes resId: Int) {
        tvTitle.setText(resId)
    }

    fun setTitleText(text: String) {
        tvTitle.text = text
    }

    fun setInputHints(@StringRes fromHint: Int, @StringRes toHint: Int) {
        tiFrom.hint = context.getString(fromHint)
        tiTo.hint = context.getString(toHint)
    }

    fun setCurrentStartValue(value: String) {
        currentLeftValue = value.toInt()
        fixLeftValue()
        updateMinValuePosition()
        updateMinValueText()
    }

    fun setCurrentEndValue(value: String) {
        currentRightValue = value.toInt()
        fixRightValue()
        updateMaxValuePosition()
        updateMaxValueText()
    }

    fun resetToInitialPositions() {
        currentLeftValue = minValue.toInt()
        currentRightValue = maxValue.toInt()
        updateMinValuePosition()
        updateMaxValuePosition()
        updateMinValueText()
        updateMaxValueText()
    }

    fun getRightValueText(): String = currentRightValue.toString()

    fun getLeftValueText(): String = currentLeftValue.toString()

    fun isRightPinInInitialPosition(): Boolean = rangeBar.end == 100

    fun isLeftPinInInitialPosition(): Boolean = rangeBar.start == 0

    fun setFilterEnabled(isEnabled: Boolean) {
        etFrom.isEnabled = isEnabled
        etTo.isEnabled = isEnabled
        rangeBar.isEnabled = isEnabled
    }

    fun setFormatWithComas(doFormat: Boolean) {
        formatWithComas = doFormat
    }

    private fun updateMinValueText() {
        etFrom.setText(
            if (formatWithComas) {
                String.format(Locale.US, "%,d", currentLeftValue)
            } else {
                currentLeftValue.toString()
            }
        )
    }

    private fun updateMaxValueText() {
        etTo.setText(if (formatWithComas) {
            String.format(Locale.US, "%,d", currentRightValue)
        } else {
            currentRightValue.toString()
        })
    }

    private fun updateMinValuePosition() {
        if (maxValue == minValue) {
            rangeBar.start = 0

            if (minValue == 0L) {
                setFilterEnabled(false)
            }
        } else {
            val newValue: Long = (currentLeftValue.toLong() - minValue) * 100L / (maxValue - minValue)
            rangeBar.start = newValue.toInt()
        }
    }

    private fun updateMaxValuePosition() {
        if (maxValue == minValue) {
            rangeBar.end = 100

            if (minValue == 0L) {
                setFilterEnabled(false)
            }
        } else {
            val newValue: Long = (currentRightValue.toLong() - minValue) * 100L / (maxValue - minValue)
            rangeBar.end = newValue.toInt()
        }
    }

    private fun fixLeftValue() {
        when {
            currentLeftValue > currentRightValue -> currentLeftValue = currentRightValue
            currentLeftValue < minValue -> currentLeftValue = minValue.toInt()
        }
    }

    private fun fixRightValue() {
        when {
            currentRightValue < currentLeftValue -> currentRightValue = currentLeftValue
            currentRightValue > maxValue -> currentRightValue = maxValue.toInt()
        }
    }
}