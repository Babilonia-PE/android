package com.babilonia.presentation.flow.main.search.filters

import androidx.lifecycle.Observer
import com.babilonia.Constants
import com.babilonia.R
import com.babilonia.databinding.ListingFiltersFragmentBinding
import com.babilonia.domain.model.Facility
import com.babilonia.domain.model.enums.FilterType
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.flow.main.search.ListingSearchViewModel

class ListingFiltersFragment : BaseFragment<ListingFiltersFragmentBinding, ListingSearchViewModel>() {

    private var facilitiesRecyclerAdapter: FilterFacilitiesAdapter? = null

    override fun viewCreated() {
        binding.model = viewModel
        viewModel.initTempFacilities()
        setToolbar()
        binding.filterGeneral.initGeneralFilters(viewModel)
        binding.filterQuality.initQualityFilters(viewModel)
        initRecycler()
        setClicks()
        viewModel.metadataUpdateSubject.onNext(true)
        requestFacilities()
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
        viewModel.priceRangeLiveData.observe(this, androidx.lifecycle.Observer {
            binding.filterGeneral.initRangeBar(it)
        })
        viewModel.listingsMetadata.observe(this, Observer {
            binding.filterQuality.setMetadata(it)
        })
        viewModel.gotFacilitiesEvent.observe(this, Observer { newFacilities ->
            handleNewFacilities(newFacilities)
        })
        viewModel.filtersVisibilityLiveData.observe(this, Observer {
            binding.filterQuality.setVisibility(it)
        })
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        viewModel.priceRangeLiveData.removeObservers(this)
        viewModel.listingsMetadata.removeObservers(this)
        viewModel.gotFacilitiesEvent.removeObservers(this)
        viewModel.filtersVisibilityLiveData.removeObservers(this)
    }

    private fun handleNewFacilities(newFacilities: List<Facility>) {
        if (newFacilities.isEmpty()) {
            binding.tvFacilitiesTitle.invisible()
            binding.rvFacilities.invisible()
        } else {
            binding.tvFacilitiesTitle.visible()
            binding.rvFacilities.visible()
        }

        val checkedItems = viewModel.getTempFacilities().filter { it.isChecked }

        if (checkedItems.isNotEmpty()) {
            checkedItems.forEach { checkedFacilities ->
                newFacilities.firstOrNull { it.id == checkedFacilities.id }?.let {
                    it.isChecked = true
                }
            }
            facilitiesRecyclerAdapter?.let {
                it.addAll(newFacilities)
                it.onItemChecked()
            }
        } else {
            facilitiesRecyclerAdapter?.addAll(newFacilities)
        }
    }

    private fun requestFacilities() {
        if (viewModel.getFilter(FilterType.PROPERTY) == null) {
            viewModel.getFacilities(Constants.ALL_FACILITIES)
        } else {
            viewModel.getFilter(FilterType.PROPERTY)?.let { filter ->
                viewModel.getFacilities(filter.value)
            }
        }
    }

    private fun initRecycler() {
        if (facilitiesRecyclerAdapter == null) {
            facilitiesRecyclerAdapter = FilterFacilitiesAdapter(
                onItemClickListener = viewModel,
                onHeaderClick = { isChecked -> viewModel.onAllFacilitiesClicked(isChecked) },
                headerText = getString(R.string.all_facilities)
            )
        }
        binding.rvFacilities.adapter = facilitiesRecyclerAdapter

    }

    private fun setClicks() {
        binding.tvReset.setOnClickListener {
            viewModel.needToResetPaginator = true
            viewModel.clearFilters()
            binding.filterGeneral.initDefaultFilters()
            binding.filterGeneral.resetRangeBar()
            binding.filterQuality.resetFilters()
            facilitiesRecyclerAdapter?.resetAllFacilities()
            viewModel.metadataUpdateSubject.onNext(true)
            viewModel.onPropertyTypeChanged(Constants.ALL_FACILITIES)
            requestFacilities()
        }
    }

    private fun setToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_close_listing)
        binding.toolbar.setNavigationOnClickListener { viewModel.navigateBack() }
    }
}
