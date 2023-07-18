package com.babilonia.domain.usecase

import com.babilonia.domain.model.Listing
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 04.07.2019.
class UpdateListingUseCase @Inject constructor(private val listingRepository: ListingRepository) :
    SingleUseCase<Listing, Listing>() {
    override fun buildUseCaseSingle(params: Listing): Single<Listing> {
        return listingRepository.updateListing(params)
    }
}