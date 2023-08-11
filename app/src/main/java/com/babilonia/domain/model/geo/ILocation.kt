package com.babilonia.domain.model.geo

import com.google.android.gms.maps.model.LatLngBounds

interface ILocation {
    var latitude: Double
    var longitude: Double
    var altitude: Double
    var address: String?
    var department: String?
    var province: String?
    var district: String?
    var zipCode: String?
    var country: String?
}