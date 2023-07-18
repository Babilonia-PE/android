package com.babilonia.domain.usecase

import com.babilonia.domain.model.Facility
import com.babilonia.domain.model.Filter
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.ObservableUseCase
import com.babilonia.presentation.flow.main.search.model.ListingsMetadata
import io.reactivex.Observable
import javax.inject.Inject

// Created by Anton Yatsenko on 24.07.2019.
class GetListingsMetadataUseCase @Inject constructor(private val listingRepository: ListingRepository) :
    ObservableUseCase<ListingsMetadata, GetListingsMetadataUseCase.Params>() {
    override fun buildUseCaseObservable(params: Params): Observable<ListingsMetadata> {
        return listingRepository.getListingsMetadata(
            params.lat, params.lon, params.radius, params.filters, params.facilities
        )
    }

    class Params(
        val lat: Float,
        val lon: Float,
        val radius: Int,
        val filters: List<Filter>,
        val facilities: List<Facility>
    )
}