package com.babilonia.presentation.flow.main.payment.checkout

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputFilter
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import com.babilonia.BuildConfig
import com.babilonia.R
import com.babilonia.databinding.FragmentCheckoutBinding
import com.babilonia.domain.model.enums.PaymentPlanKey
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.flow.main.payment.PaymentActivitySharedViewModel
import com.babilonia.presentation.utils.PriceFormatter
import com.babilonia.presentation.utils.payment.StripeHelper
import com.stripe.android.model.CardBrand
import com.stripe.android.model.CardParams
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethodCreateParams
import kotlinx.android.synthetic.main.fragment_checkout.*

class CheckoutFragment : BaseFragment<FragmentCheckoutBinding, PaymentActivitySharedViewModel>() {

    private var currentCardBrand = CardBrand.Unknown
    private val stripeHelper = StripeHelper()
    private var paymentParams: PaymentMethodCreateParams? = null
    private var progressDialog: AlertDialog? = null

    override fun viewCreated() {
        setToolbar()
        fillUiFields()
        setCardBrand(CardBrand.Unknown)
        listenToCardBrandChange()
        initStripe()
        setOnPayClickListener()
        observeViewModel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        stripeHelper.onActivityResult(requestCode, resultCode, data)
    }

    private fun observeViewModel() {
        viewModel.getClientSecretLiveData().observe(this, Observer { pay(it) })
        viewModel.getErrorLiveData().observe(this, Observer { errorMessage -> // our backend error. Unused because has no localization.
            hideProgress()
            showErrorDialog(resources.getString(R.string.error_processing_payment_backend), true)
        })
        viewModel.getPaymentSuccessfulLiveData().observe(this, Observer { paymentSuccessful ->
            if (paymentSuccessful) {
                hideProgress()
                activity?.let {
                    it.setResult(Activity.RESULT_OK)
                    it.finish()
                }
            } else {
                hideProgress()
                showErrorDialog(resources.getString(R.string.error_processing_payment_backend), true)
            }
        })
    }

    private fun fillUiFields() {
        viewModel.selectedPeriod?.let { period ->
            val formattedPrice = String.format(PriceFormatter.PRICE_TWO_DIGITS_AFTER_POINT, period.price)
            tvPaymentAmount.text = getString(R.string.sol_amount, formattedPrice)
            tvPaymentPeriod.text = resources.getQuantityString(
                R.plurals.checkout_plan_duration_days_plural, period.duration, period.duration.toString()
            )
            btnPay.text = getString(R.string.pay_amount, formattedPrice)
        }
        viewModel.selectedPlan?.let { plan ->
            tvPlanName.text = getString(R.string.plan_title, plan.title)
            when (plan.key) {
                PaymentPlanKey.STANDARD -> {
                    ivPlanIcon.invisible()
                    tvPlanName.setTextColor(
                        ResourcesCompat.getColor(resources, R.color.dark_blue_grey, null)
                    )
                }
                PaymentPlanKey.PLUS -> {
                    ivPlanIcon.setImageResource(R.drawable.ic_payment_plan_plus_28)
                    applyPlanIconTint(R.color.paymentPlanPlus)
                    ivPlanIcon.visible()
                    tvPlanName.setTextColor(
                        ResourcesCompat.getColor(resources, R.color.paymentPlanPlus, null)
                    )
                }
                PaymentPlanKey.PREMIUM -> {
                    ivPlanIcon.setImageResource(R.drawable.ic_payment_plan_premium_28)
                    applyPlanIconTint(R.color.paymentPlanPremium)
                    ivPlanIcon.visible()
                    tvPlanName.setTextColor(
                        ResourcesCompat.getColor(resources, R.color.paymentPlanPremium, null)
                    )
                }
            }
        }
    }

    private fun listenToCardBrandChange() {
        etStripeCardNumber.doOnTextChanged { _, _, _, _ ->
            if (currentCardBrand != etStripeCardNumber.cardBrand) {
                setCardBrand(etStripeCardNumber.cardBrand)
            }
        }
    }

    private fun initStripe() {
        activity?.let {
            stripeHelper.initStripe(it.applicationContext, BuildConfig.STRIPE_KEY)
            stripeHelper.setPaymentCallbacks(
                onSuccess = {
                    viewModel.checkPublishStatus()
                },
                onError = { errorMessage -> // Stripe error message. Unused because has no localization.
                    hideProgress()
                    showErrorDialog(resources.getString(R.string.error_processing_payment_stripe), false)
                })
        }
    }

