package com.babilonia.domain.usecase

import com.babilonia.domain.model.Place
import com.babilonia.domain.repository.PlacesRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 17.07.2019.
class GetPlacesByQueryUseCase @Inject constructor(private val placesRepository: PlacesRepository) :
    SingleUseCase<List<Place>, FindAutocompletePredictionsRequest>() {
    override fun buildUseCaseSingle(params: FindAutocompletePredictionsRequest): Single<List<Place>> {
        return placesRepository.getPlacesByQuery(params)
    }
}