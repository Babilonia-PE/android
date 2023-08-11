package com.babilonia.domain.usecase

import com.babilonia.domain.model.Facility
import com.babilonia.domain.model.Filter
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import com.babilonia.presentation.view.priceview.BarEntry
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 25.07.2019.
class GetPriceRangeUseCase @Inject constructor(private val listingRepository: ListingRepository) :
    SingleUseCase<List<BarEntry>, GetPriceRangeUseCase.Params>() {
    override fun buildUseCaseSingle(params: Params): Single<List<BarEntry>> {
        return listingRepository.getListingsPriceRange(
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