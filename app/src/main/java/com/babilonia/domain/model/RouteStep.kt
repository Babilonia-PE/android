package com.babilonia.domain.model

import com.google.android.gms.maps.model.LatLng

data class RouteStep(
    val startLocation: LatLng,
    val endLocation: LatLng
)