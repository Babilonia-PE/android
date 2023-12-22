package com.babilonia.presentation.flow.main.search.list

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.babilonia.R
import com.babilonia.databinding.SearchFragmentBinding
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.enums.SortType
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.extension.visibleOrGone
import com.babilonia.presentation.flow.main.common.EndlessScrollListener
import com.babilonia.presentation.flow.main.common.ListingPreviewRecyclerAdapter
import com.babilonia.presentation.flow.main.search.ListingSearchViewModel
import com.babilonia.presentation.view.dialog.StyledAlertDialog
import com.babilonia.presentation.view.textpickerdialog.TextPickerDialog
import com.tbruyelle.rxpermissions2.RxPermissions


class SearchFragment : BaseFragment<SearchFragmentBinding, ListingSearchViewModel>() {

    private var adapter: ListingPreviewRecyclerAdapter? = null
    private var endlessScrollListener: EndlessScrollListener? = null
    private var locationInfo: ILocation? = null
    var listaAcumulativa: MutableList<Listing> = mutableListOf()
    var listaNueva: MutableList<Listing> = mutableListOf()

    override fun viewCreated() {
        binding.model = viewModel
        viewModel.setIsShowScreenMap(false)
        viewModel.clearSearchFromMap(false)
        setupRecyclerView()
        updateSortingView()
        viewModel.getUserId()
        withRequestLocationPermission { isGranted ->
            if (!isGranted) {
                switchSortingToDefault()
            }
            viewModel.getListingsWithLocationPermission(isGranted)
        }
        setClicks()

        binding.rcListingsContainer.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    if (lastVisibleItemPosition == totalItemCount - 1) {
                        onLoadMore()
                    }
                }
        })
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
        with(viewModel) {
            listings.observe(this@SearchFragment, Observer { listings ->
                listaNueva = mutableListOf()
                if (viewModel.pagesInfo == 1) {
                    listaAcumulativa = listings.toMutableList()
                } else {
                    listaNueva = listings.toMutableList()
                }
                if (listaNueva.isNotEmpty()) {
                    listaAcumulativa.addAll(listaNueva)
                }
                adapter?.let {
                    val resetAdapter: Boolean = viewModel.needToResetAdapter
                    if (resetAdapter) {
                        endlessScrollListener?.reset()
                        it.addItems(listaAcumulativa, resetAdapter)
                        viewModel.needToResetAdapter = false
                        binding.rcListingsContainer.scrollToPosition(0)
                    } else {
                        it.addItems(listaAcumulativa, resetAdapter)
                    }
                }
                updateEmptyStateVisibility()
                updateSortingVisibility()
                binding.constraintLayout.visibility = View.GONE
                endlessScrollListener?.cancelLoading()
            })
            locationLiveData.observe(this@SearchFragment, Observer {
                locationInfo = it
                subscribeToMyLocation(it)
            })
            gpsUnavailableError.observe(this@SearchFragment, Observer {
                switchSortingToDefault()
                showLocationUnavailableDialog()
            })
            userIdLiveData.observe(this@SearchFragment, Observer {
                adapter?.setCurrentUserId(it)
            })
            topListingsLiveData.observe(this@SearchFragment, Observer { topListings ->
                adapter?.addTopListings(topListings)
                topListingsVisibilityLiveData.value?.let {
                    adapter?.setTopListingsVisible(it)
                }
                updateEmptyStateVisibility()
                updateSortingVisibility()
            })
            topListingsVisibilityLiveData.observe(this@SearchFragment, Observer {
                adapter?.setTopListingsVisible(it)
                updateEmptyStateVisibility()
                updateSortingVisibility()
            })

            isLoading.observe(this@SearchFragment, Observer{
                hideLoading(it)
            })
        }
    }

    private fun hideLoading(status: Boolean){
        binding.constraintLayout.visibleOrGone(status)
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        with(viewModel) {
            listings.removeObservers(this@SearchFragment)
            locationLiveData.removeObservers(this@SearchFragment)
            suggestions.removeObservers(this@SearchFragment)
            gpsUnavailableError.removeObservers(this@SearchFragment)
            topListingsLiveData.removeObservers(this@SearchFragment)
        }
    }

    override fun onPause() {
        viewModel.topListingsSavedState = adapter?.saveTopListingsState()
        super.onPause()
    }

    private fun updateSortingVisibility() {
        val hasListings = viewModel.listings.value.isNullOrEmpty().not()
        val hasTopListings = viewModel.topListingsVisibilityLiveData.value ?: false
        if (hasTopListings) {
            binding.layoutSorting.llSorting.invisible()
            if (hasListings) {
                adapter?.setFilterVisible(true)
            }
        } else {
            adapter?.setFilterVisible(false)
            if (hasListings) {
                binding.layoutSorting.llSorting.visible()
            }
        }
    }

    private fun updateEmptyStateVisibility() {
        val hasNoListings = viewModel.listings.value.isNullOrEmpty()
        val hasNoTopListings = viewModel.topListingsLiveData.value.isNullOrEmpty()
        val shouldNotShowTopListings = viewModel.topListingsVisibilityLiveData.value != true
        if (hasNoListings && (hasNoTopListings || shouldNotShowTopListings)) {
            binding.emptyGroup.visible()
        } else {
            binding.emptyGroup.invisible()
        }
    }

    private fun setupRecyclerView() {
        val listingsAdapter = ListingPreviewRecyclerAdapter(viewModel) { showSortingDialog() }
        viewModel.topListingsSavedState?.let {
            listingsAdapter.topListingsRestoredState = it
            viewModel.topListingsSavedState = null
        }

        endlessScrollListener =
            EndlessScrollListener(
                binding.rcListingsContainer.layoutManager as LinearLayoutManager,
                viewModel.paginator,
                listingsAdapter,
                viewModel.lastLoadedPageIndex
            ).apply {
                binding.rcListingsContainer.addOnScrollListener(this)
            }

        adapter = listingsAdapter
        binding.rcListingsContainer.adapter = listingsAdapter
        binding.rcListingsContainer.itemAnimator?.changeDuration = 0
    }

    private fun showSortingDialog() {
        val titles = resources.getStringArray(R.array.sort_types).toList()
        TextPickerDialog(
            requireContext(),
            titles,
            viewModel.sortingBy,
            object : TextPickerDialog.NumberPickerCallBack {
                override fun onSelectingValue(value: String) {
                    viewModel.sortingBy = titles.indexOf(value)
                    setSortByText(value)
                    viewModel.needToResetPaginator = true
                    viewModel.getListings()
                }
            }).show()
    }

    private fun setSortByText(text: String) {
        val sortText = ": $text"
        adapter?.setFilterText(sortText)
        binding.layoutSorting.tvSortBy.text = sortText
    }

    private fun setClicks() {
        binding.fabToMap.setOnClickListener {
            viewModel.setSearchByBounds(true)
            viewModel.setIsForcedLocationGPS(false)
            viewModel.setSearchWithSwipeMap(false)
            viewModel.setIsSearchByAutocomplete(false)
            findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToListingsMapFragment())
        }
        binding.layoutSorting.llSorting.setOnClickListener { showSortingDialog() }
    }

    private fun subscribeToMyLocation(location: ILocation) {
        if(!location.department.isNullOrBlank() ||
            !location.province.isNullOrBlank() ||
            !location.district.isNullOrBlank() ||
            !location.address.isNullOrBlank()) {
            viewModel.getListings(location)
            viewModel.getTopListings(location)
        }else {
            viewModel.getTopListingsLoading(location)
        }
    }

    private fun withRequestLocationPermission(onComplete: (isGranted: Boolean) -> Unit) {
        when (checkLocationPermission()) {
            PackageManager.PERMISSION_GRANTED -> {
                onComplete(true)
            }
            else -> {
                if (needToShowLocationRationale()) {
                    context?.let {
                        StyledAlertDialog.Builder(it)
                            .setTitleText(getString(R.string.location_rationale_title))
                            .setBodyText(getString(R.string.location_rationale_body))
                            .setRightButton(getString(R.string.allow)) {
                                requestPermission(onComplete)
                            }
                            .setLeftButton(getString(R.string.deny)) {
                                onComplete(false)
                            }
                            .build()
                            .show()
                    }
                } else {
                    requestPermission(onComplete)
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun requestPermission(onComplete: (isGranted: Boolean) -> Unit) {
        RxPermissions(this)
            .request(Manifest.permission.ACCESS_FINE_LOCATION)
            .subscribe {
                onComplete(it)
            }
    }

    private fun checkLocationPermission() = ContextCompat.checkSelfPermission(requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION)

    private fun needToShowLocationRationale() = ActivityCompat.shouldShowRequestPermissionRationale(
        requireActivity(),
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private fun showLocationUnavailableDialog() {
        AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_gps_unavailable)
            .create()
            .apply {
                setOnShowListener {
                    findViewById<Button>(R.id.btnGoToSettings)?.setOnClickListener {
                        try {
                            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        } catch (ignore: Exception) {
                        }
                        dismiss()
                    }
                    findViewById<Button>(R.id.btnNotNow)?.setOnClickListener {
                        viewModel.getDefaultPlace()
                        dismiss()
                    }
                }
            }.show()
    }

    private fun updateSortingView() {
        val sortTypes = resources.getStringArray(R.array.sort_types).toList()
        setSortByText(sortTypes[viewModel.sortingBy])
    }

    private fun switchSortingToDefault() {
        viewModel.sortingBy = SortType.MOST_RELEVANT.ordinal
        updateSortingView()
    }

     fun onLoadMore() {
        if (viewModel.pagesInfo < viewModel.total_pages) {
            binding.constraintLayout.visibility = View.VISIBLE
            viewModel.pagesInfo++
            locationInfo?.let { viewModel.getListings(it, viewModel.pagesInfo) }
        }
    }
}
