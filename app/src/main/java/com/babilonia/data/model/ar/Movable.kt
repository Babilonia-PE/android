package com.babilonia.data.model.ar

import io.reactivex.Observable

interface Movable {
    val azimuthObservable: Observable<Double>
    val distanceObservable: Observable<Double>
}