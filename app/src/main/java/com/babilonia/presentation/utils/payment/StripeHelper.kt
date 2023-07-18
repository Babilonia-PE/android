package com.babilonia.presentation.utils.payment

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.StripeIntent

class StripeHelper {
    private lateinit var stripe: Stripe
    private lateinit var onSuccessCallback: () -> Unit
    private lateinit var onErrorCallback: (String?) -> Unit


    fun initStripe(applicationContext: Context, publishKey: String) {
        stripe = Stripe(applicationContext, publishKey)
    }

    fun setPaymentCallbacks(onSuccess: () -> Unit, onError: (String?) -> Unit) {
        onSuccessCallback = onSuccess
        onErrorCallback = onError
    }

    fun pay(fragment: Fragment, confirmParams: ConfirmPaymentIntentParams) {
        stripe.confirmPayment(fragment, confirmParams)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        stripe.onPaymentResult(requestCode, data, object : ApiResultCallback<PaymentIntentResult> {
            override fun onSuccess(result: PaymentIntentResult) {
                val paymentIntent = result.intent
                val status = paymentIntent.status
                if (status == StripeIntent.Status.Succeeded) {
                    onSuccessCallback()
                } else if (status == StripeIntent.Status.RequiresPaymentMethod) {
                    val error = paymentIntent.lastPaymentError?.message.orEmpty()
                    onErrorCallback(error)
                }
            }

            override fun onError(e: Exception) {
                onErrorCallback(e.message)
            }
        })
    }
}