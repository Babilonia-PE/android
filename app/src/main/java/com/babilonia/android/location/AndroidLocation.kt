package com.babilonia.android.location

import android.location.Location
import com.babilonia.EmptyConstants
import com.babilonia.domain.model.geo.ILocation

class AndroidLocation(val androidLocation: Location) : ILocation {

    override var latitude: Double = androidLocation.latitude
    override var longitude: Double = androidLocation.longitude
    override var altitude: Double = androidLocation.altitude
    override var address: String? = EmptyConstants.EMPTY_STRING
    override var department: String? = EmptyConstants.EMPTY_STRING
    override var province: String? = EmptyConstants.EMPTY_STRING
    override var district: String? = EmptyConstants.EMPTY_STRING
    override var zipCode: String? = EmptyConstants.EMPTY_STRING
    override var country: String? = EmptyConstants.EMPTY_STRING

    override fun toString(): String {
        return "latitude = ${latitude}; longitude = ${longitude}; altitude = ${altitude};"
    }
}