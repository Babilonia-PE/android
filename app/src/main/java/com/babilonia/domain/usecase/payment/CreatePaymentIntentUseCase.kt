package com.babilonia.domain.usecase.payment

import com.babilonia.domain.model.payment.PaymentIntent
import com.babilonia.domain.repository.PaymentsRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CreatePaymentIntentUseCase @Inject constructor(
    private val paymentsRepository: PaymentsRepository
) : SingleUseCase<PaymentIntent, CreatePaymentIntentUseCase.Params>() {

    override fun buildUseCaseSingle(params: Params): Single<PaymentIntent> {
        return paymentsRepository.createPaymentIntent(
            params.request,
            params.listingId,
            params.productKey,
            params.publisherRole,
            params.clientId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class Params(
        val request: String,
        val listingId: Long,
        val productKey: String?,
        val publisherRole: String,
        val clientId: Long
    )
}