package com.babilonia.data.datasource.system

import io.reactivex.Observable

interface OrientationProvider {
    fun getDeviceOrientation(): Observable<FloatArray>
}