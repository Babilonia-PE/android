package com.babilonia.domain.usecase

import com.babilonia.domain.model.ListingImage
import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 26.06.2019.
// Modified by Alex Quevedo on 29.02.2023.
class UploadImagesUseCase @Inject constructor(private val authRepository: AuthRepository) :
    SingleUseCase<List<ListingImage>, UploadImagesUseCase.Params>() {
    override fun buildUseCaseSingle(params: Params): Single<List<ListingImage>> {
        return authRepository.uploadImages(
            params.image,
            params.type
        )
    }

    class Params(val image: String, val type: String)
}