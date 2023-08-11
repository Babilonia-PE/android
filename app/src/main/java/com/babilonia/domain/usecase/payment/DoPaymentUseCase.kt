package com.babilonia.domain.usecase.payment

import com.babilonia.domain.model.payment.DoPayment
import com.babilonia.domain.repository.PaymentsRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class DoPaymentUseCase @Inject constructor(
    private val paymentsRepository: PaymentsRepository
) : SingleUseCase<DoPayment, DoPaymentUseCase.Params>() {

    override fun buildUseCaseSingle(params: Params): Single<DoPayment> {
        return paymentsRepository.doPayment(
            params.deviceSessionId,
            params.paymentType,
            params.cardNumber,
            params.orderId,
            params.documentType,
            params.cardCvv,
            params.cardExpiration,
            params.cardName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class Params(
        val deviceSessionId: String?,
        val paymentType: String,
        val cardNumber: String,
        val orderId: Long?,
        val documentType: String,
        val cardCvv: String,
        val cardExpiration: String,
        val cardName: String)
}