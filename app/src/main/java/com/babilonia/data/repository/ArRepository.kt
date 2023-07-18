package com.babilonia.data.repository

import com.babilonia.data.model.DataResult
import com.babilonia.data.model.ar.ArState
import com.babilonia.data.model.geo.LocationRequest
import io.reactivex.Observable
import io.reactivex.Single

interface ArRepository {
    fun getArObjects(
        cameraProjectionMatrix: FloatArray,
        locationRequest: LocationRequest,
        sceneWidth: Int,
        sceneHeight: Int
    ): Observable<DataResult<ArState>>

    fun getNavigationTarget(
        cameraProjectionMatrix: FloatArray,
        locationRequest: LocationRequest,
        sceneWidth: Int,
        sceneHeight: Int,
        selectedId: Long
    ): Observable<DataResult<ArState>>

    fun needToShowOnboarding(): Single<Boolean>
}