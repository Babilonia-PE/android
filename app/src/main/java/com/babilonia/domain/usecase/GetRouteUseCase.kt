package com.babilonia.domain.usecase

import com.babilonia.domain.model.RouteStep
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.domain.repository.PlacesRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetRouteUseCase @Inject constructor(private val placesRepository: PlacesRepository) :
    SingleUseCase<List<RouteStep>, GetRouteUseCase.Params>() {
    override fun buildUseCaseSingle(params: Params): Single<List<RouteStep>> {
        return placesRepository.getRoute(params.origin, params.destination)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class Params(
        val origin: ILocation,
        val destination: ILocation
    )
}