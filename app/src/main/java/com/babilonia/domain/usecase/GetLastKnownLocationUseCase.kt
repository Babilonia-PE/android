package com.babilonia.domain.usecase

import com.babilonia.data.datasource.system.LocationProvider
import com.babilonia.data.model.DataResult
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 11.07.2019.
class GetLastKnownLocationUseCase @Inject constructor(private val locationProvider: LocationProvider) :
    SingleUseCase<DataResult<ILocation>, Unit>() {
    override fun buildUseCaseSingle(params: Unit): Single<DataResult<ILocation>> {
        return locationProvider.getLastKnownLocation()
    }
}