package com.babilonia.data.repository

import com.babilonia.data.datasource.PlacesDataSourceRemote
import com.babilonia.data.mapper.PlacesMapper
import com.babilonia.data.mapper.RouteMapper
import com.babilonia.domain.model.Place
import com.babilonia.domain.model.PlaceLocation
import com.babilonia.domain.model.RouteStep
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.domain.repository.PlacesRepository
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import io.reactivex.Single
import java.util.*
import javax.inject.Inject


// Created by Anton Yatsenko on 17.07.2019.
internal typealias GooglePlaceField = com.google.android.libraries.places.api.model.Place.Field

class PlacesRepositoryImpl @Inject constructor(
    private val placesDataSourceRemote: PlacesDataSourceRemote,
    private val placesMapper: PlacesMapper,
    private val routeMapper: RouteMapper
) :
    PlacesRepository {
    override fun getCurrentPlace(): Single<PlaceLocation> {
        return placesDataSourceRemote.getCurrentPlace()
            .map { place ->
                placesMapper.mapPlaceToLocation(place)
            }
    }

    override fun getPlaceById(id: String): Single<PlaceLocation> {
        val fields = Arrays.asList(
            GooglePlaceField.ID,
            GooglePlaceField.NAME,
            GooglePlaceField.ADDRESS,
            GooglePlaceField.LAT_LNG,
            GooglePlaceField.VIEWPORT
        )
        val request = FetchPlaceRequest.builder(id, fields)
            .build()
        return placesDataSourceRemote.getPlaceById(request)
            .map { place ->
                placesMapper.mapPlaceToLocation(place)
            }
    }

    override fun getPlacesByQuery(query: FindAutocompletePredictionsRequest): Single<List<Place>> {
        return placesDataSourceRemote.getPlacesByQuery(query)
            .map {
                it.map { prediction -> Place(prediction.placeId, prediction.getFullText(null).toString()) }
            }
    }

    override fun getRoute(origin: ILocation, destination: ILocation): Single<List<RouteStep>> {
        return placesDataSourceRemote.getRoute(origin, destination).map {
            routeMapper.mapRemoteToDomain(it)
        }
    }
}