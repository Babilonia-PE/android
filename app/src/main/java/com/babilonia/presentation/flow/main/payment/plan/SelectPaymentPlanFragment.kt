package com.babilonia.presentation.flow.main.payment.plan

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.babilonia.R
import com.babilonia.databinding.FragmentPaymentSelectPlanBinding
import com.babilonia.domain.model.enums.PaymentPlanKey
import com.babilonia.domain.model.payment.PaymentPlan
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.flow.main.payment.PaymentActivitySharedViewModel
import com.babilonia.presentation.utils.StartSnapHelper
import kotlinx.android.synthetic.main.fragment_payment_select_plan.*

class SelectPaymentPlanFragment : BaseFragment<FragmentPaymentSelectPlanBinding, PaymentActivitySharedViewModel>() {

    private var maxVelocity: Int = 0

    private val plansAdapter = SelectPaymentPlanAdapter()
    private lateinit var plansLayoutManager: LinearLayoutManager

    private val descriptionsAdapter = SelectPaymentPlanDescriptionAdapter()

    override fun viewCreated() {
        setToolbar()
        setClicks()
        calculateMaxVelocity()
        initPlansAdapter()
        initDescriptionsAdapter()
        observeViewModel()
        viewModel.getPaymentPlans()
    }

    private fun observeViewModel() {
        viewModel.getPaymentPlansLiveData().observe(this, Observer { paymentPlans ->
            if (plansAdapter.getRealItemCount() == 0) {
                setItems(paymentPlans)
            } else {
                pagerIndicator.setDotCount(plansAdapter.getRealItemCount())
                btnSelectPaymentPlan.isEnabled = true
            }
        })
    }

    private fun setItems(paymentPlans: List<PaymentPlan>) {
        if (paymentPlans.isNotEmpty()) {
            plansAdapter.setItems(paymentPlans)

            val plusPlan = paymentPlans.firstOrNull { it.key == PaymentPlanKey.PLUS }
            val plusPlanPosition = if (plusPlan != null) {
                paymentPlans.indexOf(plusPlan)
            } else {
                0
            }

            descriptionsAdapter.setItems(paymentPlans[plusPlanPosition].descriptions)
            viewModel.selectedPlan = paymentPlans[plusPlanPosition]
            plansLayoutManager.scrollToPositionWithOffset(plusPlanPosition, 0)
            pagerIndicator.reattach()
            pagerIndicator.setDotCount(paymentPlans.size)
            pagerIndicator.setCurrentPosition(plusPlanPosition)
            btnSelectPaymentPlan.isEnabled = true
        }
    }

    private fun calculateMaxVelocity() {
        maxVelocity = resources.getDimensionPixelSize(R.dimen.payment_plan_vh_width) * 3
    }

    private fun initPlansAdapter() {
        rvPaymentPlans.adapter = plansAdapter

        // scrolls RecyclerView to position closest card at the start of the screen
        StartSnapHelper().attachToRecyclerView(rvPaymentPlans)

        plansLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvPaymentPlans.layoutManager = plansLayoutManager
        pagerIndicator.attachToRecyclerView(rvPaymentPlans, 0)
        rvPaymentPlans.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // when scroll or fling has finished, we need to show payment plan description
                // which corresponds to picked card
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val selectedItemPosition = plansLayoutManager.findFirstCompletelyVisibleItemPosition()
                    viewModel.getPaymentPlansLiveData().value?.let { plans ->
                        if (selectedItemPosition >= 0 && plans.size > selectedItemPosition) {
                            descriptionsAdapter.setItems(plans[selectedItemPosition].descriptions)
                            rvPaymentPlanDescription.scrollToPosition(0)
                            viewModel.selectedPlan = plans[selectedItemPosition]
                        }
                    }
                }
            }
        })
        rvPaymentPlans.onFlingListener = object : RecyclerView.OnFlingListener() {
            // we need to swipe cards one by one, not whole carousel with one fling
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                return when {
                    velocityX > maxVelocity -> {
                        rvPaymentPlans.fling(maxVelocity, velocityY)
                        true
                    }
                    velocityX < -maxVelocity -> {
                        rvPaymentPlans.fling(-maxVelocity, velocityY)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun initDescriptionsAdapter() {
        rvPaymentPlanDescription.adapter = descriptionsAdapter
        rvPaymentPlanDescription.layoutManager = LinearLayoutManager(context)
    }

    private fun setClicks() {
        btnSelectPaymentPlan.setOnClickListener {
            viewModel.navigateToSelectPaymentPeriod()
        }
    }

    private fun setToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_close_listing)
        binding.toolbar.setNavigationOnClickListener { viewModel.navigateBack() }
    }
}