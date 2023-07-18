package com.babilonia.domain.usecase.payment

import com.babilonia.domain.model.enums.PaymentStatus
import com.babilonia.domain.repository.PaymentsRepository
import com.babilonia.domain.usecase.base.FlowableUseCase
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetPublishStatusUseCase @Inject constructor(
    private val paymentsRepository: PaymentsRepository
) : FlowableUseCase<PaymentStatus, GetPublishStatusUseCase.Params>() {

    override fun buildUseCaseFlowable(params: Params): Flowable<PaymentStatus> {
        return paymentsRepository.observePublishStatus(params.listingId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class Params(val listingId: Long)
}