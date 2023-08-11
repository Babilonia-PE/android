package com.babilonia.domain.usecase.payment

import com.babilonia.domain.model.payment.CreateOrder
import com.babilonia.domain.repository.PaymentsRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CreateNewOrderUseCase @Inject constructor(
    private val paymentsRepository: PaymentsRepository
) : SingleUseCase<CreateOrder, CreateNewOrderUseCase.Params>() {

    override fun buildUseCaseSingle(params: Params): Single<CreateOrder> {
        return paymentsRepository.createNewOrder(
            params.request,
            params.paymentMethod,
            params.paymentId,
            params.productKey,
            params.listingId,
            params.publisherRole,
            params.clientId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class Params(
        val request: String,
        val paymentMethod: String,
        val paymentId: String?,
        val productKey: String?,
        val listingId: Long,
        val publisherRole: String,
        val clientId: Long
    )
}