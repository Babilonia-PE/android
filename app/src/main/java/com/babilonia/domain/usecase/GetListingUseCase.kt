package com.babilonia.domain.usecase

import com.babilonia.domain.model.Listing
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 19.06.2019.
class GetListingUseCase @Inject constructor(private val listingRepository: ListingRepository) :
    SingleUseCase<Listing, GetListingUseCase.Params>() {
    override fun buildUseCaseSingle(params: Params): Single<Listing> {
        return when {
            params.mode == ListingDisplayMode.PREVIEW -> listingRepository.getDraftListingById(params.id)
            params.local -> listingRepository.getLocalListing(params.id)
            params.mode == ListingDisplayMode.PUBLISHED ||
            params.mode == ListingDisplayMode.UNPUBLISHED -> {
                listingRepository.getMyListingById(params.id)
            }
            else -> listingRepository.getListingById(params.id)
        }
    }

    class Params(val id: Long, val mode: ListingDisplayMode, val local: Boolean = false)
}