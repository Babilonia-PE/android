package com.babilonia.domain.usecase

import com.babilonia.domain.model.ListingImage
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 18.06.2019.
class UploadListingImageUseCase @Inject constructor(private val listingRepository: ListingRepository) :
    SingleUseCase<List<ListingImage>, String>() {
    override fun buildUseCaseSingle(params: String): Single<List<ListingImage>> {
        return listingRepository.uploadListingImage(params)
    }
}