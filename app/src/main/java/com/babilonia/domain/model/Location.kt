package com.babilonia.domain.model

import com.babilonia.Constants
import com.babilonia.EmptyConstants
import com.babilonia.domain.model.geo.ILocation

// Created by Anton Yatsenko on 24.06.2019.
data class Location(
    override var latitude: Double = Constants.LIMA_LAT,
    override var longitude: Double = Constants.LIMA_LON,
    override var altitude: Double = EmptyConstants.ZERO_DOUBLE,
    override var address: String? = EmptyConstants.EMPTY_STRING
) : ILocation {

    companion object {
        val emptyLocation = Location()
    }
}