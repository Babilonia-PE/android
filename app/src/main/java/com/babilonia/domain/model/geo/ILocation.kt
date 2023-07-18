package com.babilonia.domain.model.geo

interface ILocation {
    var latitude: Double
    var longitude: Double
    var altitude: Double
    var address: String?
}