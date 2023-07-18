package com.babilonia.domain.usecase

import com.babilonia.domain.model.Listing
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 21.06.2019.
class CreateListingUseCase @Inject constructor(private val listingRepository: ListingRepository) :
    SingleUseCase<Listing, Listing>() {
    override fun buildUseCaseSingle(params: Listing): Single<Listing> {
        return listingRepository.createListing(params)
    }
}