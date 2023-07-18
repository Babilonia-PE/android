package com.babilonia.presentation.flow.main.publish.advanced

import android.content.Context
import androidx.lifecycle.Observer
import com.babilonia.R
import com.babilonia.databinding.FragmentAdvancedDetailsBinding
import com.babilonia.domain.model.Facility
import com.babilonia.domain.model.enums.PropertyType
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.flow.main.publish.common.BaseCreateListingFragment
import com.babilonia.presentation.flow.main.publish.createlisting.CreateListingContainerViewModel
import com.babilonia.presentation.flow.main.publish.facilities.common.FacilitiesRecyclerAdapter
import com.babilonia.presentation.flow.main.publish.facilities.common.FacilityChangeListener

class AdvancedDetailsFragment
    : BaseCreateListingFragment<FragmentAdvancedDetailsBinding, CreateListingContainerViewModel>() {

    companion object {
        fun newInstance() = AdvancedDetailsFragment()
    }

    private var advancedDetailsRecyclerAdapter: FacilitiesRecyclerAdapter? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        retainInstance = true
    }

    override fun viewCreated() {
        subscribeToAdvancedDetailsUpdates()
        initRecycler()
    }

    override fun onResume() {
        super.onResume()
        val details = sharedViewModel.advancedDetails.value
        if (details == null || details.isEmpty()) {
            fillEmptyStateData()
            binding.tvEmptyState.visible()
        }
    }

    private fun subscribeToAdvancedDetailsUpdates() {
        viewModel.gotAdvancedDetailsEvent.observe(this, Observer {
            sharedViewModel.setAdvancedDetails(it.toMutableList())
            if (it.isEmpty()) {
                fillEmptyStateData()
                binding.tvEmptyState.visible()
            } else {
                binding.tvEmptyState.invisible()
            }
            advancedDetailsRecyclerAdapter?.addAll(it)
        })
    }

    private fun fillEmptyStateData() {
        context?.resources?.let {
            val localizedPropertyType = PropertyType.getLocalizedPropertyName(it, sharedViewModel.property.value)
            binding.tvEmptyState.text = getString(R.string.no_advanced_details_hint, localizedPropertyType)
        }
    }

    private fun initRecycler() {
        if (advancedDetailsRecyclerAdapter == null) {
            advancedDetailsRecyclerAdapter = FacilitiesRecyclerAdapter(object : FacilityChangeListener {
                override fun onChange(value: Facility?) {
                    sharedViewModel.onAdvancedDetailChange(value)
                }
            })
        }
        binding.rvAdvancedDetails.adapter = advancedDetailsRecyclerAdapter
    }
}