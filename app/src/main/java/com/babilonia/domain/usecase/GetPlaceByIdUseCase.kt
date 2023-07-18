package com.babilonia.domain.usecase

import com.babilonia.domain.model.PlaceLocation
import com.babilonia.domain.repository.PlacesRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 17.07.2019.
class GetPlaceByIdUseCase @Inject constructor(private val placesRepository: PlacesRepository) :
    SingleUseCase<PlaceLocation, String>() {
    override fun buildUseCaseSingle(params: String): Single<PlaceLocation> {
        return placesRepository.getPlaceById(params)
    }
}