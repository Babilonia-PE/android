package com.babilonia.presentation.flow.main.payment.checkout

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.doAfterTextChanged
import com.babilonia.BuildConfig
import com.babilonia.Constants
import com.babilonia.Constants.BUNDLE_PAYMENT_MESSAGE
import com.babilonia.R
import com.babilonia.databinding.FragmentCheckoutBinding
import com.babilonia.domain.model.enums.PaymentPlanKey
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.extension.gone
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.flow.main.payment.PaymentActivitySharedViewModel
import com.babilonia.presentation.utils.PriceFormatter
import com.babilonia.presentation.utils.SvgUtil.formatByGroup
import com.babilonia.presentation.utils.SvgUtil.formatExpiryDate
import com.babilonia.presentation.utils.SvgUtil.removeCharacter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.fragment_checkout.*
import java.util.*

class CheckoutFragment : BaseFragment<FragmentCheckoutBinding, PaymentActivitySharedViewModel>() {

    private var progressDialog: AlertDialog? = null

    companion object {
        private const val DIM_GROUP_CARD   = 4
        private const val DIM_GROUP_DATE   = 2
        private const val CARD_MIN_LENGTH  = 15
        private const val CVC_MIN_LENGTH   = 3
        private const val DATE_EXPIRED_MAX = 5
    }

    override fun viewCreated() {
        setToolbar()
        fillUiFields()
        listenToNumberCard()
        listenToDateExpired()
        listenToCVC()
        listenToCardHolderName()
        setOnPayClickListener()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.getErrorLiveData().observe(this, {
            hideProgress()
            try{
                it?.let{ message ->
                    if(message.contains(resources.getString(R.string.mapper_error_copy_rxjava)))
                        showErrorDialog(resources.getString(R.string.error_processing_payment_backend), true)
                    else showErrorDialog(message, true)
                }?:run{
                    showErrorDialog(resources.getString(R.string.error_processing_payment_backend), true)
                }
            }catch (e: Exception){
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
                showErrorDialog(resources.getString(R.string.error_processing_payment_backend), true)
            }
        })

        viewModel.getPaymentIntentLiveData().observe(this, {
            viewModel.doPayment(getCardNumber(), getCvc(), getExpiryDate(), getCardholderName())
        })

        viewModel.getDoPaymentLiveData().observe(this, { doPayment ->
            hideProgress()
            activity?.let {
                val bundle = Bundle()
                bundle.putSerializable(BUNDLE_PAYMENT_MESSAGE, doPayment.description?:resources.getString(R.string.listing_published_successfully))
                it.setResult(Activity.RESULT_OK)
                it.finish()
            }
        })

        validateLinks(viewModel.publisherRole)
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

        tvTermsAndConditions.setOnClickListener {
            showConditions()
        }
    }

