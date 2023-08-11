package com.babilonia.domain.usecase

import com.babilonia.domain.model.Listing
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.FlowableUseCase
import io.reactivex.Flowable
import javax.inject.Inject

// Created by Anton Yatsenko on 21.06.2019.
class GetMyListingsUseCase @Inject constructor(private val listingRepository: ListingRepository) :
    FlowableUseCase<List<Listing>, GetMyListingsUseCase.Params>() {
    override fun buildUseCaseFlowable(params: Params): Flowable<List<Listing>> {
        return listingRepository.getMyListings(state = params.state)
    }

    class Params(
        val state: String
    )
}