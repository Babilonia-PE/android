package com.babilonia.presentation.flow.main.payment.period

import androidx.recyclerview.widget.LinearLayoutManager
import com.babilonia.R
import com.babilonia.databinding.FragmentPaymentSelectPeriodBinding
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.flow.main.payment.PaymentActivitySharedViewModel
import kotlinx.android.synthetic.main.fragment_payment_select_period.*

class SelectPaymentPeriodFragment : BaseFragment<FragmentPaymentSelectPeriodBinding, PaymentActivitySharedViewModel>() {

    private val adapter = SelectPaymentPeriodAdapter()

    override fun viewCreated() {
        setToolbar()
        initAdapter()

        viewModel.selectedPlan?.let { paymentPlanCard.setData(it) }

        btnCheckout.setOnClickListener {
            viewModel.onPaymentPeriodSelected(adapter.checkedItemIndex)
        }
    }

    private fun initAdapter() {
        rvPaymentPeriod.adapter = adapter
        rvPaymentPeriod.layoutManager = LinearLayoutManager(context)
        viewModel.selectedPlan?.let { selectedPlan ->
            adapter.setItems(selectedPlan.products)
            viewModel.selectedPeriod?.let { selectedPeriod ->
                if (selectedPlan.products.contains(selectedPeriod)) {
                    adapter.selectItem(selectedPeriod)
                }
            }
        }

    }

    private fun setToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_close_listing)
        binding.toolbar.setNavigationOnClickListener { viewModel.navigateBack() }
    }
}