package com.babilonia.domain.usecase

import com.babilonia.domain.model.DataLocation
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

// Created by Renso Contreras on 14.07.2021.
class GetDataLocationSearchedUseCase @Inject constructor(private val listingRepository: ListingRepository) :
    SingleUseCase<DataLocation, GetDataLocationSearchedUseCase.Params>() {
    override fun buildUseCaseSingle(params: Params): Single<DataLocation> {
        return with (params) {
            listingRepository.getDataLocationSearched(
                address,
                page,
                perPage
            )
        }
    }

    class Params(
        val address: String,
        val page: Int,
        val perPage: Int
    )
}