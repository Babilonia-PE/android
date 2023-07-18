package com.babilonia.presentation.view.picker

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.babilonia.R


// Created by Anton Yatsenko on 06.06.2019.
class NumberPickerDialog(
    private var mContext: Context,
    start: Int,
    last: Int,
    seleted: Int,
    private var callBack: NumberPickerCallBack
) : Dialog(mContext), NumberPickerAdapter.ItemClickCallBack, NumberPickerAdapter.ValueAvailableListener {
    internal var selectNumber = 0
    internal var start = 0
    internal var last = 0

    internal lateinit var recyclerView: RecyclerView
    private lateinit var okView: TextView
    private lateinit var cancelView: TextView
    private lateinit var selectedTextView: String

    private lateinit var linearLayoutManager: LinearLayoutManager

    init {
        if (start > last) {
            throw IllegalStateException("Start value must be smaller than last value")
        }
        this.selectNumber = seleted
        this.start = start
        this.last = last
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.interval_picker_dialog)
        window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        initViews()
        initValues()
        initValuesInViews()
        setOnClickListener()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        okView = findViewById(R.id.ok)
        cancelView = findViewById(R.id.cancel)

    }

    private fun initValues() {

    }

    private fun initValuesInViews() {
        linearLayoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        if (last - start <= -1)
            recyclerView.setItemViewCacheSize(100000)
        else
            recyclerView.setItemViewCacheSize(last - start)
        selectedTextView = selectNumber.toString()

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        recyclerView.adapter = NumberPickerAdapter(mContext, this, this, start, last, selectNumber)
        recyclerView.scrollToPosition(last - selectNumber)
    }

    private fun setOnClickListener() {


        cancelView.setOnClickListener { dismiss() }
        okView.setOnClickListener {
            if (selectedTextView.isEmpty() || selectedTextView == "-") {
            } else {
                callBack.onSelectingValue(selectNumber)
                dismiss()
            }
        }
    }

    override fun onItemClicked(value: Int, position: Int) {
        this.selectNumber = value
        selectedTextView = (value.toString())
        linearLayoutManager.scrollToPositionWithOffset(position, 0)
    }

    override fun onValueAvailable(value: Int, position: Int) {
        this.selectNumber = value
        linearLayoutManager.scrollToPositionWithOffset(position, 0)
    }

    interface NumberPickerCallBack {
        fun onSelectingValue(value: Int)
    }
}