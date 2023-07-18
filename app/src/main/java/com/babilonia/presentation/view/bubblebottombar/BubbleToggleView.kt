package com.babilonia.presentation.view.bubblebottombar

// Created by Anton Yatsenko on 12.06.2019.
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.babilonia.R
import com.babilonia.presentation.extension.pxValue
import com.babilonia.presentation.view.bubblebottombar.utils.ViewUtils

/**
 * BubbleToggleView
 *
 * @author Gaurav Kumar
 */
class BubbleToggleView : RelativeLayout {

    private var bubbleToggleItem: BubbleToggleItem = BubbleToggleItem()

    /**
     * Get the current state of the view
     *
     * @return the current state
     */
    var isActive = false
        private set

    private lateinit var iconView: ImageView
    private lateinit var titleView: TextView
    private lateinit var badgeView: TextView

    private var animationDuration: Int = 0
    private var showShapeAlways: Boolean = false

    private var maxTitleWidth: Float = 0.toFloat()
    private var measuredTitleWidth: Float = 0.toFloat()

    /**
     * Constructors
     */
    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, @Nullable attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(context, attrs)
    }

    /////////////////////////////////////
    // PRIVATE METHODS
    /////////////////////////////////////

    /**
     * Initialize
     *
     * @param context current context
     * @param attrs   custom attributes
     */
    private fun init(context: Context, @Nullable attrs: AttributeSet?) {
        //initialize default component
        var title: String? = "Title"
        var icon: Drawable? = null
        var shape: Drawable? = null
        var shapeColor = ContextCompat.getColor(context, R.color.colorPrimary)
        var colorActive = ViewUtils.getThemeAccentColor(context)
        var colorInactive = ContextCompat.getColor(context, R.color.default_inactive_color)
        var titleSize = context.resources.getDimension(R.dimen.default_nav_item_text_size)
        maxTitleWidth = context.resources.getDimension(R.dimen.default_nav_item_title_max_width)
        var iconWidth = context.resources.getDimension(R.dimen.default_icon_size)
        var iconHeight = context.resources.getDimension(R.dimen.default_icon_size)
        var internalPadding = context.resources.getDimension(R.dimen.default_nav_item_padding).toInt()
        var titlePadding = context.resources.getDimension(R.dimen.default_nav_item_text_padding).toInt()

        var badgeTextSize = context.resources.getDimension(R.dimen.default_nav_item_badge_text_size).toInt()
        var badgeBackgroundColor = ContextCompat.getColor(context, R.color.default_badge_background_color)
        var badgeTextColor = ContextCompat.getColor(context, R.color.default_badge_text_color)
        var badgeText: String? = null

        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.BubbleToggleView, 0, 0)
            try {
                icon = ta.getDrawable(R.styleable.BubbleToggleView_bt_icon)
                iconWidth = ta.getDimension(R.styleable.BubbleToggleView_bt_iconWidth, iconWidth)
                iconHeight = ta.getDimension(R.styleable.BubbleToggleView_bt_iconHeight, iconHeight)
                shape = ta.getDrawable(R.styleable.BubbleToggleView_bt_shape)
                shapeColor = ta.getColor(R.styleable.BubbleToggleView_bt_shapeColor, shapeColor)
                showShapeAlways = ta.getBoolean(R.styleable.BubbleToggleView_bt_showShapeAlways, false)
                title = ta.getString(R.styleable.BubbleToggleView_bt_title)
                titleSize = ta.getDimension(R.styleable.BubbleToggleView_bt_titleSize, titleSize)
                colorActive = ta.getColor(R.styleable.BubbleToggleView_bt_colorActive, colorActive)
                colorInactive = ta.getColor(R.styleable.BubbleToggleView_bt_colorInactive, colorInactive)
                isActive = ta.getBoolean(R.styleable.BubbleToggleView_bt_active, false)
                animationDuration = ta.getInteger(R.styleable.BubbleToggleView_bt_duration, DEFAULT_ANIM_DURATION)
                internalPadding =
                    ta.getDimension(R.styleable.BubbleToggleView_bt_padding, internalPadding.toFloat()).toInt()
                titlePadding =
                    ta.getDimension(R.styleable.BubbleToggleView_bt_titlePadding, titlePadding.toFloat()).toInt()
                badgeTextSize =
                    ta.getDimension(R.styleable.BubbleToggleView_bt_badgeTextSize, badgeTextSize.toFloat()).toInt()
                badgeBackgroundColor =
                    ta.getColor(R.styleable.BubbleToggleView_bt_badgeBackgroundColor, badgeBackgroundColor)
                badgeTextColor = ta.getColor(R.styleable.BubbleToggleView_bt_badgeTextColor, badgeTextColor)
                badgeText = ta.getString(R.styleable.BubbleToggleView_bt_badgeText)
            } finally {
                ta.recycle()
            }
        }

        //set the default icon
        if (icon == null)
            icon = ContextCompat.getDrawable(context, R.drawable.ic_done_grey)

        //set the default shape
        if (shape == null)
            shape = ContextCompat.getDrawable(context, R.drawable.transition_background_drawable)

        //create a default bubble item
        bubbleToggleItem.icon = icon
        bubbleToggleItem.shape = shape
        bubbleToggleItem.title = title.toString()
        bubbleToggleItem.titleSize = titleSize
        bubbleToggleItem.titlePadding = titlePadding
        bubbleToggleItem.shapeColor = shapeColor
        bubbleToggleItem.colorActive = colorActive
        bubbleToggleItem.colorInactive = colorInactive
        bubbleToggleItem.iconWidth = iconWidth
        bubbleToggleItem.iconHeight = iconHeight
        bubbleToggleItem.internalPadding = internalPadding
        bubbleToggleItem.badgeText = badgeText
        bubbleToggleItem.badgeBackgroundColor = badgeBackgroundColor
        bubbleToggleItem.badgeTextColor = badgeTextColor
        bubbleToggleItem.badgeTextSize = badgeTextSize.toFloat()

        //set the gravity
        gravity = Gravity.CENTER

        //set the internal padding
        setPadding(
            bubbleToggleItem.internalPadding,
            0,
            bubbleToggleItem.internalPadding,
            0
        )
        post {
            //make sure the padding is added
            setPadding(
                bubbleToggleItem.internalPadding,
                0,
                bubbleToggleItem.internalPadding,
                0
            )
        }

        createBubbleItemView(context)
        setInitialState(isActive)
    }

    /**
     * Create the components of the bubble item view [.iconView] and [.titleView]
     *
     * @param context current context
     */
    private fun createBubbleItemView(context: Context) {

        //create the nav icon
        iconView = ImageView(context)
        iconView.id = ViewCompat.generateViewId()
        val lpIcon =
            LayoutParams(bubbleToggleItem.iconWidth.toInt(), bubbleToggleItem.iconHeight.toInt())
        lpIcon.addRule(CENTER_VERTICAL, TRUE)
        iconView.layoutParams = lpIcon
        iconView.setImageDrawable(bubbleToggleItem.icon)

        //create the nav title
        titleView = TextView(context)
        val lpTitle = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        lpTitle.addRule(CENTER_VERTICAL, TRUE)
        lpTitle.addRule(END_OF, iconView.id)
        titleView.layoutParams = lpTitle
        titleView.setSingleLine(true)
        titleView.setTextColor(bubbleToggleItem.colorActive)
        titleView.text = bubbleToggleItem.title
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, bubbleToggleItem.titleSize)
        //get the current measured title width
        titleView.visibility = View.VISIBLE
        //update the margin of the text view
        titleView.setPadding(bubbleToggleItem.titlePadding, 0, 0, 0)
        //measure the content width
        titleView.measure(0, 0)       //must call measure!
        measuredTitleWidth = titleView.measuredWidth.toFloat()  //get width
        //limit measured width, based on the max width
        if (measuredTitleWidth > maxTitleWidth)
            measuredTitleWidth = maxTitleWidth
        measuredTitleWidth += 16f.pxValue(context = context)
        //change the visibility
        titleView.visibility = View.GONE
        addView(iconView)
        addView(titleView)


        //set the initial state
        setInitialState(isActive)
    }

    /**
     * Adds or removes the badge
     */

    /////////////////////////////////
    // PUBLIC METHODS
    ////////////////////////////////

    /**
     * Updates the Initial State
     *
     * @param isActive current state
     */
    fun setInitialState(isActive: Boolean) {
        //set the background
        background = bubbleToggleItem.shape

        if (isActive) {
            ViewUtils.updateDrawableColor(
                bubbleToggleItem.shape,
                ContextCompat.getColor(context, R.color.colorPrimary)
            )
            ViewUtils.updateDrawableColor(iconView.drawable, ContextCompat.getColor(context, android.R.color.white))

            this.isActive = true
            titleView.visibility = View.VISIBLE
            if (background is TransitionDrawable) {
                val trans = background as TransitionDrawable
                trans.startTransition(0)
            } else {
                if (!showShapeAlways && bubbleToggleItem.shapeColor != Integer.MIN_VALUE)
                    ViewUtils.updateDrawableColor(bubbleToggleItem.shape, bubbleToggleItem.shapeColor)
            }
        } else {
            ViewUtils.updateDrawableColor(
                bubbleToggleItem.shape,
                ContextCompat.getColor(context, R.color.toggle_gray)
            )
            ViewUtils.updateDrawableColor(
                iconView.drawable,
                ContextCompat.getColor(context, R.color.listing_bottombar_gray)
            )
            this.isActive = false
            titleView.visibility = View.GONE
            if (!showShapeAlways) {
                if (background !is TransitionDrawable) {
                    background = null
                } else {
                    val trans = background as TransitionDrawable
                    trans.resetTransition()
                }
            }
        }
    }

    /**
     * Toggles between Active and Inactive state
     */
    fun toggle(past: Boolean = false) {
        if (!isActive)
            activate()
        else
            deactivate(past)
    }

    /**
     * Set Active state
     */
    private fun activate() {
        ViewUtils.updateDrawableColor(bubbleToggleItem.shape, ContextCompat.getColor(context, R.color.colorPrimary))
        ViewUtils.updateDrawableColor(iconView.drawable, ContextCompat.getColor(context, android.R.color.white))
        isActive = true
        titleView.visibility = View.VISIBLE
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = animationDuration.toLong()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            titleView.width = (measuredTitleWidth * value).toInt()
            //end of animation
            if (value >= 1.0f) {
                //do something
            }
        }
        animator.start()
        if (background is TransitionDrawable) {
            val trans = background as TransitionDrawable
            trans.startTransition(animationDuration)
        } else {
            //if not showing Shape Always and valid shape color present, use that as tint
            if (!showShapeAlways && bubbleToggleItem.shapeColor != Integer.MIN_VALUE)
                ViewUtils.updateDrawableColor(bubbleToggleItem.shape, bubbleToggleItem.shapeColor)
            background = bubbleToggleItem.shape
        }
    }

    /**
     * Set Inactive State
     */
    fun deactivate(past: Boolean) {
        if (past) {
            ViewUtils.updateDrawableColor(bubbleToggleItem.shape, ContextCompat.getColor(context, R.color.ice_blue))
            ViewUtils.updateDrawableColor(iconView.drawable, ContextCompat.getColor(context, R.color.colorPrimary))
        } else {
            ViewUtils.updateDrawableColor(
                bubbleToggleItem.shape,
                ContextCompat.getColor(context, R.color.toggle_gray)
            )
            ViewUtils.updateDrawableColor(
                iconView.drawable,
                ContextCompat.getColor(context, R.color.listing_bottombar_gray)
            )
        }
        isActive = false

        val animator = ValueAnimator.ofFloat(1f, 0f)
        animator.duration = animationDuration.toLong()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            titleView.width = (measuredTitleWidth * value).toInt()
            //end of animation
            if (value <= 0.0f)
                titleView.visibility = View.GONE
        }
        animator.start()
        if (background is TransitionDrawable) {
            val trans = background as TransitionDrawable
            trans.reverseTransition(animationDuration)
        } else {
            if (!showShapeAlways) background = null
        }
    }

    /**
     * Sets the [Typeface] of the [.titleView]
     *
     * @param typeface to be used
     */
    fun setTitleTypeface(typeface: Typeface) {
        titleView.typeface = typeface
    }

    /**
     * Updates the measurements and fits the view
     *
     * @param maxWidth in pixels
     */
    fun updateMeasurements(maxWidth: Int) {
        var marginLeft = 0
        var marginRight = 0
        val titleViewLayoutParams = titleView.layoutParams
        if (titleViewLayoutParams is LayoutParams) {
            marginLeft = titleViewLayoutParams.rightMargin
            marginRight = titleViewLayoutParams.leftMargin
        }

        val newTitleWidth = ((maxWidth
                - (paddingRight + paddingLeft)
                - (marginLeft + marginRight)
                - bubbleToggleItem.iconWidth.toInt())
                + titleView.paddingRight + titleView.paddingLeft)

        //if the new calculate title width is less than current one, update the titleView specs
        if (newTitleWidth > 0 && newTitleWidth < measuredTitleWidth) {
            measuredTitleWidth = titleView.measuredWidth.toFloat()
        }
    }

    /**
     * Set value to the Badge's
     *
     * @param value as String, null to hide
     */
    fun setBadgeText(value: String) {
        bubbleToggleItem.badgeText = value
    }

    companion object {

        private const val TAG = "BNI_View"
        private const val DEFAULT_ANIM_DURATION = 300
    }

}