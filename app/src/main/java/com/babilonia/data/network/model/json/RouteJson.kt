package com.babilonia.data.network.model.json

import com.babilonia.EmptyConstants
import com.google.gson.annotations.SerializedName

data class RouteJson(
    @SerializedName("legs")
    val legs: List<RouteLegJson>? = null
)

data class RouteLegJson(
    @SerializedName("steps")
    val steps: List<RouteStepJson>? = null
)

data class RouteStepJson(
    @SerializedName("start_location")
    val startLocation: RouteLocationJson? = null,

    @SerializedName("end_location")
    val endLocation: RouteLocationJson? = null
)

data class RouteLocationJson(
    @SerializedName("lat")
    val lat: Double = EmptyConstants.EMPTY_DOUBLE,

    @SerializedName("lng")
    val lon: Double = EmptyConstants.EMPTY_DOUBLE
)