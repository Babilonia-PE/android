package com.babilonia.presentation.view.numberpicker

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.babilonia.R
import com.google.android.material.button.MaterialButton

private const val CLICK_NUMBER_PICKER_MIN_VALUE_DEFAULT = 0
private const val CLICK_NUMBER_PICKER_MAX_VALUE_DEFAULT = 99
private const val CLICK_NUMBER_PICKER_VALUE_DEFAULT = 0
private const val CLICK_NUMBER_PICKER_STEP_DEFAULT = 1
private const val CLICK_NUMBER_PICKER_VALUE_TEXT_SIZE_DEFAULT = 15
private const val CLICK_NUMBER_PICKER_VALUE_ANIMATION_MIN_TEXT_SIZE_DEFAULT = 10
private const val CLICK_NUMBER_PICKER_VALUE_ANIMATION_MAX_TEXT_SIZE_DEFAULT = 22
private const val CLICK_NUMBER_PICKER_VALUE_VIEW_OFFSET_DEFAULT = 20
private const val CLICK_NUMBER_PICKER_CORNER_RADIUS_DEFAULT = 10f
private const val CLICK_NUMBER_PICKER_DECIMAL_NUMBER_DEFAULT = 2
private const val CLICK_NUMBER_PICKER_UP_DOWN_DURATION_DEFAULT = 200
private const val CLICK_NUMBER_PICKER_OFFSET_ANIMATION_DURATION_DEFAULT = 150

