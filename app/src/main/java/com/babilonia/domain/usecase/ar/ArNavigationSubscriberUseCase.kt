package com.babilonia.domain.usecase.ar

import com.babilonia.data.model.DataResult
import com.babilonia.data.model.ar.ArState
import com.babilonia.data.model.geo.LocationRequest
import com.babilonia.data.repository.ArRepository
import com.babilonia.domain.usecase.base.SubscriberUseCase
import io.reactivex.Observable
import javax.inject.Inject

class ArNavigationSubscriberUseCase @Inject constructor(private val arRepository: ArRepository) :
    SubscriberUseCase<DataResult<ArState>, ArNavigationSubscriberUseCase.Params>() {

    override fun buildSubscriptionUseCase(params: Params): Observable<DataResult<ArState>> {
        return arRepository.getNavigationTarget(
            params.cameraProjectionMatrix,
            params.locationRequest,
            params.sceneWidth,
            params.sceneHeight,
            params.selectedId
        )
    }

    class Params(
        val locationRequest: LocationRequest,
        val cameraProjectionMatrix: FloatArray,
        val sceneWidth: Int,
        val sceneHeight: Int,
        val selectedId: Long
    )
}

