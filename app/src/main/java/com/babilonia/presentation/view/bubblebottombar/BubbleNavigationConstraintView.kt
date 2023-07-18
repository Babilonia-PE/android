package com.babilonia.presentation.view.bubblebottombar

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import com.babilonia.R
import com.babilonia.presentation.extension.pxValue
import com.babilonia.presentation.view.bubblebottombar.listener.BubbleNavigationChangeListener
import timber.log.Timber


// Created by Anton Yatsenko on 12.06.2019.
class BubbleNavigationConstraintView : ConstraintLayout, IBubbleNavigation {

    private var bubbleNavItems = mutableListOf<BubbleToggleView>()
    private var navigationChangeListener: BubbleNavigationChangeListener? = null

    /**
     * Gets the current active position
     *
     * @return active item position
     */
    override var currentActiveItemPosition = 0
    private var loadPreviousState: Boolean = false

    //default display mode
    private var displayMode = DisplayMode.SPREAD

    private var currentTypeface: Typeface? = null

    private var pendingBadgeUpdate: SparseArray<String>? = null

    internal enum class DisplayMode {
        SPREAD,
        INSIDE,
        PACKED
    }

    /**
     * Constructors
     */
    constructor(@NonNull context: Context) : super(context) {
        init(context, null)
    }

    constructor(@NonNull context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(@NonNull context: Context, @Nullable attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putInt("current_item", currentActiveItemPosition)
        bundle.putBoolean("load_prev_state", true)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var state = state
        if (state is Bundle) {
            val bundle = state as Bundle?
            currentActiveItemPosition = bundle?.getInt("current_item") ?: 0
            loadPreviousState = bundle?.getBoolean("load_prev_state") ?: false
            state = bundle?.getParcelable("superState")
        }
        super.onRestoreInstanceState(state)
    }

    /////////////////////////////////////////
    // PRIVATE METHODS
    /////////////////////////////////////////

    /**
     * Initialize
     *
     * @param context current context
     * @param attrs   custom attributes
     */
    private fun init(context: Context, attrs: AttributeSet?) {
        var mode = 0
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.BubbleNavigationConstraintView, 0, 0)
            try {
                mode = ta.getInteger(R.styleable.BubbleNavigationConstraintView_bnc_mode, mode)
            } finally {
                ta.recycle()
            }
        }

        //sets appropriate display node
        if (mode >= 0 && mode < DisplayMode.values().size)
            displayMode = DisplayMode.values()[mode]

