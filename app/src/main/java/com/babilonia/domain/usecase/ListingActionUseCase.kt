package com.babilonia.domain.usecase

import com.babilonia.domain.model.enums.ListingAction
import com.babilonia.domain.model.enums.ListingActionMode
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.CompletableUseCase
import io.reactivex.Completable
import java.util.*
import javax.inject.Inject

// Created by Anton Yatsenko on 04.07.2019.
class ListingActionUseCase @Inject constructor(private val listingRepository: ListingRepository) :
    CompletableUseCase<ListingActionUseCase.Params>() {
    override fun buildUseCaseCompletable(params: Params): Completable {
        return if (params.mode == ListingActionMode.SET) {
            listingRepository.setListingAction(
                params.id ?: 0,
                params.action.name.toLowerCase(Locale.ROOT),
                params.ipAddress,
                params.userAgent,
                params.signProvider
            )
        } else {
            listingRepository.deleteListingAction(
                params.id ?: 0,
                params.action.name.toLowerCase(Locale.ROOT),
                params.ipAddress,
                params.userAgent,
                params.signProvider
            )
        }
    }

    class Params(
        val id: Long?,
        val action: ListingAction,
        val mode: ListingActionMode,
        val ipAddress: String,
        val userAgent: String,
        val signProvider: String
    )
}