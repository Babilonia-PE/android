package com.babilonia.data.datasource.system

import io.reactivex.Observable

interface RotationProvider {
    fun getWindowRotation(): Observable<Pair<FloatArray, FloatArray>>
}