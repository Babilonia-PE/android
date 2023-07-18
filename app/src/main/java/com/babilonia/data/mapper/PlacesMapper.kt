package com.babilonia.data.mapper

import com.babilonia.EmptyConstants
import com.babilonia.domain.model.PlaceLocation
import com.google.android.libraries.places.api.model.Place
import javax.inject.Inject

// Created by Anton Yatsenko on 23.07.2019.
class PlacesMapper @Inject constructor() {
    fun mapPlaceToLocation(place: Place): PlaceLocation {
        return PlaceLocation().apply {
            latitude = place.latLng?.latitude ?: EmptyConstants.ZERO_DOUBLE
            longitude = place.latLng?.longitude ?: EmptyConstants.ZERO_DOUBLE
            address = place.address
            viewport = place.viewport
            googlePlaceId = place.id
        }
    }
}