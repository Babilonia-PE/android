package com.babilonia.presentation.flow.main.publish.facilities.common

import com.babilonia.domain.model.Facility

// Created by Anton Yatsenko on 12.06.2019.
interface FacilityChangeListener {
    fun onChange(value: Facility?)
}