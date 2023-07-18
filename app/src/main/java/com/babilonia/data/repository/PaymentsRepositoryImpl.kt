package com.babilonia.data.repository

import com.babilonia.EmptyConstants
import com.babilonia.data.datasource.PaymentsDataSourceRemote
import com.babilonia.data.mapper.PaymentPlanMapper
import com.babilonia.domain.model.enums.PaymentStatus
import com.babilonia.domain.model.payment.PaymentPlan
import com.babilonia.domain.repository.PaymentsRepository
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PaymentsRepositoryImpl @Inject constructor(
    private val paymentsDataSourceRemote: PaymentsDataSourceRemote,
    private val paymentPlanMapper: PaymentPlanMapper
) : PaymentsRepository {

    override fun getPaymentPlans(): Single<List<PaymentPlan>> {
        return paymentsDataSourceRemote.getPaymentPlans().map { response ->
            response.data.records.map { paymentPlanMapper.mapRemoteToDomain(it) }
        }
    }

    override fun createPaymentIntent(
        listingId: Long,
        productKey: String,
        publisherRole: String
    ): Single<String> {
        return paymentsDataSourceRemote.createPaymentIntent(listingId, productKey, publisherRole)
            .map { it.data.clientSecret ?: EmptyConstants.EMPTY_STRING }
    }

    override fun observePublishStatus(listingId: Long): Flowable<PaymentStatus> {
        return paymentsDataSourceRemote.getPublishStatus(listingId)
            .map {
                it.data.status?.let { statusString ->
                    PaymentStatus.valueOf(statusString.toUpperCase())
                } ?: PaymentStatus.NEW
            }
            .repeatWhen { flowable ->
                flowable.take(PUBLISH_STATUS_MAX_ATTEMPTS)
                    .delay(PUBLISH_STATUS_CHECK_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            }
    }

    companion object {
        private const val PUBLISH_STATUS_MAX_ATTEMPTS = 15L
        private const val PUBLISH_STATUS_CHECK_DELAY_MILLIS = 3000L
    }
}