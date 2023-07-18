package com.babilonia.data.datasource

import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.geo.ILocation
import io.reactivex.Observable

interface RealEstateDataSource {
    //TODO Add and use some kind of model which will describe the real estate object instead of ILocation
    fun getRealEstateList(currentLocation: ILocation): Observable<List<Listing>>
}