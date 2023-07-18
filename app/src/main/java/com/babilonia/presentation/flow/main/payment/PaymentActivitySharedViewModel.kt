package com.babilonia.presentation.flow.main.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.domain.model.enums.PaymentProfile
import com.babilonia.domain.model.enums.PaymentStatus
import com.babilonia.domain.model.payment.AdProduct
import com.babilonia.domain.model.payment.PaymentPlan
import com.babilonia.domain.usecase.payment.CreatePaymentIntentUseCase
import com.babilonia.domain.usecase.payment.GetPaymentPlansUseCase
import com.babilonia.domain.usecase.payment.GetPublishStatusUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.subscribers.DisposableSubscriber
import javax.inject.Inject

class PaymentActivitySharedViewModel @Inject constructor(
    private val getPaymentPlansUseCase: GetPaymentPlansUseCase,
    private val getPublishStatusUseCase: GetPublishStatusUseCase,
    private val createPaymentIntentUseCase: CreatePaymentIntentUseCase
) : BaseViewModel() {

    private val paymentPlansLiveData = MutableLiveData<List<PaymentPlan>>()
    private val clientSecretLiveData = MutableLiveData<String>()
    private val errorLiveData = SingleLiveEvent<String>()
    private val paymentSuccessfulLiveData = SingleLiveEvent<Boolean>()

    var listingId = EmptyConstants.EMPTY_LONG
    var selectedProfile: PaymentProfile = PaymentProfile.OWNER
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

    fun createPaymentIntent() {
        selectedPeriod?.let { period ->
            createPaymentIntentUseCase.execute(object : DisposableSingleObserver<String>() {
                override fun onSuccess(clientSecret: String) {
                    clientSecretLiveData.value = clientSecret
                }

                override fun onError(e: Throwable) {
                    errorLiveData.value = e.message
                }
            }, CreatePaymentIntentUseCase.Params(listingId, period.key, selectedProfile.name.toLowerCase()))
        }
    }

    fun checkPublishStatus() {
        getPublishStatusUseCase.execute(object : DisposableSubscriber<PaymentStatus>() {
            override fun onComplete() {
                paymentSuccessfulLiveData.value = false
            }

            override fun onNext(status: PaymentStatus) {
                if (status == PaymentStatus.SUCCEEDED) {
                    getPublishStatusUseCase.dispose()
                    paymentSuccessfulLiveData.value = true
                } else if (status == PaymentStatus.PAYMENT_FAILED) {
                    getPublishStatusUseCase.dispose()
                    paymentSuccessfulLiveData.value = false
                }
            }

            override fun onError(e: Throwable) {
                errorLiveData.value = e.message
            }
        }, GetPublishStatusUseCase.Params(listingId))
    }

    fun onPaymentPeriodSelected(selectedIndex: Int) {
        selectedPlan?.let {
            selectedPeriod = it.products[selectedIndex]
            navigate(R.id.action_paymentPeriodFragment_to_checkoutFragment)
        }
    }

    fun getPaymentPlansLiveData(): LiveData<List<PaymentPlan>> = paymentPlansLiveData
    fun getClientSecretLiveData(): LiveData<String> = clientSecretLiveData
    fun getErrorLiveData(): LiveData<String> = errorLiveData
    fun getPaymentSuccessfulLiveData(): LiveData<Boolean> = paymentSuccessfulLiveData
}