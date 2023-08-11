package com.babilonia.presentation.view.textpickerdialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.babilonia.R

// Created by Anton Yatsenko on 22.07.2019.
class TextPickerDialog(
    private var mContext: Context,
    private var titles: List<String>,
    seleted: Int,
    private var callBack: NumberPickerCallBack
) : Dialog(mContext), TextPickerAdapter.ItemClickCallBack, TextPickerAdapter.ValueAvailableListener {

    private lateinit var cancelView: TextView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var okView: TextView
    private lateinit var recyclerView: RecyclerView
    private var selectNumber = 0
    private lateinit var selectedTextView: String

    init {
        this.selectNumber = seleted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.title_picker_dialog)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        initViews()
        initValuesInViews()
        setOnClickListener()
    }

    override fun onItemClicked(value: String, position: Int) {
        this.selectNumber = titles.indexOf(value)
        selectedTextView = (value)
        linearLayoutManager.scrollToPositionWithOffset(position, 0)
    }

    override fun onValueAvailable(value: String, position: Int) {
        this.selectNumber = titles.indexOf(value)
        linearLayoutManager.scrollToPositionWithOffset(position, 0)
    }


    private fun initValuesInViews() {
        linearLayoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        selectedTextView = selectNumber.toString()

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        recyclerView.adapter = TextPickerAdapter(mContext, this, titles, titles[selectNumber], initList(titles))
        recyclerView.scrollToPosition(selectNumber)
    }

    private fun initList(titles: List<String>): List<TitleModel> {
        val dataList = mutableListOf<TitleModel>()
        titles.forEach {
            dataList.add(TitleModel(it, titles[selectNumber] == it))
        }
        return dataList
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        okView = findViewById(R.id.ok)
        cancelView = findViewById(R.id.cancel)

    }

    private fun setOnClickListener() {
        cancelView.setOnClickListener { dismiss() }
        okView.setOnClickListener {
            if (selectedTextView.isNotEmpty() && selectedTextView != "-") {
                callBack.onSelectingValue(titles[selectNumber])
                dismiss()
            }
        }
    }

    interface NumberPickerCallBack {
        fun onSelectingValue(value: String)
    }
}