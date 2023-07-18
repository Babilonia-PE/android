package com.babilonia.data.network.model

import com.babilonia.data.network.model.json.RouteJson
import com.google.gson.annotations.SerializedName

class RouteResponse(
    @SerializedName("routes")
    val routes: List<RouteJson>? = null
)