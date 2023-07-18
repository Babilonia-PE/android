package com.babilonia.domain.repository

import com.babilonia.domain.model.enums.PaymentStatus
import com.babilonia.domain.model.payment.PaymentPlan
import io.reactivex.Flowable
import io.reactivex.Single

interface PaymentsRepository {
    fun getPaymentPlans(): Single<List<PaymentPlan>>

    fun createPaymentIntent(listingId: Long, productKey: String, publisherRole: String): Single<String>

    fun observePublishStatus(listingId: Long): Flowable<PaymentStatus>
}