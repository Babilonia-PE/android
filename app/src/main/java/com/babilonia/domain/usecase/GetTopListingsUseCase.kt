package com.babilonia.domain.usecase

import com.babilonia.domain.model.Listing
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetTopListingsUseCase @Inject constructor(private val listingRepository: ListingRepository) :
    SingleUseCase<List<Listing>, GetTopListingsUseCase.Params>() {
    override fun buildUseCaseSingle(params: Params): Single<List<Listing>> {
        return listingRepository.getTopListings(params.latitude, params.longitude, params.radius)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class Params(
        val latitude: Float,
        val longitude: Float,
        val radius: Int
    )
}