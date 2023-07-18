package com.babilonia.domain.usecase.payment

import com.babilonia.domain.repository.PaymentsRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CreatePaymentIntentUseCase @Inject constructor(
    private val paymentsRepository: PaymentsRepository
) : SingleUseCase<String, CreatePaymentIntentUseCase.Params>() {

    override fun buildUseCaseSingle(params: Params): Single<String> {
        return paymentsRepository.createPaymentIntent(
            params.listingId,
            params.productKey,
            params.publisherRole
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class Params(
        val listingId: Long,
        val productKey: String,
        val publisherRole: String
    )
}