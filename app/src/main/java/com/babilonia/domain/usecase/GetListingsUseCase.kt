package com.babilonia.domain.usecase

import com.babilonia.Constants
import com.babilonia.domain.model.Facility
import com.babilonia.domain.model.Filter
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.enums.SortType
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 08.07.2019.
class GetListingsUseCase @Inject constructor(private val listingRepository: ListingRepository) :
    SingleUseCase<List<Listing>, GetListingsUseCase.Params>() {
    override fun buildUseCaseSingle(params: Params): Single<List<Listing>> {
        return with (params) {
            listingRepository.getListings(
                lat,
                lon,
                queryText,
                placeId,
                page,
                pageSize,
                radius,
                sortType,
                filters,
                facilities,
                department,
                province,
                district,
                address
            )
        }
    }

    class Params(
        val lat: Float?,
        val lon: Float?,
        val queryText: String?,
        val placeId: String?,
        val page: Int = 1,
        val radius: Int?,
        val sortType: SortType,
        val filters: List<Filter>,
        val facilities: List<Facility>,
        val pageSize: Int = Constants.PER_PAGE,
        val department: String?,
        val province: String?,
        val district: String?,
        val address: String?)
}