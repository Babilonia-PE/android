package com.babilonia.presentation.view.numberpicker

// Created by Anton Yatsenko on 04.06.2019.
interface ClickNumberPickerListener {

    /**
     * Listen on picker value change
     * @param previousValue of the picker
     * @param currentValue of the picker
     * @param pickerClickType tells if value was increased on decreased
     */
    fun onValueChange(previousValue: Int, currentValue: Int, pickerClickType: PickerClickType)

}