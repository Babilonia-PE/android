package com.babilonia.domain.usecase.payment

import com.babilonia.domain.model.payment.PaymentPlan
import com.babilonia.domain.repository.PaymentsRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetPaymentPlansUseCase @Inject constructor(
    private val paymentsRepository: PaymentsRepository
) : SingleUseCase<List<PaymentPlan>, Unit>() {

    override fun buildUseCaseSingle(params: Unit): Single<List<PaymentPlan>> {
        return paymentsRepository.getPaymentPlans()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}