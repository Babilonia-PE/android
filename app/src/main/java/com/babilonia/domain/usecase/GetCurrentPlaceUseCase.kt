package com.babilonia.domain.usecase

import com.babilonia.domain.model.PlaceLocation
import com.babilonia.domain.repository.PlacesRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 19.07.2019.
class GetCurrentPlaceUseCase @Inject constructor(private val placesRepository: PlacesRepository) :
    SingleUseCase<PlaceLocation, Unit>() {
    override fun buildUseCaseSingle(params: Unit): Single<PlaceLocation> {
        return placesRepository.getCurrentPlace()
    }
}