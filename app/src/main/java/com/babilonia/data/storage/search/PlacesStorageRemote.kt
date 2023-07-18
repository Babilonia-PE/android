package com.babilonia.data.storage.search

import android.content.Context
import com.babilonia.BuildConfig
import com.babilonia.data.datasource.GooglePlace
import com.babilonia.data.datasource.PlacesDataSourceRemote
import com.babilonia.data.network.model.RouteResponse
import com.babilonia.data.network.service.MapService
import com.babilonia.data.repository.GooglePlaceField
import com.babilonia.domain.model.geo.ILocation
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

// Created by Anton Yatsenko on 17.07.2019.
class PlacesStorageRemote @Inject constructor(
    private val placesClient: PlacesClient,
    private val mapService: MapService,
    private val context: Context
) : PlacesDataSourceRemote {
    override fun getPlaceById(request: FetchPlaceRequest): Single<GooglePlace> {
        return Single.create { subscriber ->
            placesClient.fetchPlace(request)
                .addOnSuccessListener {
                    subscriber.onSuccess(it.place)
                }.addOnFailureListener { subscriber.onError(it) }
        }
    }

    override fun getPlacesByQuery(query: FindAutocompletePredictionsRequest): Single<List<AutocompletePrediction>> {
        return Single.create { subscriber ->
            placesClient.findAutocompletePredictions(query)
                .addOnSuccessListener {
                    subscriber.onSuccess(it.autocompletePredictions)
                }.addOnFailureListener { subscriber.onError(it) }
        }
    }

    override fun getCurrentPlace(): Single<GooglePlace> {
        val fields = Arrays.asList(
            GooglePlaceField.ID,
            GooglePlaceField.NAME,
            GooglePlaceField.ADDRESS,
            GooglePlaceField.LAT_LNG,
            GooglePlaceField.VIEWPORT
        )
        val request = FindCurrentPlaceRequest.builder(fields).build()
        return Single.create { subscriber ->
            placesClient.findCurrentPlace(request)
                .addOnSuccessListener {
                    subscriber.onSuccess(it.placeLikelihoods.first().place)
                }.addOnFailureListener { subscriber.onError(it) }
        }
    }

    override fun getRoute(origin: ILocation, destination: ILocation): Single<RouteResponse> {
        return mapService.getRoute(
            origin = "${origin.latitude},${origin.longitude}",
            destination = "${destination.latitude},${destination.longitude}",
            apiKey = BuildConfig.DIRECTIONS_KEY
        )
    }
}