package com.babilonia.domain.usecase

import com.babilonia.domain.model.RecentSearch
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetRecentSearchesUseCase @Inject constructor(private val listingRepository: ListingRepository) :
    SingleUseCase<List<RecentSearch>, Unit>() {

    override fun buildUseCaseSingle(params: Unit): Single<List<RecentSearch>> {
        return listingRepository.getRecentSearch()
    }
}