package com.babilonia.data.model.ar.tag

import android.graphics.RectF
import com.babilonia.ar.tag.ArTag
import com.babilonia.data.model.ar.Movable
import com.babilonia.data.model.ar.Sizebale
import com.babilonia.data.model.ar.navigator.Navigatable
import com.babilonia.data.model.geo.IGeoData
import com.babilonia.domain.model.Listing
import com.babilonia.domain.utils.ArTagType
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlin.properties.Delegates

class MovableArObject(listing: Listing, val geoData: IGeoData) : ArObject(listing), Movable, Sizebale, Navigatable {

    private val azimuthSubject = BehaviorSubject.create<Double>()
    private val distanceSubject = BehaviorSubject.create<Double>()
    private val sizeSubject = BehaviorSubject.create<RectF>()

    override val azimuthObservable: Observable<Double> = azimuthSubject
    override val distanceObservable: Observable<Double> = distanceSubject
    override var sizeObservable: Observable<RectF> = sizeSubject

    constructor(another: MovableArObject) : this(another.listing, another.geoData) {
        tagRect = RectF(another.tagRect)
        arTagSizeType = another.arTagSizeType
        azimuth = another.azimuth
        distance = another.distance
    }

    override var azimuth: Double by Delegates.observable(geoData.azimuth)
    { _, _, newValue ->
        azimuthSubject.onNext(newValue)
    }

    override var distance: Double by Delegates.observable(geoData.distance)
    { _, _, newValue ->
        distanceSubject.onNext(newValue)
    }

    override var tagRect: RectF by Delegates.observable(RectF(
        ArTag.HIDDEN_POSITION_COORDS,
        ArTag.HIDDEN_POSITION_COORDS,
        ArTag.HIDDEN_POSITION_COORDS,
        ArTag.HIDDEN_POSITION_COORDS
    ))
    { _, _, newValue ->
        sizeSubject.onNext(newValue)
    }

    override var arTagSizeType: ArTagType = ArTagType.INITIAL
}