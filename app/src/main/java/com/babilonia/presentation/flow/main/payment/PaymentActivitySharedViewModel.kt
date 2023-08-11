package com.babilonia.presentation.flow.main.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.domain.model.enums.*
import com.babilonia.domain.model.payment.*
import com.babilonia.domain.usecase.payment.CreatePaymentIntentUseCase
import com.babilonia.domain.usecase.payment.DoPaymentUseCase
import com.babilonia.domain.usecase.payment.GetPaymentPlansUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import io.reactivex.observers.DisposableSingleObserver
import javax.inject.Inject

class PaymentActivitySharedViewModel @Inject constructor(
    private val getPaymentPlansUseCase: GetPaymentPlansUseCase,
    private val createPaymentIntentUseCase: CreatePaymentIntentUseCase,
    private val doPaymentUseCase: DoPaymentUseCase
    ) : BaseViewModel() {

    private val paymentPlansLiveData      = MutableLiveData<List<PaymentPlan>>()
    private val errorLiveData             = SingleLiveEvent<String?>()
    private val paymentIntentLiveData     = SingleLiveEvent<PaymentIntent>()
    private val doPaymentLiveData         = SingleLiveEvent<DoPayment>()
    private val hideLoadingLiveData       = SingleLiveEvent<Void>()

    private val selectPaymentMethod: PaymentMethod = PaymentMethod.PAYU
    private val selectPaymentType: PaymentType = PaymentType.CARD
    private val selectDocumentType: DocumentType = DocumentType.TICKET
    var selectRequest: MyPaymentRequest = MyPaymentRequest.LISTING
    var selectedProfile: PaymentProfile = PaymentProfile.OWNER

    var userId        = EmptyConstants.EMPTY_LONG
    var listingId     = EmptyConstants.EMPTY_LONG
    var publisherRole = EmptyConstants.EMPTY_STRING
    var selectedPlan: PaymentPlan? = null
    var selectedPeriod: AdProduct? = null

    fun navigateToSelectPaymentPlan() {
        navigate(R.id.action_paymentProfileFragment_to_paymentPlanFragment)
    }

    fun navigateToSelectPaymentPeriod() {
        navigate(R.id.action_paymentPlanFragment_to_paymentPeriodFragment)
    }

    fun getPaymentPlans() {
        getPaymentPlansUseCase.execute(object : DisposableSingleObserver<List<PaymentPlan>>() {
            override fun onSuccess(paymentPlans: List<PaymentPlan>) {
                paymentPlansLiveData.value = paymentPlans
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }
        }, Unit)
    }

    fun onPaymentPeriodSelected(selectedIndex: Int) {
        selectedPlan?.let {
            selectedPeriod = it.products[selectedIndex]
            navigate(R.id.action_paymentPeriodFragment_to_checkoutFragment)
        }
    }

    fun createPaymentIntent() {
        createPaymentIntentUseCase.execute(object : DisposableSingleObserver<PaymentIntent>() {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                paymentIntentLiveData.value = paymentIntent
            }

            override fun onError(e: Throwable) {
                errorLiveData.value = e.message
                //hideLoadingLiveData.call()
            }

        }, CreatePaymentIntentUseCase.Params(selectRequest.name.toLowerCase(), listingId, selectedPeriod?.key, selectedProfile.name.toLowerCase(), userId))
    }

    fun doPayment(cardNumber: String, cardCvv: String, cardExpiration: String, cardName: String) {
        doPaymentUseCase.execute(object : DisposableSingleObserver<DoPayment>() {
            override fun onSuccess(doPayment: DoPayment) {
                doPaymentLiveData.value = doPayment
            }

            override fun onError(e: Throwable) {
                errorLiveData.value = e.message
                //hideLoadingLiveData.call()
            }

        }, DoPaymentUseCase.Params(
            getPaymentIntent()?.paymentIntentId, selectPaymentType.name.toLowerCase(),
            cardNumber, getPaymentIntent()?.orderId?.toLong(), selectDocumentType.name.toLowerCase(),
            cardCvv, cardExpiration, cardName))
    }

    fun getPaymentPlansLiveData(): LiveData<List<PaymentPlan>> = paymentPlansLiveData
    fun getPaymentIntentLiveData(): LiveData<PaymentIntent> = paymentIntentLiveData
    fun getDoPaymentLiveData(): LiveData<DoPayment> = doPaymentLiveData
    fun getErrorLiveData(): LiveData<String?> = errorLiveData
    fun hideLoadingLiveData(): LiveData<Void> = hideLoadingLiveData

    private fun getPaymentIntent(): PaymentIntent? {
        return paymentIntentLiveData.value
    }
}