    private fun listenToNumberCard() {
        etCardNumber.keyListener = DigitsKeyListener.getInstance(getString(R.string.limit_card_number))
        etExpiryDate.keyListener = DigitsKeyListener.getInstance(getString(R.string.limit_card_number))
        etCvc.keyListener        = DigitsKeyListener.getInstance(getString(R.string.limit_card_number))

        etCardNumber.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                etCardNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                s?.let{ editable ->
                    if (editable.isNotEmpty()){
                        etCardNumber.removeTextChangedListener(this)
                        etCardNumber.setText(formatByGroup(editable.toString(), DIM_GROUP_CARD, " "))
                        etCardNumber.setSelection(etCardNumber.text?.length?:0)
                        etCardNumber.addTextChangedListener(this)
                    }
                }
            }
        })
    }

    private fun listenToDateExpired() {
        etExpiryDate.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                etExpiryDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                s?.let{ editable ->
                    if (editable.isNotEmpty()){
                        etExpiryDate.removeTextChangedListener(this)
                        etExpiryDate.setText(formatByGroup(editable.toString(), DIM_GROUP_DATE, "/"))
                        etExpiryDate.setSelection(etExpiryDate.text?.length?:0)
                        etExpiryDate.addTextChangedListener(this)
                    }
                }
            }
        })
    }

    private fun listenToCVC(){
        etCvc.doAfterTextChanged {
            etCvc.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    private fun listenToCardHolderName(){
        etCardholderName.doAfterTextChanged{
            etCvc.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    private fun setOnPayClickListener() {
        btnPay.setOnClickListener {
            if (isPaymentDataValid()) {
                 showProgress()
                 viewModel.createPaymentIntent()
            }else {
                showErrorDialog(resources.getString(R.string.please_fill_in_the_fields_correctly))
            }
        }
    }

    private fun isPaymentDataValid(): Boolean {
        tilCardNumber.error     = null
        tilExpiryDate.error     = null
        tilCvc.error            = null
        tilCardholderName.error = null
        return when {
            isCardNumberValid().not()     -> {
                etCardNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorError))
                tilCardNumber.error = " "
                etCardNumber.requestFocus()
                false
            }
            isDateValid().not()           -> {
                etExpiryDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorError))
                tilExpiryDate.error = " "
                etExpiryDate.requestFocus()
                false
            }
            isCvcValid().not()            -> {
                etCvc.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorError))
                tilCvc.error = " "
                etCvc.requestFocus()
                false
            }
            isCardholderNameValid().not() -> {
                etCardholderName.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorError))
                tilCardholderName.error = " "
                etCardholderName.requestFocus()
                false
            }
            else -> {
                etCardNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                etExpiryDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                etCvc.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                etCardholderName.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                true
            }
        }
    }

    private fun isCardNumberValid(): Boolean = removeCharacter(etCardNumber.text.toString()).length>=CARD_MIN_LENGTH

    private fun isDateValid(): Boolean {
        try {
            val dim = etExpiryDate.toString().replace(" ", "").length
            if (dim < DATE_EXPIRED_MAX)
                return false

            val today = Calendar.getInstance()
            val year = today.get(Calendar.YEAR).toString().substring(1, 4).toInt()
            val month = today.get(Calendar.MONTH).toString().toInt().plus(1)

            etExpiryDate.text?.toString()?.replace(" ", "")?.let { expiredDate ->
                return if (expiredDate.contains("/")) {
                    val arr = expiredDate.split("/")
                    val tempYear = arr[1].toInt()
                    val tempMonth = arr[0].toInt()
                    when {
                        tempYear == year -> {
                            if (tempMonth in 1..12) {
                                tempMonth >= month
                            } else false
                        }
                        tempYear < year -> false
                        tempYear > year -> true
                        else -> false
                    }
                } else false
            } ?: run {
                return false
            }
        }catch (e:Exception){
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            return false
        }
    }

    private fun isCvcValid(): Boolean = removeCharacter(etCvc.text.toString()).length >= CVC_MIN_LENGTH

    private fun isCardholderNameValid(): Boolean = !etCardholderName.text.isNullOrBlank()

    private fun getCardNumber() = etCardNumber.text.toString().replace(" ", "")

    private fun getCvc() = etCvc.text.toString().replace(" ", "")

    private fun getExpiryDate() = etExpiryDate.text.toString().replace(" ", "")

    private fun getCardholderName() = etCardholderName.text.toString().trim()

    private fun setToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_close_listing)
        binding.toolbar.setNavigationOnClickListener { viewModel.navigateBack() }
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

    private fun showErrorDialog(errorMessage: String?, closeActivityOnDismiss: Boolean = false) {
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

    private fun validateLinks(publisherRole: String?){
        when(publisherRole?.trim()){
            Constants.PUBLISHER_ROLE_REALTOR -> {
                tvByClicking.visible()
                tvTermsAndConditions.visible()
            }
            Constants.PUBLISHER_ROLE_OWNER -> {
                tvByClicking.visible()
                tvTermsAndConditions.visible()
            }
            else -> {
                tvByClicking.gone()
                tvTermsAndConditions.gone()
            }
        }
    }

    private fun showConditions(){
        val web = activity?.getString(R.string.url_terms_and_conditions) ?: ""
        val url = BuildConfig.BASE_URL_WEB + web
        val uri    = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}