    private fun pay(clientSecret: String) {
        paymentParams?.let {
            stripeHelper.pay(
                this,
                ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(it, clientSecret)
            )
            paymentParams = null
        }
    }

    private fun setOnPayClickListener() {
        btnPay.setOnClickListener {
            if (isPaymentDataValid()) {
                showProgress()
                paymentParams = createPaymentParams()
                viewModel.createPaymentIntent()
            }
        }
    }

    private fun isPaymentDataValid(): Boolean {
        when {
            isCardNumberValid().not() -> etStripeCardNumber.requestFocus()
            isExpiryDateValid().not() -> etExpiryDate.requestFocus()
            isCvcValid().not() -> etCvc.requestFocus()
            isCardholderNameValid().not() -> etCardholderName.requestFocus()
            else -> {
                return true
            }
        }
        return false
    }

    private fun createPaymentParams(): PaymentMethodCreateParams? {
        return getExpiryDate()?.let { expiryDate ->
            PaymentMethodCreateParams.createCard(
                CardParams(
                    getCardNumber(),
                    expiryDate.month,
                    expiryDate.year,
                    getCvc(),
                    getCardholderName()
                )
            )
        }
    }

    private fun isCardNumberValid(): Boolean = etStripeCardNumber.isCardNumberValid

    private fun isExpiryDateValid(): Boolean = etExpiryDate.isDateValid

    private fun isCvcValid(): Boolean = etCvc.text.toString().length >= CVC_MIN_LENGTH

    private fun isCardholderNameValid(): Boolean = etCardholderName.text.isNullOrBlank().not()

    private fun getCardNumber() = etStripeCardNumber.text.toString().replace(" ", "")

    private fun getCvc() = etCvc.text.toString()

    private fun getExpiryDate() = etExpiryDate.validatedDate

    private fun getCardholderName() = etCardholderName.text.toString()

    private fun setToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_close_listing)
        binding.toolbar.setNavigationOnClickListener { viewModel.navigateBack() }
    }

    private fun setCardBrand(newBrand: CardBrand) {
        currentCardBrand = newBrand

        etCvc.filters = arrayOf(InputFilter.LengthFilter(newBrand.maxCvcLength))

        tilCvc.hint = if (newBrand == CardBrand.AmericanExpress) {
            resources.getString(R.string.cvc_amex_hint)
        } else {
            resources.getString(R.string.cvc_number_hint)
        }

        ivCardBrand.setImageResource(newBrand.icon)
        if (newBrand == CardBrand.Unknown) {
            applyCardTint()
        }
    }

    private fun applyCardTint() {
        ivCardBrand.setImageDrawable(
            DrawableCompat.unwrap(
                DrawableCompat.wrap(ivCardBrand.drawable).also { compatIcon ->
                    DrawableCompat.setTint(
                        compatIcon.mutate(),
                        ResourcesCompat.getColor(ivCardBrand.resources, R.color.silver_sand, null)
                    )
                }
            )
        )
    }

    private fun applyPlanIconTint(@ColorRes color: Int) {
        ivPlanIcon.setImageDrawable(
            DrawableCompat.unwrap(
                DrawableCompat.wrap(ivPlanIcon.drawable).also { compatIcon ->
                    DrawableCompat.setTint(
                        compatIcon.mutate(),
                        ResourcesCompat.getColor(ivPlanIcon.resources, color, null)
                    )
                }
            )
        )
    }

    private fun showProgress() {
        if (progressDialog == null) {
            createProgressDialog()
        }
        progressDialog?.show()
    }

    private fun hideProgress() {
        progressDialog?.dismiss()
    }

    private fun createProgressDialog() {
        context?.let {
            progressDialog = AlertDialog.Builder(it)
                .setView(R.layout.dialog_payment_progress)
                .setCancelable(false)
                .create()
                .apply {
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
        }
    }

    private fun showErrorDialog(errorMessage: String?, closeActivityOnDismiss: Boolean) {
        context?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.error_processing_payment))
                .setMessage(errorMessage)
                .setCancelable(false)
                .setNegativeButton(getString(R.string.close)) { _, _ ->
                    if (closeActivityOnDismiss) activity?.finish()
                }
                .show()
        }
    }

    companion object {
        private const val CVC_MIN_LENGTH = 3
    }
}