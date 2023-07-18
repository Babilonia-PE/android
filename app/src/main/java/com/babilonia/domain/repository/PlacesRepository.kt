package com.babilonia.domain.repository

import com.babilonia.domain.model.Place
import com.babilonia.domain.model.PlaceLocation
import com.babilonia.domain.model.RouteStep
import com.babilonia.domain.model.geo.ILocation
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import io.reactivex.Single

// Created by Anton Yatsenko on 17.07.2019.
interface PlacesRepository {
    fun getPlacesByQuery(query: FindAutocompletePredictionsRequest): Single<List<Place>>
    fun getPlaceById(id: String): Single<PlaceLocation>
    fun getCurrentPlace(): Single<PlaceLocation>
    fun getRoute(origin: ILocation, destination: ILocation): Single<List<RouteStep>>
}