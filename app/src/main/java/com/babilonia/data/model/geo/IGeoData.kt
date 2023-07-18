package com.babilonia.data.model.geo

import com.babilonia.domain.model.geo.ILocation

interface IGeoData {
    val objectLocation: ILocation
    val azimuth: Double
    val distance: Double
    val coordinatesENU: FloatArray
}