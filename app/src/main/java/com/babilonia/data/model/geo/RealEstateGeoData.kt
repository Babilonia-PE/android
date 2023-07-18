package com.babilonia.data.model.geo

import com.babilonia.ar.LocationHelper
import com.babilonia.domain.model.Location
import com.babilonia.domain.model.geo.ILocation

/**
 * The representation of local east, north, up (ENU) coordinates
 */
data class RealEstateGeoData(
    private val currentPosition: ILocation,
    override val objectLocation: ILocation
) : IGeoData {

    override val azimuth: Double = LocationHelper.azimuthBetween(currentPosition, objectLocation)
    override val distance: Double = LocationHelper.distanceBetween(currentPosition, objectLocation)
    override val coordinatesENU: FloatArray

    init {

        //Maybe in the future, the altitude will be taken into account.
        val positionWithoutAltitude: ILocation = Location(
            latitude = currentPosition.latitude,
            longitude = currentPosition.longitude
        )

        val currentLocationInECEF = LocationHelper.WSG84toECEF(positionWithoutAltitude)
        val objectLocationInECEF = LocationHelper.WSG84toECEF(objectLocation)

        coordinatesENU = LocationHelper.ECEFtoENU(positionWithoutAltitude, currentLocationInECEF, objectLocationInECEF)
    }
}