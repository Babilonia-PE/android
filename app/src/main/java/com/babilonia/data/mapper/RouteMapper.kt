package com.babilonia.data.mapper

import com.babilonia.data.network.model.RouteResponse
import com.babilonia.domain.model.RouteStep
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class RouteMapper @Inject constructor() {

    fun mapRemoteToDomain(response: RouteResponse): List<RouteStep> {
        val routes = response.routes

        if (routes.isNullOrEmpty()) {
            return emptyList()
        }

        val legs = routes[0].legs

        if (legs.isNullOrEmpty()) {
            return emptyList()
        }

        val steps = legs[0].steps

        if (steps.isNullOrEmpty()) {
            return emptyList()
        }

        val mappedSteps = arrayListOf<RouteStep>()
        steps.forEach { step ->
            if (step.startLocation != null && step.endLocation != null) {
                mappedSteps.add(
                    RouteStep(
                        LatLng(step.startLocation.lat, step.startLocation.lon),
                        LatLng(step.endLocation.lat, step.endLocation.lon)
                    )
                )
            }
        }

        return mappedSteps
    }
}