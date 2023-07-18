package com.babilonia.data.datasource

import com.babilonia.data.network.model.RouteResponse
import com.babilonia.domain.model.geo.ILocation
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import io.reactivex.Single

// Created by Anton Yatsenko on 17.07.2019.
internal typealias GooglePlace = Place

interface PlacesDataSourceRemote {
    fun getPlacesByQuery(query: FindAutocompletePredictionsRequest): Single<List<AutocompletePrediction>>
    fun getPlaceById(request: FetchPlaceRequest): Single<GooglePlace>
    fun getCurrentPlace(): Single<GooglePlace>
    fun getRoute(origin: ILocation, destination: ILocation): Single<RouteResponse>
}