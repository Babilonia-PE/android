package com.babilonia.presentation.flow.main.publish.facilities

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.Observer
import com.babilonia.R
import com.babilonia.databinding.FacilitiesFragmentBinding
import com.babilonia.domain.model.Facility
import com.babilonia.domain.model.enums.PropertyType
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.flow.main.publish.common.BaseCreateListingFragment
import com.babilonia.presentation.flow.main.publish.createlisting.CreateListingContainerViewModel
import com.babilonia.presentation.flow.main.publish.facilities.common.FacilitiesRecyclerAdapter
import com.babilonia.presentation.flow.main.publish.facilities.common.FacilityChangeListener

class FacilitiesFragment : BaseCreateListingFragment<FacilitiesFragmentBinding, CreateListingContainerViewModel>() {
    companion object {
        fun newInstance() = FacilitiesFragment()
    }

    private var facilitiesRecyclerAdapter: FacilitiesRecyclerAdapter? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        retainInstance = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel.propertySelectedEvent.postValue(sharedViewModel.property.value)
    }

    override fun viewCreated() {
        subscribeToFacilitiesUpdates()
        initRecycler()
    }

    override fun onResume() {
        super.onResume()
        val facilities = sharedViewModel.facilities.value
        if (facilities == null || facilities.isEmpty()) {
            context?.resources?.let {
                val localizedPropertyType = PropertyType.getLocalizedPropertyName(it, sharedViewModel.property.value)
                binding.tvEmptyState.text = getString(R.string.no_facilities_hint, localizedPropertyType)
                binding.tvEmptyState.visible()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedViewModel.propertySelectedEvent.removeObservers(this)
        viewModel.gotFacilitiesEvent.removeObservers(this)
    }

    private fun initRecycler() {
        if (facilitiesRecyclerAdapter == null) {
            facilitiesRecyclerAdapter = FacilitiesRecyclerAdapter(object : FacilityChangeListener {
                override fun onChange(value: Facility?) {
                    sharedViewModel.onFacilityChange(value)
                }
            })
        }
        binding.rcFacilitiesContainer.adapter = facilitiesRecyclerAdapter

    }

    private fun subscribeToFacilitiesUpdates() {
        sharedViewModel.propertySelectedEvent.observe(this, Observer { unlocalizedName ->
            context?.resources?.let {
                val localizedPropertyType = PropertyType.getLocalizedPropertyName(it, unlocalizedName)
                binding.tvEmptyState.text = getString(R.string.no_facilities_hint, localizedPropertyType)
            }
            unlocalizedName?.let {
                viewModel.getFacilities(unlocalizedName)
            }
        })
        viewModel.gotFacilitiesEvent.observe(this, Observer {
            sharedViewModel.setFacilities(it.toMutableList())
            if (it.isEmpty()) {
                binding.tvEmptyState.visible()
            } else {
                binding.tvEmptyState.invisible()
            }
            facilitiesRecyclerAdapter?.addAll(it)
        })
    }
}

