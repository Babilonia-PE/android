package com.babilonia.data.network.service

import com.babilonia.data.network.model.RouteResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface MapService {

    @GET("https://maps.googleapis.com/maps/api/directions/json")
    fun getRoute(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("sensor") sensor: Boolean = false,
        @Query("mode") mode: String = "walking",
        @Query("key") apiKey: String
    ): Single<RouteResponse>
}