        post { updateChildNavItems() }
    }

    /**
     * Get the chain type from the display mode
     *
     * @param mode display mode
     * @return the constraint chain mode
     */
    private fun getChainTypeFromMode(mode: DisplayMode): Int {
        return when (mode) {
            DisplayMode.SPREAD -> ConstraintSet.CHAIN_SPREAD
            DisplayMode.INSIDE -> ConstraintSet.CHAIN_SPREAD_INSIDE
            DisplayMode.PACKED -> ConstraintSet.CHAIN_PACKED
        }

        return ConstraintSet.CHAIN_SPREAD
    }

    /**
     * Finds Child Elements of type [BubbleToggleView] and adds them to [.bubbleNavItems]
     */
    private fun updateChildNavItems() {
        for (index in 0 until childCount) {
            val view = getChildAt(index)
            if (view is BubbleToggleView)
                bubbleNavItems.add(view)
            else {
                return
            }
        }

        setClickListenerForItems()
        setInitialActiveState()
        updateMeasurementForItems()
        createChains()
        currentTypeface = ResourcesCompat.getFont(context, R.font.avenir_heavy)
        currentTypeface?.let { setTypeface(it) }
    }

    /**
     * Creates the chains to spread the [.bubbleNavItems] based on the [.displayMode]
     */
    private fun createChains() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(this)

        val chainIdsList = IntArray(bubbleNavItems.size)
        val chainWeightList = FloatArray(bubbleNavItems.size)

        for (i in 0 until bubbleNavItems.size) {
            val id = bubbleNavItems[i].id
            chainIdsList[i] = id
            chainWeightList[i] = 0.0f
            //set the top and bottom constraint for each items
            constraintSet.connect(id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
            constraintSet.connect(id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        }

        //create an horizontal chain
        constraintSet.createHorizontalChain(
            id, ConstraintSet.LEFT,
            id, ConstraintSet.RIGHT,
            chainIdsList, chainWeightList,
            getChainTypeFromMode(displayMode)
        )

        //apply the constraint
        constraintSet.applyTo(this)
    }

    /**
     * Makes sure that ONLY ONE child [.bubbleNavItems] is active
     */
    private fun setInitialActiveState() {

        if (bubbleNavItems == null) return

        var foundActiveElement = false

        // find the initial state
        if (!loadPreviousState) {
            for (i in 0 until bubbleNavItems.size) {
                if (bubbleNavItems[i].isActive && !foundActiveElement) {
                    foundActiveElement = true
                    currentActiveItemPosition = i
                } else {
                    bubbleNavItems[i].setInitialState(false)
                }
            }
        } else {
            for (i in 0 until bubbleNavItems.size) {
                bubbleNavItems[i].setInitialState(false)
            }
        }
        //set the active element
        if (!foundActiveElement)
            post {
                setCurrentActiveItem(currentActiveItemPosition, true)
            }
    }

    /**
     * Update the measurements of the child components [.bubbleNavItems]
     */
    private fun updateMeasurementForItems() {
        val numChildElements = bubbleNavItems.size
        if (numChildElements > 0) {
            for (btv in bubbleNavItems)
                btv.updateMeasurements(124f.pxValue(context = context).toInt())
        }
    }

    private fun setClickListenerForItems() {

    }

    /**
     * Gets the Position of the Child from [.bubbleNavItems] from its id
     *
     * @param id of view to be searched
     * @return position of the Item
     */
    fun getItemPositionById(id: Int): Int {
        for (i in 0 until bubbleNavItems.size)
            if (id == bubbleNavItems[i].id)
                return i
        return -1
    }

    ///////////////////////////////////////////
    // PUBLIC METHODS
    ///////////////////////////////////////////

    /**
     * Set the navigation change listener [BubbleNavigationChangeListener]
     *
     * @param navigationChangeListener sets the passed parameters as listener
     */
    override fun setNavigationChangeListener(navigationChangeListener: BubbleNavigationChangeListener) {
        this.navigationChangeListener = navigationChangeListener
    }

    /**
     * Set the [Typeface] for the Text Elements of the View
     *
     * @param typeface to be used
     */
    override fun setTypeface(typeface: Typeface) {
        if (bubbleNavItems != null) {
            for (btv in bubbleNavItems)
                btv.setTitleTypeface(typeface)
        } else {
            currentTypeface = typeface
        }
    }

    /**
     * Sets the current active item
     *
     * @param position current position change
     */
    override fun setCurrentActiveItem(position: Int, restoreState: Boolean) {

        if (bubbleNavItems == null) {
            currentActiveItemPosition = position
            return
        }

        if (currentActiveItemPosition == position && restoreState.not()) return

        if (position < 0 || position >= bubbleNavItems.size)
            return

        val btv = bubbleNavItems[position]
        onBubbleClick(btv, restoreState)
    }

    /**
     * Sets the badge value
     *
     * @param position current position change
     * @param value    value to be set in the badge
     */
    override fun setBadgeValue(position: Int, value: String) {
        if (bubbleNavItems != null) {
            val btv = bubbleNavItems[position]
            btv.setBadgeText(value)
        } else {
            if (pendingBadgeUpdate == null)
                pendingBadgeUpdate = SparseArray()
            pendingBadgeUpdate?.put(position, value)
        }
    }

    fun onBubbleClick(v: View, restoreState: Boolean = false) {
        val changedPosition = getItemPositionById(v.id)
        if (changedPosition >= 0) {
            if (changedPosition == currentActiveItemPosition && restoreState.not()) {
                return
            }
            val newActiveToggleView = bubbleNavItems[changedPosition]
            for (i in 0 until changedPosition) {
                if (i != changedPosition)
                    bubbleNavItems[i].deactivate(true)
            }
            for (i in changedPosition until bubbleNavItems.size) {
                if (i != changedPosition)
                    bubbleNavItems[i].deactivate(false)
            }
            newActiveToggleView.toggle()

            //changed the current active position
            currentActiveItemPosition = changedPosition

            if (navigationChangeListener != null)
                navigationChangeListener?.onNavigationChanged(v, currentActiveItemPosition)
        } else {
            Timber.tag(TAG).w("Selected id not found! Cannot toggle")
        }
    }

    companion object {

        //constants
        private const val TAG = "BNLView"
        private const val MIN_ITEMS = 2
        private const val MAX_ITEMS = 5
    }
}