package com.babilonia.domain.usecase

import com.babilonia.domain.model.enums.ListingAction
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.CompletableUseCase
import io.reactivex.Completable
import java.util.*
import javax.inject.Inject

class SendActionUseCase @Inject constructor(private val listingRepository: ListingRepository) :
    CompletableUseCase<SendActionUseCase.Params>() {
    override fun buildUseCaseCompletable(params: Params): Completable {
        return listingRepository.setContactAction(
            params.id ?: 0,
            params.action.name.toLowerCase(Locale.ROOT),
            params.ipAddress,
            params.userAgent,
            params.signProvider
        )
    }

    class Params(
        val id: Long?,
        val action: ListingAction,
        val ipAddress: String,
        val userAgent: String,
        val signProvider: String
    )
}