// Created by Anton Yatsenko on 04.06.2019.
class ClickNumberPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var flLeftPicker: MaterialButton? = null
    private var flRightPicker: MaterialButton? = null
    private var tvValue: TextView? = null
    private var tvTitle: TextView? = null
    private var rlRootView: ConstraintLayout? = null

    private var swipeEnabled: Boolean = false
    /**
     * Get picker's current value directly instead through listener [ClickNumberPickerListener]
     * @return current picker value
     */
    var value: Int = 0
    private var minValue: Int = 0
    private var maxValue: Int = 99
    private var step: Int = 1
    private var integerPriority: Boolean = false
    private var valueBackgroundColor: Int = 0
    private var pickersBackgroundColor: Int = 0
    private var animationUpDuration: Int = 0
    private var animationDownDuration: Int = 0
    private var animationUpEnabled: Boolean = false
    private var animationDownEnabled: Boolean = false
    private var animationSwipeEnabled: Boolean = false
    private var valueColor: Int = 0
    private var valueTextSize: Int = 0
    private var valueMinTextSize: Int = 0
    private var valueMaxTextSize: Int = 0
    private var valueViewOffset: Float = 0.toFloat()
    private var pickerCornerRadius: Float = 0.toFloat()
    private var pickerBorderStrokeWidth: Int = 0
    private var pickerBorderStrokeColor: Int = 0
    private var decimalNumbers: Int = 0
    private var animationOffsetLeftDuration: Int = 0
    private var animationOffsetRightDuration: Int = 0
    private var leftPickerLayout: Int = 0
    private var rightPickerLayout: Int = 0
    private var title: String = ""

    private var clickNumberPickerListener: ClickNumberPickerListener = object : ClickNumberPickerListener {
        override fun onValueChange(previousValue: Int, currentValue: Int, pickerClickType: PickerClickType) {}
    }

    private var leftPickerTranslationXAnimator: ObjectAnimator? = null
    private var rightPickerTranslationXAnimator: ObjectAnimator? = null


    private lateinit var valueFormatter: String
    private val swipeValueChangeHandler = Handler()
    private var swipeDirection = PickerClickType.NONE
    private var swipeStep = 1

    private val valueChangeRunnable = object : Runnable {
        override fun run() {
            try {
                when (swipeDirection) {
                    PickerClickType.LEFT -> updatePickerValueByStep(-(swipeStep * swipeStep))
                    PickerClickType.RIGHT -> updatePickerValueByStep(swipeStep * swipeStep)
                }
            } finally {
                ++swipeStep
                swipeValueChangeHandler.postDelayed(this, 200)
            }
        }
    }

    private val touchListener = object : OnTouchListener {
        private var dX = 0.0f
        private var initTouchX = 0.0f

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    swipeStep = 1
                }

                MotionEvent.ACTION_MOVE -> {
                    if (initTouchX - valueViewOffset * 2 > event.rawX + dX) {
                        swipeDirection = PickerClickType.LEFT
                        valueChangeRunnable.run()

                    } else if (initTouchX + valueViewOffset * 2 < event.rawX + dX) {
                        swipeDirection = PickerClickType.RIGHT
                        valueChangeRunnable.run()

                    }
                }
                MotionEvent.ACTION_UP -> {
                    swipeValueChangeHandler.removeCallbacks(valueChangeRunnable)

                }
                else -> return false
            }

            return true
        }
    }

    private val leftPickerListener = OnClickListener {
        if (animationSwipeEnabled) {
            if (leftPickerTranslationXAnimator?.isRunning == true) {
                leftPickerTranslationXAnimator?.end()
            }

            leftPickerTranslationXAnimator?.start()
        }



        updatePickerValueByStep(-step)
    }

    private val rightPickerListener = OnClickListener {
        if (animationSwipeEnabled) {
            if (rightPickerTranslationXAnimator?.isRunning == true) {
                rightPickerTranslationXAnimator?.end()
            }
            rightPickerTranslationXAnimator?.start()
        }



        updatePickerValueByStep(step)
    }

    init {

        readAttributes(context, attrs)

        init()
    }

    private fun readAttributes(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClickNumberPickerView)

        swipeEnabled = typedArray.getBoolean(R.styleable.ClickNumberPickerView_swipe_enabled, true)
        value = typedArray.getInteger(R.styleable.ClickNumberPickerView_value, CLICK_NUMBER_PICKER_VALUE_DEFAULT)
        minValue =
            typedArray.getInteger(R.styleable.ClickNumberPickerView_min_value, CLICK_NUMBER_PICKER_MIN_VALUE_DEFAULT)
        maxValue =
            typedArray.getInteger(R.styleable.ClickNumberPickerView_max_value, CLICK_NUMBER_PICKER_MAX_VALUE_DEFAULT)
        step = typedArray.getInteger(R.styleable.ClickNumberPickerView_step, CLICK_NUMBER_PICKER_STEP_DEFAULT)
        integerPriority = typedArray.getBoolean(R.styleable.ClickNumberPickerView_integer_priority, false)
        valueBackgroundColor = typedArray.getColor(R.styleable.ClickNumberPickerView_value_background_color, 0)
        pickersBackgroundColor = typedArray.getColor(R.styleable.ClickNumberPickerView_pickers_background_color, 0)
        animationUpEnabled = typedArray.getBoolean(R.styleable.ClickNumberPickerView_value_animation_up, false)
        animationDownEnabled = typedArray.getBoolean(R.styleable.ClickNumberPickerView_value_animation_down, false)
        title = typedArray.getString(R.styleable.ClickNumberPickerView_title) ?: ""

        valueTextSize = typedArray.getDimensionPixelSize(
            R.styleable.ClickNumberPickerView_value_text_size,
            CLICK_NUMBER_PICKER_VALUE_TEXT_SIZE_DEFAULT
        )
        valueMinTextSize = typedArray.getDimensionPixelSize(
            R.styleable.ClickNumberPickerView_value_min_text_size,
            CLICK_NUMBER_PICKER_VALUE_ANIMATION_MIN_TEXT_SIZE_DEFAULT
        )
        valueMaxTextSize = typedArray.getDimensionPixelSize(
            R.styleable.ClickNumberPickerView_value_max_text_size,
            CLICK_NUMBER_PICKER_VALUE_ANIMATION_MAX_TEXT_SIZE_DEFAULT
        )
        valueViewOffset = typedArray.getFloat(
            R.styleable.ClickNumberPickerView_value_view_offset,
            CLICK_NUMBER_PICKER_VALUE_VIEW_OFFSET_DEFAULT.toFloat()
        )
        animationSwipeEnabled = typedArray.getBoolean(R.styleable.ClickNumberPickerView_swipe_animation, false)
        pickerCornerRadius = typedArray.getFloat(
            R.styleable.ClickNumberPickerView_picker_corner_radius,
            CLICK_NUMBER_PICKER_CORNER_RADIUS_DEFAULT
        )
        pickerBorderStrokeWidth = typedArray.getInt(R.styleable.ClickNumberPickerView_picker_border_stroke_width, 0)
        pickerBorderStrokeColor = typedArray.getColor(R.styleable.ClickNumberPickerView_picker_border_stroke_color, 0)
        decimalNumbers =
            typedArray.getInt(
                R.styleable.ClickNumberPickerView_decimal_number,
                CLICK_NUMBER_PICKER_DECIMAL_NUMBER_DEFAULT
            )
        animationUpDuration = typedArray.getInt(
            R.styleable.ClickNumberPickerView_animation_value_up_duration,
            CLICK_NUMBER_PICKER_UP_DOWN_DURATION_DEFAULT
        )
        animationDownDuration = typedArray.getInt(
            R.styleable.ClickNumberPickerView_animation_value_down_duration,
            CLICK_NUMBER_PICKER_UP_DOWN_DURATION_DEFAULT
        )
        animationOffsetRightDuration = typedArray.getInt(
            R.styleable.ClickNumberPickerView_animation_offset_right_duration,
            CLICK_NUMBER_PICKER_OFFSET_ANIMATION_DURATION_DEFAULT
        )
        animationOffsetLeftDuration = typedArray.getInt(
            R.styleable.ClickNumberPickerView_animation_offset_left_duration,
            CLICK_NUMBER_PICKER_OFFSET_ANIMATION_DURATION_DEFAULT
        )
        typedArray.recycle()
    }

    private fun init() {
        initViews()
        initAnimators()
        initListeners()

        applyViewAttributes()
    }

    private fun applyViewAttributes() {

        valueFormatter = NumberFormatUtils.provideFloatFormater(decimalNumbers)
        swipeStep = step
        setPickerValue(value)
    }

    private fun initListeners() {
        if (swipeEnabled) {
        }

        flLeftPicker?.setOnClickListener(leftPickerListener)
        flRightPicker?.setOnClickListener(rightPickerListener)
    }

    private fun initViews() {
        val view = inflate(context, R.layout.view_click_numberpicker, this)

        flLeftPicker = view.findViewById(R.id.fl_click_numberpicker_left)
        rlRootView = view.findViewById(R.id.rl_pickers_root) as ConstraintLayout
        flRightPicker = view.findViewById(R.id.fl_click_numberpicker_right)
        tvValue = view.findViewById(R.id.tv_value_numberpicker)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvTitle?.text = title
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun initAnimators() {

    }

    private fun formatValue(value: Int): String {
        return value.toString()

    }

    fun setPickerMinValue(value: Int) {
        this.minValue = value
    }

    fun setPickerMaxValue(value: Int) {
        this.maxValue = value
    }

    /**
     * Set picker current value
     * @param value
     */
    public fun setPickerValue(value: Int) {
        if (value < minValue || value > maxValue) {
            return
        }

        clickNumberPickerListener.onValueChange(
            this.value,
            value,
            if (this.value > value) PickerClickType.LEFT else PickerClickType.RIGHT
        )

        this.value = value
        tvValue?.text = formatValue(this.value)
        when {
            value <= minValue -> flLeftPicker?.isEnabled = false
            value >= maxValue -> flRightPicker?.isEnabled = false
            else -> {
                flRightPicker?.isEnabled = true
                flLeftPicker?.isEnabled = true
            }
        }
    }

    /**
     * Update current picker value by provided step
     * @param step
     */
    fun updatePickerValueByStep(step: Int) {
        if (value + step < minValue) {
            setPickerValue(minValue)
        } else if (value + step > maxValue) {
            setPickerValue(maxValue)
        }
        setPickerValue(value + step)
    }

    /**
     * Set picker number value change listener
     * @param clickNumberPickerListener
     */
    fun setClickNumberPickerListener(clickNumberPickerListener: ClickNumberPickerListener) {
        this.clickNumberPickerListener = clickNumberPickerListener
    }
}