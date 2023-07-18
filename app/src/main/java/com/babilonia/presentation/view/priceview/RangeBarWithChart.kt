package com.babilonia.presentation.view.priceview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.babilonia.R
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import kotlinx.android.synthetic.main.item_range_bar.view.*
import java.util.*

// Created by Anton Yatsenko on 25.07.2019.
typealias ChartEntry = com.github.mikephil.charting.data.BarEntry

class RangeBarWithChart @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr),
    SimpleRangeView.OnTrackRangeListener {

    var onLeftPinChanged: ((index: Int, leftPinValue: Long?) -> Unit)? = null
    var onRangeChanged: ((leftPinValue: String?, rightPinValue: String?) -> Unit)? = null
    var onRightPinChanged: ((index: Int, rightPinValue: Long?) -> Unit)? = null
    var onSelectedItemsSizeChanged: ((sizeItemsSelected: Int) -> Unit)? = null
    var onSelectedEntriesSizeChanged: ((entriesSize: Int) -> Unit)? = null
    var rangeEntries = mutableListOf<BarEntry>()

    /**
     * Background color of selected part in chart
     * */
    var chartSelectedBackgroundColor: Int =
        ContextCompat.getColor(context, R.color.colorChartSelected)

    /**
     * Background color of not selected part in chart
     * */
    var chartUnselectedBackgroundColor: Int =
        ContextCompat.getColor(context, R.color.colorChartUnselected)

    /**
     * Color of selected line part in chart
     * */
    var chartSelectedLineColor: Int =
        ContextCompat.getColor(context, R.color.colorChartSelectedLine)

    /**
     * Color of not selected line part in chart
     * */
    var chartUnSelectedLineColor: Int =
        ContextCompat.getColor(context, R.color.colorChartUnselectedLine)

    /**
     * Color of selected part of rangebar
     * */
    var selectedSeekColor: Int = ContextCompat.getColor(context, R.color.colorRangeSelected)
        set(value) {
            field = value
            applyRangeBarStyle()
        }

    /**
     * Color of not selected part of rangebar
     * */
    var unselectedSeekColor: Int =
        ContextCompat.getColor(context, R.color.colorChartUnselectedLine)
        set(value) {
            field = value
            applyRangeBarStyle()
        }

    /**
     * Color of active thumb in rangebar
     * */
    var thumbColor: Int = ContextCompat.getColor(context, R.color.colorRangeSelected)
        set(value) {
            field = value
            applyRangeBarStyle()
        }

    /**
     * Color of active thumb in rangebar
     * */
    var thumbActiveColor: Int = ContextCompat.getColor(context, R.color.colorRangeSelected)
        set(value) {
            field = value
            applyRangeBarStyle()
        }

    /**
     * Radius of active tick in rangebar
     * */
    var tickRadius: Float =
        resources.getDimensionPixelSize(R.dimen.default_active_tick_radius).toFloat()
        set(value) {
            field = value
            applyRangeBarStyle()
        }

    private var entries: ArrayList<ChartEntry> = ArrayList()
    private var leftUnselectedDataSet: ArrayList<ChartEntry> = ArrayList()
    private var rightUnselectedDataSet: ArrayList<ChartEntry> = ArrayList()
    private var selectedDataSet: ArrayList<ChartEntry> = ArrayList()
    private var mainData: BarData? = null

    private var oldLeftPinIndex = 0
    private var oldRightPinIndex = 0

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.item_range_bar, this, true)

        attrs?.let {
            parseAttr(it)
        }

        initChart()
    }

    override fun onEndRangeChanged(rangeView: SimpleRangeView, rightPinIndex: Int) {
        onRangeChange(oldLeftPinIndex, rightPinIndex)
    }

    override fun onStartRangeChanged(rangeView: SimpleRangeView, leftPinIndex: Int) {
        onRangeChange(leftPinIndex, oldRightPinIndex)
    }

    /**
     * Set the data to display
     * */
    fun setEntries(entries: MutableList<BarEntry>) {
        rangeEntries = entries
        this.entries.clear()
        this.entries.addAll(entries.map { ChartEntry(it.count.toFloat(), it.listingsFound.toFloat()) })
        initRangeBar()
    }

    /**
     * Apply style for rangebar
     * */
    private fun applyRangeBarStyle() {
        elementRangeBar.apply {
            activeLineColor = selectedSeekColor
            lineColor = unselectedSeekColor
        }
        elementRangeBar.activeThumbColor = thumbColor
        elementRangeBar.activeFocusThumbColor = thumbActiveColor
        elementRangeBar.activeTickRadius = tickRadius
    }

    /**
     * Calculate all selected items
     * */
    private fun calculateSelectedItemsSize() {
        var totalSelectedSize = 0
        selectedDataSet.forEach { entry ->
            totalSelectedSize += entry.y.toInt()
        }
        onSelectedItemsSizeChanged?.invoke(totalSelectedSize)
    }

    /**
     * Calculate selected entries
     * */
    private fun calculateSelectedEntriesSize() {
        onSelectedEntriesSizeChanged?.invoke(selectedDataSet.size)
    }

    /**
     * Prepare data and draw chart
     * */
    private fun drawChart() {
        var leftDataSet = BarDataSet(leftUnselectedDataSet, "")
        leftDataSet = styleDataSet(dataSet = leftDataSet, isSelected = false)

        var centerDataSet = BarDataSet(selectedDataSet, "")
        centerDataSet = styleDataSet(dataSet = centerDataSet, isSelected = true)

        var rightDataSet = BarDataSet(rightUnselectedDataSet, "")
        rightDataSet = styleDataSet(dataSet = rightDataSet, isSelected = false)

        mainData = BarData()
        mainData?.barWidth = 1f
        if (leftUnselectedDataSet.isNotEmpty()) {
            mainData?.addDataSet(leftDataSet)
        }
        if (selectedDataSet.isNotEmpty()) {
            mainData?.addDataSet(centerDataSet)
        }
        if (rightUnselectedDataSet.isNotEmpty()) {
            mainData?.addDataSet(rightDataSet)
        }
        chart.apply {
            data = mainData
            xAxis.spaceMax = 0f
            legend.isEnabled = false
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.isGranularityEnabled = false
            xAxis.labelCount = 0
            xAxis.isEnabled = false
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            setFitBars(true)
            axisLeft.isEnabled = false
            isClickable = false
            data.isHighlightEnabled = false
            setDrawMarkers(false)
            setDrawGridBackground(false)
        }

        chart.invalidate()
    }

    /**
     * Initialize chart
     * */
    private fun initChart() {
        chart.apply {
            setPinchZoom(false)
            setScaleEnabled(false)
            isDoubleTapToZoomEnabled = false
        }
    }

    /**
     * Initialize rangebar
     * */
    private fun initRangeBar() {
        elementRangeBar.apply {
            onTrackRangeListener = this@RangeBarWithChart
            count = entries.size
            start = 0
            end = entries.size + 1
        }
        onRangeChange(0, entries.size + 1)

        drawChart()
    }

    /**
     * Draw chart and calculate all data.
     * @param leftPinIndex passed left pin index from seekbar
     * @param rightPinIndex passed right pin index from seekbar
     * */
    fun onRangeChange(leftPinIndex: Int, rightPinIndex: Int) {
        leftUnselectedDataSet.clear()
        selectedDataSet.clear()
        rightUnselectedDataSet.clear()

        entries.forEachIndexed { index, item ->
            if (index < leftPinIndex) {
                leftUnselectedDataSet.add(item)
            }

            if (index in leftPinIndex..rightPinIndex) {
                selectedDataSet.add(item)
            }

            if (index > rightPinIndex) {
                rightUnselectedDataSet.add(item)
            }
        }

        if ((leftPinIndex >= 0 && leftPinIndex < entries.size)
            && (rightPinIndex >= 0 && rightPinIndex < entries.size)
        ) {
            val leftVal = entries[leftPinIndex].x.toInt().toString()
            val rightVal = entries[rightPinIndex].x.toInt().toString()
            onRangeChanged?.invoke(leftVal, rightVal)
        }

        if (oldLeftPinIndex != leftPinIndex) {
            if (leftPinIndex >= 0 && leftPinIndex < entries.size) {
                onLeftPinChanged?.invoke(
                    leftPinIndex,
                    rangeEntries[leftPinIndex].priceStart.toLong()
                )
            }
            oldLeftPinIndex = leftPinIndex
        }
        if (oldRightPinIndex != rightPinIndex) {
            if (rightPinIndex >= 0 && rightPinIndex < entries.size) {
                onRightPinChanged?.invoke(
                    rightPinIndex,
                    rangeEntries[rightPinIndex].priceStart.toLong()
                )
            }
            oldRightPinIndex = rightPinIndex
        }

        calculateSelectedItemsSize()
        calculateSelectedEntriesSize()
        drawChart()
    }

    /**
     * Styling data for chart
     * @param dataSet passed prepared data for chart
     * @param isSelected indicate selected part of chart
     * */
    private fun styleDataSet(dataSet: BarDataSet, isSelected: Boolean = false): BarDataSet {
        if (!isSelected) {
            dataSet.apply {
                //fillColor = chartUnselectedBackgroundColor
                color = chartUnSelectedLineColor
            }
        } else {
            dataSet.apply {
                // fillColor = chartSelectedBackgroundColor
                color = chartSelectedLineColor
            }
        }
        dataSet.apply {
            //  setDrawCircles(false)
            setDrawValues(false)
            //setDrawFilled(true)
        }

        return dataSet
    }

    /**
     * Parse attributes from xml.
     * @param attrs passed attributes from XML file
     * */
    @SuppressLint("CustomViewStyleable")
    private fun parseAttr(attrs: AttributeSet) {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.PriceRangeBar)

        chartSelectedBackgroundColor = typedArray.getColor(
            R.styleable.PriceRangeBar_barChartSelectedBackgroundColor,
            chartSelectedBackgroundColor
        )

        chartUnselectedBackgroundColor = typedArray.getColor(
            R.styleable.PriceRangeBar_barChartUnselectedBackgroundColor,
            chartUnselectedBackgroundColor
        )

        chartSelectedLineColor = typedArray.getColor(
            R.styleable.PriceRangeBar_barChartSelectedLineColor,
            chartSelectedLineColor
        )

        chartUnSelectedLineColor = typedArray.getColor(
            R.styleable.PriceRangeBar_barChartUnSelectedLineColor,
            chartUnSelectedLineColor
        )

        selectedSeekColor = typedArray.getColor(
            R.styleable.PriceRangeBar_barActiveLineColor,
            selectedSeekColor
        )

        unselectedSeekColor = typedArray.getColor(
            R.styleable.PriceRangeBar_barLineColor,
            unselectedSeekColor
        )

        thumbColor = typedArray.getColor(
            R.styleable.PriceRangeBar_barThumbColor,
            thumbColor
        )

        thumbActiveColor = typedArray.getColor(
            R.styleable.PriceRangeBar_barActiveThumbColor,
            thumbActiveColor
        )

        tickRadius = typedArray.getDimension(
            R.styleable.PriceRangeBar_barActiveTickRadius,
            tickRadius
        )

        typedArray.recycle()
    }
}