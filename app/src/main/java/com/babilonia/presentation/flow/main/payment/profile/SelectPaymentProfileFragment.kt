package com.babilonia.presentation.flow.main.payment.profile

import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Typeface.BOLD
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import com.babilonia.R
import com.babilonia.databinding.FragmentPaymentSelectProfileBinding
import com.babilonia.domain.model.enums.PaymentProfile
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.flow.main.payment.PaymentActivitySharedViewModel
import kotlinx.android.synthetic.main.fragment_payment_select_profile.*

class SelectPaymentProfileFragment : BaseFragment<FragmentPaymentSelectProfileBinding, PaymentActivitySharedViewModel>() {

    private var selectedItemBackground: Drawable? = null
    private var unselectedItemBackground: Drawable? = null

    override fun viewCreated() {
        initButtonBackgrounds()
        setToolbar()
        setClicks()
        highlightPreselectedProfile()
        initAlertMessage()
    }

    private fun highlightPreselectedProfile() {
        when (viewModel.selectedProfile) {
            PaymentProfile.OWNER -> btnOwner.callOnClick()
            PaymentProfile.REALTOR -> btnRealtor.callOnClick()
            PaymentProfile.CONSTRUCTION_COMPANY -> btnConstructor.callOnClick()
        }
    }

    private fun initButtonBackgrounds() {
        context?.let {
            selectedItemBackground = ContextCompat.getDrawable(it, R.drawable.bg_rect_outlined)
            unselectedItemBackground = ContextCompat.getDrawable(it, R.drawable.bg_rect_rounded_dirty_white)
        }
    }

    private fun initAlertMessage() {
        val alertStr = getString(R.string.realtor_constructor_alert)
        val websiteStr = getString(R.string.website)
        var spannable = SpannableStringBuilder(alertStr)
        spannable = spannable.append(" ").append(websiteStr)
        context?.let {
            spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(it, R.color.colorAccent)), alertStr.length, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(BOLD), alertStr.length, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        tvAlert.text = spannable
        tvAlert.setOnClickListener { openUrl() }
    }

    private fun setClicks() {
        val onOwnerClickListener = View.OnClickListener {
            selectItem(btnOwner)
            unselectItem(btnRealtor)
            unselectItem(btnConstructor)
            viewModel.selectedProfile = PaymentProfile.OWNER
        }
        // TODO these options are disabled for now, maybe they will be implemented later
//        val onRealtorClickListener = View.OnClickListener {
//            unselectItem(btnOwner)
//            selectItem(btnRealtor)
//            unselectItem(btnConstructor)
//            viewModel.selectedProfile = PaymentProfile.REALTOR
//        }
//        val onConstructorClickListener = View.OnClickListener {
//            unselectItem(btnOwner)
//            unselectItem(btnRealtor)
//            selectItem(btnConstructor)
//            viewModel.selectedProfile = PaymentProfile.CONSTRUCTION_COMPANY
//        }
        btnOwner.setOnClickListener(onOwnerClickListener)
        tvOwner.setOnClickListener(onOwnerClickListener)
        // TODO these options are disabled for now, maybe they will be implemented later
//        btnRealtor.setOnClickListener(onRealtorClickListener)
//        tvRealtor.setOnClickListener(onRealtorClickListener)
//        btnConstructor.setOnClickListener(onConstructorClickListener)
//        tvConstructor.setOnClickListener(onConstructorClickListener)
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val colorFilter = ColorMatrixColorFilter(colorMatrix)
        btnRealtor.colorFilter = colorFilter
        btnConstructor.colorFilter = colorFilter

        btnSelectPaymentProfile.setOnClickListener {
            viewModel.navigateToSelectPaymentPlan()
        }
    }

    private fun selectItem(item: AppCompatImageButton) {
        item.background = selectedItemBackground
    }

    private fun unselectItem(item: AppCompatImageButton) {
        item.background = unselectedItemBackground
    }

    private fun openUrl() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://${getString(R.string.website)}"))
        startActivity(browserIntent)
    }

    private fun setToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_close_listing)
        binding.toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    }
}