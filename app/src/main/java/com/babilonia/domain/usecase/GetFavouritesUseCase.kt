package com.babilonia.domain.usecase

import com.babilonia.domain.model.Listing
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.FlowableUseCase
import io.reactivex.Flowable
import javax.inject.Inject

// Created by Anton Yatsenko on 08.07.2019.
class GetFavouritesUseCase @Inject constructor(private val listingRepository: ListingRepository) :
    FlowableUseCase<List<Listing>, Unit>() {
    override fun buildUseCaseFlowable(params: Unit): Flowable<List<Listing>> {
        return listingRepository.getFavouriteListings()
    }
}