package com.babilonia.domain.model

import com.babilonia.Constants
import com.babilonia.EmptyConstants
import com.babilonia.domain.model.geo.ILocation
import com.google.android.gms.maps.model.LatLngBounds

// Created by Anton Yatsenko on 26.07.2019.
data class PlaceLocation(
    override var latitude: Double = Constants.LIMA_LAT,
    override var longitude: Double = Constants.LIMA_LON,
    override var altitude: Double = EmptyConstants.ZERO_DOUBLE,
    override var address: String? = EmptyConstants.EMPTY_STRING,
    var viewport: LatLngBounds? = null,
    var googlePlaceId: String? = null
) : ILocation {

    companion object {
        val emptyLocation = Location()
    }
}