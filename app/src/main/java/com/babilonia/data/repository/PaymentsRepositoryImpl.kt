package com.babilonia.data.repository

import com.babilonia.data.datasource.PaymentsDataSourceRemote
import com.babilonia.data.mapper.CreateOrderMapper
import com.babilonia.data.mapper.DoPaymentMapper
import com.babilonia.data.mapper.PaymentIntentMapper
import com.babilonia.data.mapper.PaymentPlanMapper
import com.babilonia.data.network.error.mapErrors
import com.babilonia.data.network.error.mapNetworkErrors
import com.babilonia.domain.model.payment.CreateOrder
import com.babilonia.domain.model.payment.DoPayment
import com.babilonia.domain.model.payment.PaymentIntent
import com.babilonia.domain.model.payment.PaymentPlan
import com.babilonia.domain.repository.PaymentsRepository
import io.reactivex.Single
import javax.inject.Inject

class PaymentsRepositoryImpl @Inject constructor(
    private val paymentsDataSourceRemote: PaymentsDataSourceRemote,
    private val paymentIntentMapper: PaymentIntentMapper,
    private val createOrderMapper: CreateOrderMapper,
    private val doPaymentMapper: DoPaymentMapper,
    private val paymentPlanMapper: PaymentPlanMapper
) : PaymentsRepository {

    override fun createPaymentIntent(
        request: String, listingId: Long, productKey: String?, publisherRole: String, clientId: Long
    ): Single<PaymentIntent> {
        return paymentsDataSourceRemote.createPaymentIntent(request, listingId, productKey, publisherRole, clientId)
            .mapNetworkErrors()
            .mapErrors()
            .map { paymentIntentMapper.mapRemoteToDomain(it) }
    }

    override fun createNewOrder(request: String, paymentMethod: String, paymentId: String?, productKey: String?, listingId: Long, publisherRole: String, clientId: Long
    ): Single<CreateOrder> {
        return paymentsDataSourceRemote.createNewOrder(request, paymentMethod, paymentId, productKey, listingId, publisherRole, clientId)
            .mapNetworkErrors()
            .mapErrors()
            .map { createOrderMapper.mapRemoteToDomain(it) }
    }

    override fun doPayment(
        deviceSessionId: String?,
        paymentType: String,
        cardNumber: String,
        orderId: Long?,
        documentType: String,
        cardCvv: String,
        cardExpiration: String,
        cardName: String
    ): Single<DoPayment> {
        return paymentsDataSourceRemote.doPayment(deviceSessionId, paymentType, cardNumber, orderId, documentType, cardCvv, cardExpiration, cardName)
            .mapNetworkErrors()
            .mapErrors()
            .map { doPaymentMapper.mapRemoteToDomain(it) }
    }

    override fun getPaymentPlans(): Single<List<PaymentPlan>> {
        return paymentsDataSourceRemote.getPaymentPlans()
            .mapNetworkErrors()
            .mapErrors()
            .map { listAddPlanJson -> listAddPlanJson.map { paymentPlanMapper.mapRemoteToDomain(it) }
            }
    }
}