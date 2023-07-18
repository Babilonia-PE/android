package com.babilonia.domain.usecase

import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

// Created by Anton Yatsenko on 08.07.2019.
class DeleteDraftUseCase @Inject constructor(private val listingRepository: ListingRepository) :
    CompletableUseCase<Long>() {
    override fun buildUseCaseCompletable(params: Long): Completable {
        return listingRepository.deleteDraftById(params)
    }
}